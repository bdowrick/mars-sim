/*
 * Mars Simulation Project
 * ChatCommand.java
 * @date 2022-06-20
 * @author Barry Evans
 */

package com.mars_sim.console.chat;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class ChatCommand {

	public static final String COMMAND_GROUP = "Common";

	private String shortCommand;
	private String longCommand;
	private String commandGroup;
	private String description;
	private String introduction = null;
	private List<String> arguments = null;
	private Set<ConversationRole> roles = new HashSet<>();

	private boolean interactive = false;

	protected ChatCommand(String commandGroup, String shortCommand, String longCommand, String description) {
		super();
		this.commandGroup = commandGroup;
		this.shortCommand = shortCommand;
		this.longCommand = longCommand;
		this.description = description;
	}

	/**
	 * This processes input from a user.
	 * 
	 * @param context
	 * @param input 
	 * @return Has the input been accepted and understood
	 */
	public abstract boolean execute(Conversation context, String input);

	/**
	 * What is the short command?
	 * 
	 * @return
	 */
	public String getShortCommand() {
		return shortCommand;
	}
	
	/**
	 * The keyword that triggers the execution of this command.
	 * 
	 * @return
	 */
	public String getLongCommand() {
		return longCommand;
	}

	/**
	 * A description about the command; used for help.
	 * 
	 * @return
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * The command group this is held in.
	 * 
	 * @return
	 */
	public String getCommandGroup() {
		return commandGroup;
	}

	/**
	 * Returns any introduction to executing this command when working as interactive.
	 * Assuming that the contents may be dynamic as a subclass may override this method.
	 * 
	 * @return
	 */
	public String getIntroduction() {
		return introduction ;
	}

	/**
	 * Sets a default introduction.
	 * 
	 * @param introduction
	 */
	protected void setIntroduction(String introduction) {
		this.introduction = introduction;
	}

	/**
	 * Any fix arguments for the command. These are used in the AutoComplete operation.
	 * This should be Overridden by subclasses if they what to return a dynamic list of options. 
	 * A static list of options can be specified in {@link #setArguments(List)}
	 * 
	 * @param context Current context
	 * @return
	 */
	protected List<String> getArguments(Conversation context) {
		return arguments;
	}

	/**
	 * Sets and fixes arguments to this command. Dynamic arguments the {@link #getAutoComplete(Conversation, String)}
	 * method should be overridden.
	 * 
	 * @param arguments
	 */
	protected void setArguments(List<String> arguments) {
		this.arguments = arguments;
	}

	/**
	 * Gets the list of options that match the partial input. 
	 * 
	 * @param partialInput
	 * @return List of potential full commands or maybe null.
	 */
	public List<String> getAutoComplete(Conversation context, String parameter) {
		List<String> result;
		List<String> activeArgs = getArguments(context);
		if (activeArgs != null) {
			if (parameter != null) {
				String match = parameter.toLowerCase();
				result = activeArgs.stream()
						.filter(n -> n.toLowerCase().startsWith(match))
						.collect(Collectors.toList());
			}
			else {
				result = activeArgs;
			}
		}
		else {
			result = Collections.emptyList();
		}
		
		return result;
	}
	
	/**
	 * Is this command interactive and requesting user input?
	 * 
	 * @return
	 */
	public boolean isInteractive() {
		return interactive;
	}
	
	/**
	 * Sets this command interactive.
	 * 
	 * @param b
	 */
	protected void setInteractive(boolean b) {
		this.interactive  = b;
		
	}
	
	/**
	 * User must should have at least one of these roles to use this Command.
	 * 
	 * @return
	 */
	public Set<ConversationRole> getRequiredRoles() {
		return roles;
	}


	protected void addRequiredRole(ConversationRole newRole) {
		roles.add(newRole);
	}

	@Override
	public String toString() {
		return "ChatCommand [keyword=" + longCommand + "]";
	}
}