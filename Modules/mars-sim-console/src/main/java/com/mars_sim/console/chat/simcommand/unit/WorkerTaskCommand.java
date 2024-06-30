/*
 * Mars Simulation Project
 * WorkerTaskCommand.java
 * @date 2022-06-24
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand.unit;

import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.simcommand.StructuredResponse;
import com.mars_sim.core.Unit;
import com.mars_sim.core.person.ai.task.util.PendingTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskManager;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.person.ai.task.util.Worker;

/** 
 * The command that outputs the task stack and the pending tasks.
 */
public class WorkerTaskCommand extends AbstractUnitCommand {
	
	public WorkerTaskCommand(String group) {
		super(group, "ta", "task", "About my task stack and pending tasks");
	}

	@Override
	public boolean execute(Conversation context, String input, Unit source) {
		TaskManager mgr = null;
		if (source instanceof Worker w) {
			mgr = w.getTaskManager();
		}
		else {
			context.println("Unit " + source.getName() + " is not a Worker.");
			return false;
		}

		StructuredResponse response = new StructuredResponse();
		response.appendBlankLine();
		response.appendHeading("Task stack");
		StringBuilder prefix = new StringBuilder();
		// Task should come off person
		Task task = mgr.getTask();
		while(task != null) {
			TaskPhase phase = task.getPhase();
			
			StringBuilder sb = new StringBuilder();
			sb.append(prefix + " ");
			sb.append(task.getDescription(false));
			
			if (phase != null) {
				sb.append(" (");
				sb.append(phase.getName());
				sb.append(")");
			}
			response.append(sb.toString());
			response.appendBlankLine();
				
			task = task.getSubTask();
			if ((task != null) && task.isDone()) {
				// If the Task is done why has it not been removed ????
				task = null;
			}
			prefix.append("->");
		}
		
		// Add pending tasks
		var pending = mgr.getPendingTasks();
		if (!pending.isEmpty()) {
			response.appendBlankLine();
			response.appendHeading("Pending tasks");
			for (PendingTask p : pending) {
				response.appendText(" " + p.job().getName() + " @ " + p.when().getTruncatedDateTimeStamp());
			}
		}

		context.println(response.getOutput());
		return true;
	}
}
