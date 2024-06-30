/*
 * Mars Simulation Project
 * HelpCommand.java
 * @date 2022-06-20
 * @author Barry Evans
 */

package com.mars_sim.console.chat.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.command.InteractiveChatCommand.ParseResult;

/**
 * Generic help command that will list the commands that are supported.
 */
public class HelpCommand extends ChatCommand {

	private static final String CMD =  " * "  + InteractiveChatCommand.SHORT_PREFIX + "%-2s";
	private static final String FULL = CMD + " %s - %s";
	private static final String SHORT_ONE = CMD + " %s";
	private static final String SHORT_TWO = CMD + " %-18s " + CMD + " %s";
	
	// The long command 
	public static final String HELP_LONG = "help";
	private boolean wideFormat = true;

	public HelpCommand(boolean wide) {
		super(COMMAND_GROUP, "?", HELP_LONG, "*** Lists the available commands ***");
		
		wideFormat = wide;
	}

	@Override
	public List<String> getArguments(Conversation context) {
		return context.getCurrentCommand().getSubCommands().stream()
				.map(ChatCommand::getLongCommand).collect(Collectors.toList());
	}
	
	@Override
	public boolean execute(Conversation context, String input) {
		context.println("");
		
		boolean outputAllHelp = true;
		
		if (input != null) {
			ParseResult found = context.getCurrentCommand().parseInput(context, input);
			if (found.command != null) {
				context.println("Format: * <short command> <long command> - <description>");
				outputFullHelp(found.command, context);
				outputAllHelp = false;
			}
			else {
				context.println("Don't know command " + input);
			}
		}
		
		if (outputAllHelp) {
			context.println("Control Keys :");
			context.println(String.format(" * %s - Auto completes the input; for commands (long & short) and arguments",
										  Conversation.AUTO_COMPLETE_KEY)); 
			context.println(String.format(" * %s - Goes back in the history", Conversation.HISTORY_BACK_KEY)); 
			context.println(String.format(" * %s - Goes forward in the history", Conversation.HISTORY_FORWARD_KEY)); 
	
			context.println("");
			context.println("Console Commands (* <short command> <long command>)");
			InteractiveChatCommand current = context.getCurrentCommand();
			Map<String, List<ChatCommand>> commandGroups = current.getSubCommands().stream()
	                .collect(Collectors.groupingBy(ChatCommand::getCommandGroup));
	
			List<String> groupNames = new ArrayList<>(commandGroups.keySet());
			Collections.sort(groupNames);
			
			// Display according to group
			for (String group : groupNames) {
				List<ChatCommand> subs = commandGroups.get(group);
				outputGroup(context, group, subs, wideFormat);
			}
			
			if (wideFormat) {
				context.println("More detailed help by using > " + HELP_LONG + " <long command>");
			}
		}
		return true;
	}

	private static void outputGroup(Conversation context, String group, List<ChatCommand> subs, boolean useTwoColumns) {
		context.println("Group : " + group);
		Collections.sort(subs, (c1, c2) -> c1.getShortCommand().compareTo(c2.getShortCommand()));

		if (useTwoColumns) {
			// Two column and short format
			int half = (subs.size()+1)/2;
			for(int i = 0; i < half; i++) {
				ChatCommand first = subs.get(i);
				int j = i + half;
				if (j < subs.size()) {
					ChatCommand second = subs.get(j);
					context.println(String.format(SHORT_TWO, 
							first.getShortCommand(), first.getLongCommand(),
							second.getShortCommand(), second.getLongCommand()));
				}
				else {
					context.println(String.format(SHORT_ONE,
							first.getShortCommand(), first.getLongCommand()));			
				}
			}
		}
		else {
			for (ChatCommand chatCommand : subs) {
				outputFullHelp(chatCommand, context);
			}
		}
		context.println("");
	}

	/**
	 * Output the full details of a command.
	 * @param chatCommand
	 * @param context
	 */
	private static void outputFullHelp(ChatCommand chatCommand, Conversation context) {
		context.println(String.format(FULL,
				chatCommand.getShortCommand(),
				chatCommand.getLongCommand(), chatCommand.getDescription()));
	}
}
