package org.optaplanner.examples.icon.solver.score;

import org.optaplanner.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import org.optaplanner.core.impl.score.director.incremental.IncrementalScoreCalculator;
import org.optaplanner.examples.icon.domain.Schedule;
import org.optaplanner.examples.icon.domain.TaskAssignment;

public class IconIncrementalScoreCalculator implements IncrementalScoreCalculator<Schedule> {

    private MachineCostTracker costsOfRunningMachines;
    private TaskCostTracker costsOfRunningTasks;
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
        this.insert((TaskAssignment) entity);
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
        this.retract((TaskAssignment) entity);
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

    private void insert(final TaskAssignment entity) {
        if (!entity.isInitialized()) {
            return;
        }
        this.resourceConsumption.add(entity);
        this.costsOfRunningTasks.add(entity);
        this.costsOfRunningMachines.add(entity);
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

    private void retract(final TaskAssignment entity) {
        if (!entity.isInitialized()) {
            return;
        }
        this.resourceConsumption.remove(entity);
        this.costsOfRunningTasks.remove(entity);
        this.costsOfRunningMachines.remove(entity);
    }

}
