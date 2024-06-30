/*
 * Mars Simulation Project
 * SaveCommand.java
 * @date 2022-07-28
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.ConversationRole;

public class SaveCommand extends ChatCommand {

	private String status = null;

	public SaveCommand() {
		super(TopLevel.SIMULATION_GROUP, "sv", "save", "Save the simulation");
		setInteractive(true);
		addRequiredRole(ConversationRole.ADMIN);
	}


	@Override
	public boolean execute(Conversation context, String input) {
		String toSave = context.getInput("Save simulation (Y/N)?");
	
        if ("Y".equalsIgnoreCase(toSave)) {
            context.println("Saving Simulation...");

			status = "NotCompleted";
			CompletableFuture<Boolean> lock = new CompletableFuture<>();
			context.getSim().requestSave(null, action -> {
				status = action;
				lock.complete(true);
			});

			// Print the size of all serialized objects
			context.println("");
			context.println("Use byte arrays to show the heap size of serialized objects");
			context.println("");
			context.println(context.getSim().printObjectSize(1).toString());
			
			// Wait for the save to complete
			try {
				lock.get(20, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				context.println("Problem completing the save wait: " + e);
				Thread.currentThread().interrupt();
				return false;
			} catch (ExecutionException | TimeoutException e) {
				context.println("Problem executing the save wait: " + e);
				return false;
			}

			context.println("Done Saving. : " + status);
			context.println("");			
        }

		
		return true;
	}
}
