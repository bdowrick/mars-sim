/*
 * Mars Simulation Project
 * HelpGenerator.java
 * @date 2024-02-17
 * @author Barry Evans
 */
package com.mars_sim.tools.helpgenerator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.SimulationRuntime;
import com.mars_sim.core.process.ProcessInfo;
import com.mars_sim.core.resource.ItemType;

public class HelpGenerator {

	// Name of the index file for lists of entities
	private static final String INDEX = "index";

	// POJO for a named value pair
	public record ValuePair(String name, double value) {}

	// Represents where a Resource is used in a process
	record ResourceUse(List<ProcessInfo> asInput, List<ProcessInfo> asOutput) {}

	// A quantity of an item 
	record ItemQuantity(String name, String type, String typefolder, String amount) {}

	private static Logger logger = Logger.getLogger(HelpGenerator.class.getName());
	private static final String TEMPLATES_DIR = "templates/";
	private static final String TITLE_PREFIX = "List of ";

	/**
	 * Function that converts a string into a valid file name
	 * @return
	 */
	public Function<Object, Object> getFilename() {
		return (obj-> generateFileName((String) obj));
	}
	
	private String fileSuffix;
	private DefaultMustacheFactory mf;
	private String templateDir;
	private Map<String,Mustache> templates = new HashMap<>();
	private Map<String, Object> baseScope = null;

	private Map<String, ResourceUse> resourceUses = null;
	private SimulationConfig config;


    HelpGenerator(SimulationConfig config, String templateSet, String fileSuffix) {
        this.fileSuffix = "." + fileSuffix;
		this.templateDir = TEMPLATES_DIR + templateSet + "/";
		this.config = config;

		this.mf = new DefaultMustacheFactory();

		this.baseScope = new HashMap<>();
		this.baseScope.put("version", SimulationRuntime.VERSION.getVersion());
		this.baseScope.put("generatedOn",
						DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now()));
		
		// Locatio of other generated file; used for links
		addTypeFolder(VehicleGenerator.TYPE_NAME);
		addTypeFolder(PartGenerator.TYPE_NAME);
		addTypeFolder(ResourceGenerator.TYPE_NAME);
		addTypeFolder(ProcessGenerator.TYPE_NAME);
		addTypeFolder(FoodGenerator.TYPE_NAME);
		addTypeFolder(BuildingGenerator.TYPE_NAME);
		addTypeFolder(CrewGenerator.TYPE_NAME);
		addTypeFolder(SettlementGenerator.TYPE_NAME);
		addTypeFolder(ComplaintGenerator.TYPE_NAME);
		addTypeFolder(TreatmentGenerator.TYPE_NAME);

