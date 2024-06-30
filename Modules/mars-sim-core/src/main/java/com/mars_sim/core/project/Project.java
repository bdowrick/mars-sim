/**
 * Mars Simulation Project
 * Project.java
 * @date 2023-05-05
 * @author Barry Evans
 */
package com.mars_sim.core.project;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.ai.task.util.Worker;

/**
 * Represents a project that has a number of steps
 */
public class Project implements Serializable {
    private static final long serialVersionUID = 1L;

	private static final SimLogger logger = SimLogger.getLogger(Project.class.getName());

    private String name;
    private List<ProjectStep> steps = new ArrayList<>();
    private int currentStepIdx = -1; // Hold the index to make it easier
    private ProjectStep currentStep = null;
    private boolean isDone = false;
    private boolean isAborted = false;

    public Project(String name) {
        this.name = name;
    }

    /**
     * Get the name of the Project
     * @return
     */
    public String getName() {
        return name;
    }

    
    /**
     * Chnage the name of the project
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the current stage
     * @return
     */
    public Stage getStage() {
        Stage stage = Stage.WAITING;
        if (isDone) {
            stage = Stage.DONE;
        }
        else if (isAborted) {
            stage = Stage.ABORTED;
        }
        else if (currentStep != null) {
            stage = currentStep.getStage();
        }
        return stage;
    }

    /**
     * A worker executes the project
     * @param worker
     * @return 
     */
    public boolean execute(Worker worker) {
        if (currentStep == null) {
            advanceStep();
        }
        return currentStep.execute(worker);
    }

    /**
     * Move on the the next step
     */
    void advanceStep() {
        currentStepIdx++;
        
        if (currentStepIdx >= steps.size()) {
            isDone = true;
            completed(true);
        }
        else {
            currentStep = steps.get(currentStepIdx);
            logger.info(getName() + " started step " + currentStep);
            currentStep.start();
            stepStarted(currentStep);
        }
    }

    /**
     * Callback that this project has completed
     * @param successful
     */
    protected void completed(boolean successful) {
    }

    /**
     * Notification that a step has been started. This can be override for notification
     * @param activeStep Step started
     */
    protected void stepStarted(ProjectStep activeStep) {
    }

    /**
     * Notification that a step has been completed. This can be override for notification
     * @param completedStep Step completed
     */
    protected void stepCompleted(ProjectStep completedStep) {
    }

    /**
     * Abort the project
     */
    public void abort(String reason) {
        isAborted = true;

        if ((currentStep != null) && !currentStep.isCompleted()) {
            currentStep.complete();
        }
        completed(false);
    }

    /** 
     * Add a step to this project
     * @param step
     * @see ProjectStep#setParent(Project)
     */
    public void addStep(ProjectStep step) {
        // Check the Stage is now regressing
        if (!steps.isEmpty()) {
            int lastStage = steps.get(steps.size()-1).getStage().ordinal();
            if (lastStage > step.getStage().ordinal()) {
                throw new IllegalArgumentException("The step can not move the Stage backwards" + step.getStage());
            }
        }

        steps.add(step);
        step.setParent(this);
    }

    /**
     * Remove a registered step. Can not be a step already executed
     * @param oldStep
     * @return Returns false is the step has already been executed
     */
    public boolean removeStep(ProjectStep oldStep) {
        int oldIdx = steps.indexOf(oldStep);
        if (oldIdx >= 0 && (currentStep != null)) {
            int currentIdx = steps.indexOf(currentStep);
            if (oldIdx <= currentIdx) {
                return false;
            }
        }

        return steps.remove(oldStep);
    }

    /**
     * What is active currently
     * @return
     */
    public ProjectStep getStep() {
        return currentStep;
    }

    /**
     * Is the project finished; either completed ot aborted
     * @return Finsihed
     */
    public boolean isFinished() {
        return (isDone || isAborted);
    }

    /**
     * Get the remaining steps of the project
     * @return
     */
    public List<ProjectStep> getRemainingSteps() {
        int start = (currentStepIdx < 0 ? 0 : currentStepIdx);
        return steps.subList(start, steps.size());
    }

    /**
     * A step has completed
     * @param completedStep
     */
    void completeStep(ProjectStep completedStep) {
        stepCompleted(completedStep); // Notify

        // Check not abnormal shutdown of the project
        if (!isAborted) {
            advanceStep();
        }
    }

    /**
     * Abort the current step. Mark it as complete and advance
     */
    public void abortStep() {
       currentStep.complete(); 
    }
}
