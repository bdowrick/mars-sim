/*
 * Mars Simulation Project
 * ChainOfCommand.java
 * @date 2023-11-15
 * @author Manny Kung
 */

package com.mars_sim.core.structure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.person.Commander;
import com.mars_sim.core.person.GenderType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.PersonConfig;
import com.mars_sim.core.person.ai.NaturalAttributeManager;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillManager;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.job.util.JobUtil;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.person.ai.role.RoleUtil;

/**
 * The ChainOfCommand class creates and assigns a person a role type based on
 * one's job type and the size of the settlement
 */
public class ChainOfCommand implements Serializable {
	/** default serial id. */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(ChainOfCommand.class.getName());

	public static final int POPULATION_WITH_COMMANDER = 4;
	public static final int POPULATION_WITH_SUB_COMMANDER = 10;
	public static final int POPULATION_WITH_CHIEFS = 20;
	public static final int POPULATION_WITH_ADMINISTRATOR = 50;
	public static final int POPULATION_WITH_MAYOR = 100;
	public static final int POPULATION_WITH_PRESIDENT = 1000;
	
	
	/** Stores the number for each role. */
	private Map<RoleType, Integer> roleRegistry;
	/** Store the availability of each role. */
	private Map<RoleType, Integer> roleAvailability;
	/** The settlement of interest. */
	private Settlement settlement;


	/**
	 * This class creates a chain of command structure for a settlement. A
	 * settlement can have either 3 divisions or 7 divisions organizational
	 * structure
	 *
	 * @param settlement {@link Settlement}
	 */
	public ChainOfCommand(Settlement settlement) {
		this.settlement = settlement;

		roleRegistry = new ConcurrentHashMap<>();
		roleAvailability = new ConcurrentHashMap<>();

		// Initialize roleAvailability array once only
		if (roleAvailability.isEmpty())
			initializeRoleMaps();
	}

	/**
	 * Initializes the role maps.
	 */
	public void initializeRoleMaps() {
		int pop = settlement.getInitialPopulation();

		List<RoleType> roles = null;

		if (pop > POPULATION_WITH_COMMANDER) {
			roles = new ArrayList<>(RoleUtil.getSpecialists());
		}
		else {
			roles = new ArrayList<>(RoleUtil.getCrewRoles());
		}
		
		// Shuffle the role types randomize
		Collections.shuffle(roles);

		for (RoleType t : roles) {
			if (!roleAvailability.containsKey(t)) {
				roleAvailability.put(t, 0);
			}

			if (!roleRegistry.containsKey(t))
				roleRegistry.put(t, 0);
		}

		for (int i=0; i < pop; i++) {
			RoleType rt = roles.get(i % roles.size());
			int num = roleAvailability.get(rt);
			roleAvailability.put(rt, num+1);
		}
	}

	/**
	 * Is the given role type available ?
	 *
	 * @param type
	 * @return
	 */
	public boolean isRoleAvailable(RoleType type) {
        return roleAvailability.get(type) > roleRegistry.get(type);
	}

	/**
	 * Gets the role availability map.
	 *
	 * @return
	 */
	public Map<RoleType, Integer> getRoleAvailability() {
		return roleAvailability;
	}

	/**
	 * Increments the number of the target role type in the map.
	 *
	 * @param key {@link RoleType}
	 */
	public void registerRole(RoleType key) {
		int value = getNumFilled(key);
		roleRegistry.put(key, value + 1);
	}

	/**
	 * Decrements the number of the target role type from the map.
	 *
	 * @param key {@link RoleType}
	 */
	public void releaseRole(RoleType key) {
		int value = getNumFilled(key);
		if (value <= 0)
			roleRegistry.put(key, value - 1);
	}

