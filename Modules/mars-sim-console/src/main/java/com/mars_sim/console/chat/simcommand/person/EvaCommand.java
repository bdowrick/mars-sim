/*
 * Mars Simulation Project
 * EvaCommand.java
 * @date 2022-08-24
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand.person;

import java.util.Map;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.simcommand.StructuredResponse;
import com.mars_sim.core.person.Person;

/** 
 * 
 */
public class EvaCommand extends AbstractPersonCommand {
	public static final ChatCommand EVA = new EvaCommand();
	
	private EvaCommand() {
		super("e", "eva", "EVA time");
	}

	@Override
	public boolean execute(Conversation context, String input, Person person) {
		StructuredResponse response = new StructuredResponse();
		
		response.appendTableHeading("Sol", 5, "Millisols");

		Map<Integer, Double> eVATime = person.getTotalEVATaskTimeBySol();
		int currentDay = context.getSim().getMasterClock().getMarsTime().getMissionSol();
		for (int i = (currentDay - eVATime.size()); i <= currentDay; i++) {
			if (eVATime.containsKey(i)) {
				double milliSol = eVATime.get(i);
				milliSol = Math.round(milliSol * 10.0) / 10.0;
				response.appendTableRow("" + i, milliSol);
			}
		}
		
		context.println(response.getOutput());
		return true;
	}

}
