/*
 * Mars Simulation Project
 * SocialCommand.java
 * @date 2022-06-11
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand.person;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.simcommand.CommandHelper;
import com.mars_sim.console.chat.simcommand.StructuredResponse;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.social.RelationshipUtil;

/** 
 * Social circle of the Person
 */
public class SocialCommand extends AbstractPersonCommand {
	public static final ChatCommand SOCIAL = new SocialCommand();
	
	private SocialCommand() {
		super("so", "social", "About my social circle");
	}

	@Override
	public boolean execute(Conversation context, String input, Person person) {

		// My opinions of them
		Map<Person, Double> friends = RelationshipUtil.getMyOpinionsOfThem(person);
		if (friends.isEmpty()) {
			context.println("I don't have any friends yet.");
		}
		else {
			StructuredResponse response = new StructuredResponse();
			
			response.appendHeading("My Opinion of them");
			List<Person> list = new ArrayList<>(friends.keySet());
			
			response.appendTableHeading("Toward this Person", CommandHelper.PERSON_WIDTH, "Score", 6, "My Attitude");

			double sum = 0;
			for (Person friend : list) {
				double score = friends.get(friend);
				sum += score;
				String relation = RelationshipUtil.describeRelationship(score);

				score = Math.round(score * 10.0) / 10.0;

				response.appendTableRow(friend.getName(), score, relation);
			}
			response.appendLabeledString("Ave. option of them", "" + sum/list.size());
			response.appendBlankLine();
			
			context.println(response.getOutput());
		}
		
		return true;
	}
}