	/**
	 * Elects a new person for leadership in a settlement if a mayor, commander,
	 * sub-commander, or chiefs vacates his/her position.
	 *
	 * @param key {@link RoleType}
	 */
	public void reelectLeadership(RoleType key) {
		if (getNumFilled(key) == 0) {

			int popSize = settlement.getNumCitizens();

			if (popSize >= POPULATION_WITH_MAYOR) {
				if (key.isChief()) {
					electChief(key);
				} else if (key == RoleType.COMMANDER || key == RoleType.SUB_COMMANDER) {
					int pop = settlement.getNumCitizens();
					electCommanders(pop);
				} else if (key == RoleType.MAYOR) {
					electMayor(key);
				}
			}

			else if (popSize >= POPULATION_WITH_CHIEFS) {
				if (key.isChief()) {
					electChief(key);
				} else if (key == RoleType.COMMANDER || key == RoleType.SUB_COMMANDER) {
					int pop = settlement.getNumCitizens();
					electCommanders(pop);
				}
			}

			else if (popSize >= POPULATION_WITH_SUB_COMMANDER) {
				if (key.isChief()) {
					electChief(key);
				} else if (key == RoleType.COMMANDER || key == RoleType.SUB_COMMANDER) {
					int pop = settlement.getNumCitizens();
					electCommanders(pop);
				}
			}

			else {
				if (key == RoleType.COMMANDER || key == RoleType.SUB_COMMANDER) {
					int pop = settlement.getNumCitizens();
					electCommanders(pop);
				}
			}
		}
	}

	/**
	 * Finds out the number of people already fill this roleType.
	 *
	 * @param key
	 */
	public int getNumFilled(RoleType key) {
		int value = 0;
		if (roleRegistry.containsKey(key))
			value = roleRegistry.get(key);
		return value;
	}

	/**
	 * Elects the commanders.
	 *
	 * @param pop
	 */
	private void electCommanders(int pop) {
		Collection<Person> people = settlement.getAllAssociatedPeople();
		Person cc = null;
		int cc_leadership = 0;
		int cc_combined = 0;

		Person cv = null;
		int cv_leadership = 0;
		int cv_combined = 0;
		// compare their leadership scores
		for (Person p : people) {
			NaturalAttributeManager mgr = p.getNaturalAttributeManager();
			int p_leadership = mgr.getAttribute(NaturalAttributeType.LEADERSHIP);
			int p_combined = 3 * mgr.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE)
					+ 2 * mgr.getAttribute(NaturalAttributeType.EMOTIONAL_STABILITY)
					+ mgr.getAttribute(NaturalAttributeType.ATTRACTIVENESS)
					+ mgr.getAttribute(NaturalAttributeType.CONVERSATION);
			// if this person p has a higher leadership score than the previous
			// cc
			if (p_leadership > cc_leadership) {
				if (pop >= POPULATION_WITH_SUB_COMMANDER) {
					cv_leadership = cc_leadership;
					cv = cc;
					cv_combined = cc_combined;
				}
				cc = p;
				cc_leadership = p_leadership;
				cc_combined = p_combined;
			}
			// if this person p has the same leadership score as the previous cc
			else if (p_leadership == cc_leadership) {
				// if this person p has a higher combined score than the
				// previous cc
				if (p_combined > cc_combined) {
					// this person becomes the cc
					if (pop >= POPULATION_WITH_SUB_COMMANDER) {
						cv = cc;
						cv_leadership = cc_leadership;
						cv_combined = cc_combined;
					}
					cc = p;
					cc_leadership = p_leadership;
					cc_combined = p_combined;
				}

			} else if (pop >= POPULATION_WITH_SUB_COMMANDER) {

				if (p_leadership > cv_leadership) {
					// this person p becomes the sub-commander
					cv = p;
					cv_leadership = p_leadership;
					cv_combined = p_combined;
				} else if (p_leadership == cv_leadership) {
					// compare person p's combined score with the cv's combined
					// score
					if (p_combined > cv_combined) {
						cv = p;
						cv_leadership = p_leadership;
						cv_combined = p_combined;
					}
				}
			}
		}
		// Note: look at other attributes and/or skills when comparing
		// individuals

