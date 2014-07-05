package org.optaplanner.examples.icon.solver.score;

import org.optaplanner.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import org.optaplanner.core.impl.score.director.incremental.IncrementalScoreCalculator;
import org.optaplanner.examples.icon.domain.Machine;
import org.optaplanner.examples.icon.domain.Period;
import org.optaplanner.examples.icon.domain.Schedule;
import org.optaplanner.examples.icon.domain.TaskAssignment;

public class IconIncrementalScoreCalculator implements IncrementalScoreCalculator<Schedule> {

    private MachineCostTracker costsOfRunningMachines;
    private TaskCostTracker costsOfRunningTasks;

    private Machine previousExecutor = null;

    private Period previousFinalPeriod = null;
    private Period previousStartPeriod = null;

    private CapacityTracker resourceConsumption;

    @Override
    public void afterEntityAdded(final Object entity) {
        this.insert((TaskAssignment) entity);
    }

    @Override
    public void afterEntityRemoved(final Object entity) {
    }

    @Override
    public void afterVariableChanged(final Object entity, final String variableName) {
        final TaskAssignment ta = (TaskAssignment) entity;
        // this logic emulates a modify() instead of a full retract/insert
        final boolean canBeRemoved = this.previousExecutor != null && this.previousStartPeriod != null;
        final boolean canBeInserted = ta.getExecutor() != null && ta.getStartPeriod() != null;
        if (canBeRemoved) {
            if (canBeInserted) {
                final boolean modifyingExecutor = this.previousExecutor != ta.getExecutor();
                final boolean modifyingStartPeriod = this.previousStartPeriod != ta.getStartPeriod();
                if (modifyingExecutor) {
                    // everything changes with changing the executor
                    this.retractWithSimulation(ta);
                    this.insert(ta);
                } else if (modifyingStartPeriod) {
                    // only some periods will change
                    this.costsOfRunningTasks.modify(ta);
                    this.costsOfRunningMachines.modify(ta, this.previousStartPeriod, this.previousFinalPeriod);
                    this.resourceConsumption.modify(ta, this.previousStartPeriod, this.previousFinalPeriod);
                }
            } else {
                this.retractWithSimulation(ta);
            }
        } else {
            if (canBeInserted) {
                this.insert(ta);
            }
        }
    }

    @Override
    public void beforeEntityAdded(final Object entity) {
    }

    @Override
    public void beforeEntityRemoved(final Object entity) {
        this.retract((TaskAssignment) entity);
    }

    @Override
    public void beforeVariableChanged(final Object entity, final String variableName) {
        final TaskAssignment ta = (TaskAssignment) entity;
        this.previousExecutor = ta.getExecutor();
        this.previousStartPeriod = ta.getStartPeriod();
        this.previousFinalPeriod = ta.getFinalPeriod();
    }

    @Override
    public String buildScoreCorruptionAnalysis(final IncrementalScoreCalculator uncorruptedIncrementalScoreCalculator) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public HardSoftLongScore calculateScore() {
        final long hardScore = this.resourceConsumption.getOverusedCapacity();
        final long softScore = this.costsOfRunningTasks.getCost() + this.costsOfRunningMachines.getCost();
        return HardSoftLongScore.valueOf(-hardScore, -softScore);
    }

    private boolean insert(final TaskAssignment entity) {
        if (!entity.isInitialized()) {
            return false;
        }
        this.resourceConsumption.add(entity);
        this.costsOfRunningMachines.add(entity);
        this.costsOfRunningTasks.add(entity);
        return true;
    }

    @Override
    public void resetWorkingSolution(final Schedule workingSolution) {
        this.costsOfRunningTasks = new TaskCostTracker(workingSolution);
        this.costsOfRunningMachines = new MachineCostTracker(workingSolution);
        this.resourceConsumption = new CapacityTracker(workingSolution);
        for (final TaskAssignment ta : workingSolution.getTaskAssignments()) {
            this.insert(ta);
        }
    }

    private boolean retract(final TaskAssignment entity) {
        if (!entity.isInitialized()) {
            return false;
        }
        this.resourceConsumption.remove(entity);
        this.costsOfRunningMachines.remove(entity);
        this.costsOfRunningTasks.remove(entity);
        return true;
    }

    private void retractWithSimulation(final TaskAssignment ta) {
        final Machine tmpExecutor = ta.getExecutor();
        final Period tmpPeriod = ta.getStartPeriod();
        ta.setExecutor(this.previousExecutor); // need to set previous values to safely remove
        ta.setStartPeriod(this.previousStartPeriod);
        this.retract(ta);
        ta.setExecutor(tmpExecutor); // and return the originals back
        ta.setStartPeriod(tmpPeriod);
    }

}