		// Add a lambda so a entity name can be converted into aa valid filename
		this.baseScope.put("filename", getFilename());
    }

	/**
	 * Add a variable to the base scope this point to the folder for a type of documents
	 * @param typeName
	 */
	private void addTypeFolder(String typeName) {
		this.baseScope.put(typeName + "folder", "../" + typeName + "/");
	}
	/**
	 * Generate a valid file name for a name of a entity
	 * @param name Name of config item
	 * @return
	 */
	String generateFileName(String name) {
		String simpleName = name.toLowerCase().replaceAll("\\W", "-");
		return simpleName + fileSuffix;
	}

	/**
	 * Create the default scope properties for the generation.
	 * @param pageTitle Title of page
	 * @return
	 */
	Map<String,Object> createScopeMap(String pageTitle) {
		Map<String,Object> scope = new HashMap<>(baseScope);

		// Title of the file being created
		scope.put("title", pageTitle);

		return scope;
	}

	/**
	 * Create an index page for a set of named entities. This will use the 
	 * 'entity-list' template.
	 * @param title Page title
	 * @param description Description of the page
	 * @param entities List of entities
	 * @param typeFolder Folder for this type
	 * @param outputDir Target root folder for the file
	 * 
	 */
	 void createFlatIndex(String title, String description,
			List<? extends Object> entities, String typeFolder, File outputDir) 
		throws IOException {
		var scope = createScopeMap(TITLE_PREFIX + title);
		scope.put("description", description);
		scope.put("entities", entities);
		scope.put("typefolder", "../" + typeFolder + "/");

		File indexFile = new File(outputDir, generateFileName(INDEX));
		try (FileOutputStream dest = new FileOutputStream(indexFile)) {
			generateContent("entity-list", scope, dest);
		}
	}

	/**
	 * Create an index page for a set of named entities that are grouped. This will use the 
	 * 'entity-grouped' template.
	 * @param title Page title
	 * @param description Description of the page
	 * @param groupName Name of the groups
	 * @param groups List of groups
	 * @param typeFolder Folder for this type
	 * @param outputDir Target root folder for the file
	 * 
	 */
	 void createGroupedIndex(String title, String description,
			String groupName, List<?> groups,  String typeFolder, File outputDir) 
		throws IOException {
		var scope = createScopeMap(TITLE_PREFIX + title);
		scope.put("description", description);
		scope.put("groups", groups);
		scope.put("groupname", groupName);

		scope.put("typefolder", "../" + typeFolder + "/");

		File indexFile = new File(outputDir, generateFileName(INDEX));
		try (FileOutputStream dest = new FileOutputStream(indexFile)) {
			generateContent("entity-grouped", scope, dest);
		}
	}

	static ResourceUse buildEmptyResourceUse() {
		return new ResourceUse(new ArrayList<>(), new ArrayList<>());
	}

	/**
	 * Get the usage of a Resoruce by it's name. This will return where is an inout or output
	 * to a process.
	 */
	ResourceUse getResourceUsageByName(String name) {
		if (resourceUses == null) {
			resourceUses = new HashMap<>();
			for (var m: config.getManufactureConfiguration().getManufactureProcessList()) {
				for (var r: m.getInputNames()) {
					resourceUses.computeIfAbsent(r.toLowerCase(), k-> buildEmptyResourceUse()).asInput().add(m);
				}
				for (var r: m.getOutputNames()) {
					resourceUses.computeIfAbsent(r.toLowerCase(), k-> buildEmptyResourceUse()).asOutput().add(m);
				}
			}
		}

		return resourceUses.get(name.toLowerCase());
	}

	/**
	 * Factor method to create a ProcessItem from a set of attributes
	 * @param name
	 * @param type
	 * @param amount
	 * @return
	 */
	static ItemQuantity createItemQuantity(String name, ItemType type, double amount) {
		String typeFolder = switch(type) {
			case AMOUNT_RESOURCE -> ResourceGenerator.TYPE_NAME;
			case BIN -> null;
			case EQUIPMENT -> null;
			case PART -> PartGenerator.TYPE_NAME;
			case VEHICLE -> VehicleGenerator.TYPE_NAME;
		};

		String amountTxt = (type == ItemType.AMOUNT_RESOURCE ? amount + " kg"
													: Integer.toString((int) amount));
		return new ItemQuantity(name, type.getName(), "../" + typeFolder + "/", amountTxt);
	}

	/**
	 * Generate all configurations.
	 * @param outputDir Root location for generated files
	 * @throws IOException
	 */
	public void generateAll(File outputDir) throws IOException {
		List<TypeGenerator<? extends Object>> gens = new ArrayList<>();

		gens.add(new FoodGenerator(this));
		gens.add(new PartGenerator(this));
		gens.add(new ProcessGenerator(this));
		gens.add(new ResourceGenerator(this));
		gens.add(new VehicleGenerator(this));
		gens.add(new BuildingGenerator(this));
		gens.add(new SettlementGenerator(this));
		gens.add(new CrewGenerator(this));
		gens.add(new ScenarioGenerator(this));
		gens.add(new ComplaintGenerator(this));
		gens.add(new TreatmentGenerator(this));


		// Generate all subtype
		for(var g : gens) {
			g.generateAll(outputDir);
		}

		Map<String,Object> topScope = createScopeMap("Configurations");
		topScope.put("generators", gens);

		// Generate configuration overview page
		try (FileOutputStream topIndex = new FileOutputStream(new File(outputDir,
														generateFileName(INDEX)))) {
			generateContent("top-list", topScope, topIndex);
		}
	}

    public SimulationConfig getConfig() {
        return config;
    }

	/**
	 * Look up a template. This will select the appropriate Mustache templaet for the defined
	 * template set.
	 * 
	 * @param template
	 * @return
	 */
	Mustache getTemplate(String template) {
		return templates.computeIfAbsent(template,
				t -> mf.compile(templateDir + t + ".mustache"));
	}

	/**
	 * Creates a final output by applying a template to a scope.

	 * @param templateName Name of the template to apply. Expanded into file by usign template set.
	 * @param scope Scope of properties to apply
	 * @param output Destination for generated content
	 * @throws IOException
	 */
	void generateContent(String templateName, Map<String, Object> scope, OutputStream output)
				throws IOException {
		var template = getTemplate(templateName);
		var writer = new OutputStreamWriter(output);
        template.execute(writer, scope);
        writer.flush();
	}

	/**
	 * Get a generator that which create HTML Inline helppages
	 * @param source 
	 */
	public static HelpGenerator createHTMLInline(SimulationConfig source) {
		return new HelpGenerator(source, "html-help", "html");
	}

	/**
	 * The main starting method for generating html files.
	 *
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		// Load config files
		var config = SimulationConfig.instance();
		config.loadConfig();

		var gen = createHTMLInline(config);

		try {
			File output = new File(args[0]);
			gen.generateAll(output);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Problem generating files", e);
		}
	}
}