		// Check if this settlement is the designated settlement for
		// housing the player commander
		if (cc != null) {
			if (settlement.hasDesignatedCommander()) {
				PersonConfig personConfig = SimulationConfig.instance().getPersonConfig();
				Commander commander = personConfig.getCommander();
				updateCommander(cc, commander);
				cc.setAssociatedSettlement(settlement.getIdentifier());
				logger.config("[" + cc.getLocationTag().getLocale() + "] " + cc
						+ " had been assigned as the commander of " + settlement + ".");

				// Determine the initial leadership points
				int leadershipAptitude = cc.getNaturalAttributeManager().getAttribute(NaturalAttributeType.LEADERSHIP);
				commander.setInitialLeadershipPoint(leadershipAptitude);
			}

			else {
				cc.setRole(RoleType.COMMANDER);
			}
		}

		if (cv != null && pop >= POPULATION_WITH_SUB_COMMANDER) {
			cv.setRole(RoleType.SUB_COMMANDER);
		}
	}

	/**
	 * Applies the Commander profile to the Person with the COMMANDER role.
	 * 
	 * @param profile
	 * @return
	 */
	public Person applyCommander(Commander profile) {
		List<Person> commanders = findPeopleWithRole(RoleType.COMMANDER);
		if (commanders.isEmpty()) {
			return null;
		}
		Person commander = commanders.get(0);
		updateCommander(commander, profile);

		logger.info("[" + settlement.getName() + "] "
						+ commander.getName() + " selected as the commander.");
		Simulation.instance().getUnitManager().setCommanderId(commander.getIdentifier());

		return commander;
	}

	/**
	 * Updates the commander's profile.
	 *
	 * @param cc the person instance
	 */
	private void updateCommander(Person cc, Commander commander) {

		// Replace the commander
		cc.setName(commander.getFullName());
		cc.setGender((commander.getGender().equalsIgnoreCase("M")
							? GenderType.MALE : GenderType.FEMALE));
		cc.changeAge(commander.getAge());
		cc.setJob(JobType.valueOf(commander.getJob().toUpperCase()), JobUtil.MISSION_CONTROL);
		logger.config(commander.getFullName() + " accepted the role of being a Commander by the order of the Mission Control.");
		cc.setRole(RoleType.COMMANDER);
		//cc.setCountry(commander.getCountryStr());
		cc.setSponsor(SimulationConfig.instance().getReportingAuthorityFactory().getItem(commander.getSponsorStr()));
	}

	/**
	 * Establishes or reset the system of governance of a settlement.
	 *
	 */
	public void establishSettlementGovernance() {
		// Elect commander, subcommander, mayor
		establishTopLeadership();

		// Assign a basic role to each person
		assignBasicRoles();

		// Elect chiefs
		establishChiefs();
	}

	/**
	 * Assigns a basic role to each person.
	 *
	 * @param settlement
	 */
	private void assignBasicRoles() {
		// Assign roles to each person
		List<Person> oldlist = new ArrayList<>(settlement.getAllAssociatedPeople());
		List<Person> plist = new ArrayList<>();

		int pop = settlement.getInitialPopulation();
		
		// If a person does not have a (specialist) role, add him/her to the list
		for (Person p: oldlist) {
			if (p.getRole().getType() == null)
				plist.add(p);
		}

		List<RoleType> roleList = null;
				
		if (pop <= ChainOfCommand.POPULATION_WITH_COMMANDER) {
			roleList = new ArrayList<>(RoleUtil.getCrewRoles());
		}
		else {
			roleList = new ArrayList<>(RoleUtil.getSpecialists());
		}
		
		while (!plist.isEmpty()) {
			// Randomly reorient the order of roleList so that the
			// roles to go in different order each time
			Collections.shuffle(roleList);

			for (RoleType r : roleList) {
				Person p = RoleUtil.findBestFit(r, plist);
				p.setRole(r);
				plist.remove(p);
				if (plist.isEmpty())
					return;
			}
		}
	}

	/**
	 * Establishes the top leadership of a settlement.
	 *
	 * @param settlement the settlement.
	 */
	private void establishTopLeadership() {

		int popSize = settlement.getNumCitizens();

		if (popSize >= POPULATION_WITH_MAYOR) {
			// Elect a mayor
			electMayor(RoleType.MAYOR);
		}
		// for pop < POPULATION_WITH_MAYOR
		else if (popSize >= POPULATION_WITH_COMMANDER) {
			// Elect commander and sub-commander
			electCommanders(popSize);
		}
	}

	/**
	 * Establishes the chiefs of a settlement.
	 *
	 * @param settlement the settlement.
	 */
	private void establishChiefs() {

		int popSize = settlement.getNumCitizens();

		if (popSize >= POPULATION_WITH_CHIEFS) {
			// Elect chiefs
			int i = 0;
			int maxChiefs = popSize - POPULATION_WITH_CHIEFS + 1;
			for(RoleType rt : RoleType.values()) {
				if (rt.isChief() && (i < maxChiefs)) {
					electChief(rt);
				}
			}
		}
	}

	/**
	 * Establishes the mayor in a settlement.
	 *
	 * @param settlement
	 * @param role
	 */
	private void electMayor(RoleType role) {
		Collection<Person> people = settlement.getAllAssociatedPeople();
		Person mayorCandidate = null;
		int m_leadership = 0;
		int m_combined = 0;
		// Compare their leadership scores
		for (Person p : people) {
			NaturalAttributeManager mgr = p.getNaturalAttributeManager();
			int p_leadership = mgr.getAttribute(NaturalAttributeType.LEADERSHIP);
			int p_tradeSkill = 5 * p.getSkillManager().getEffectiveSkillLevel(SkillType.TRADING);
			p_leadership = p_leadership + p_tradeSkill;
			int p_combined = mgr.getAttribute(NaturalAttributeType.ATTRACTIVENESS)
					+ 3 * mgr.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE)
					+ mgr.getAttribute(NaturalAttributeType.CONVERSATION);
			// if this person p has a higher leadership score than the previous
			// cc
			if (p_leadership > m_leadership) {
				m_leadership = p_leadership;
				mayorCandidate = p;
				m_combined = p_combined;
			}
			// if this person p has the same leadership score as the previous cc
			else if (p_leadership == m_leadership) {
				// if this person p has a higher combined score in those 4
				// categories than the previous cc
				if (p_combined > m_combined) {
					// this person becomes the cc
					m_leadership = p_leadership;
					mayorCandidate = p;
					m_combined = p_combined;
				}
			}
		}

		if (mayorCandidate != null) {
			mayorCandidate.setRole(RoleType.MAYOR);
		}
	}

	/**
	 * Establish the chiefs in a settlement
	 *
	 * @param settlement
	 * @param role
	 */
	private void electChief(RoleType role) {
		Collection<Person> people = settlement.getAllAssociatedPeople();

		if (!role.isChief()) {
			return;
		}

		RoleType specialty = RoleUtil.getChiefSpeciality(role);
		Person winner = null;
		int cSkills = 0;
		int cCombined = 0;

		List<SkillType> requiredSkills = null;
		switch(role) {
		case CHIEF_OF_AGRICULTURE:
			requiredSkills = List.of(
									SkillType.BOTANY,
									SkillType.BIOLOGY,
									SkillType.CHEMISTRY,
									SkillType.COOKING,
									SkillType.TRADING
									);
			break;

		case CHIEF_OF_COMPUTING:
			requiredSkills = List.of(
									SkillType.ASTRONOMY,
									SkillType.COMPUTING,
									SkillType.CHEMISTRY,
									SkillType.MATHEMATICS,
									SkillType.PHYSICS
									);
			break;

		case CHIEF_OF_ENGINEERING:
			requiredSkills = List.of(
									SkillType.MATERIALS_SCIENCE,
									SkillType.COMPUTING,
									SkillType.PHYSICS,
									SkillType.MECHANICS,
									SkillType.CONSTRUCTION
									);
			break;

		case CHIEF_OF_LOGISTICS_N_OPERATIONS:
			requiredSkills = List.of(
									SkillType.COMPUTING,
									SkillType.EVA_OPERATIONS,
									SkillType.MATHEMATICS,					
									SkillType.METEOROLOGY,
									SkillType.MECHANICS,
									SkillType.PILOTING,
									SkillType.TRADING									
									);
			break;

		case CHIEF_OF_MISSION_PLANNING:
			requiredSkills = List.of(
									SkillType.AREOLOGY,
									SkillType.COMPUTING,
									SkillType.EVA_OPERATIONS,
									SkillType.MATHEMATICS,
									SkillType.MANAGEMENT,									
									SkillType.PILOTING,									
									SkillType.PSYCHOLOGY,
									SkillType.TRADING									
									);
			break;

		case CHIEF_OF_SAFETY_N_HEALTH:
			requiredSkills = List.of(
									SkillType.AREOLOGY,
									SkillType.BIOLOGY,
									SkillType.CONSTRUCTION,
									SkillType.EVA_OPERATIONS,
									SkillType.MEDICINE,
									SkillType.PSYCHOLOGY,
									SkillType.TRADING
									);
			break;

		case CHIEF_OF_SCIENCE:
			requiredSkills = List.of(
									SkillType.AREOLOGY,
									SkillType.ASTRONOMY,
									SkillType.BIOLOGY,
									SkillType.BOTANY,									
									SkillType.CHEMISTRY,
									SkillType.COMPUTING,
									SkillType.MATERIALS_SCIENCE,
									SkillType.MATHEMATICS,
									SkillType.MEDICINE,
									SkillType.PHYSICS,
									SkillType.PSYCHOLOGY
									);
			break;

		case CHIEF_OF_SUPPLY_N_RESOURCES:
			requiredSkills = List.of(
									SkillType.COOKING,
									SkillType.MATHEMATICS,
									SkillType.MANAGEMENT,
									SkillType.MATERIALS_SCIENCE,
									SkillType.TRADING
									);
			break;

		default:
			throw new IllegalStateException("Can not process Chief " + role);
		}

		// compare their scores
		for (Person p : people) {
			SkillManager skillMgr = p.getSkillManager();
			NaturalAttributeManager mgr = p.getNaturalAttributeManager();
			if (p.getRole().getType() == specialty) {

				// Calculate the skill with a decreasing importance
				int modifier = 5 + requiredSkills.size() - 1;
				int pSkills = 0;
				for (SkillType skillType : requiredSkills) {
					pSkills += (modifier * skillMgr.getEffectiveSkillLevel(skillType));
					modifier--;
				}

				int pCombined = mgr.getAttribute(NaturalAttributeType.LEADERSHIP)
						+ mgr.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE)
						+ skillMgr.getEffectiveSkillLevel(SkillType.MANAGEMENT);
				// if this person p has a higher experience score than the
				// previous cc
				// or experience score match and the combined is higher
				if ((pSkills > cSkills)
						|| ((pSkills == cSkills) && (pCombined > cCombined))) {
					cSkills = pSkills;
					winner = p;
					cCombined = pCombined;
				}
			}
		}

		if (winner != null) {
			winner.setRole(role);
		}
	}

	/**
	 * Finds a list of people with a particular role.
	 *
	 * @param role
	 * @return List<Person>
	 */
	public List<Person> findPeopleWithRole(RoleType role) {
		List<Person> people = new ArrayList<>();
		for (Person p : settlement.getAllAssociatedPeople()) {
			if (p.getRole().getType() == role)
				people.add(p);
		}

		return people;
	}

	public void destroy() {
		roleRegistry.clear();
		roleRegistry = null;
		settlement = null;
	}
}
