package org.optaplanner.examples.icon.solver.score;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.optaplanner.examples.icon.domain.Machine;
import org.optaplanner.examples.icon.domain.Period;
import org.optaplanner.examples.icon.domain.Schedule;
import org.optaplanner.examples.icon.domain.TaskAssignment;

/**
 * Calculates costs for idle time and or startup+shutdown.
 */
public class PeriodCostTracker {

    private final Map<Period, Set<TaskAssignment>> activeTasks = new HashMap<Period, Set<TaskAssignment>>();

    private final long cost = 0;

    private final Machine machine;

    private final Schedule schedule;

    public PeriodCostTracker(final Schedule schedule, final Machine m) {
        this.schedule = schedule;
        this.machine = m;
    }

    public long add(final TaskAssignment ta) {
        // if we're adding the first task, the machine becomes active. add one default startup/shutdown cost
        final long costChange = this.activeTasks.isEmpty() ? ta.getExecutor().getCostOfRespin() : 0;
        for (int i = ta.getStartPeriod().getId(); i <= ta.getFinalPeriod().getId(); i++) {
            final Period p = Period.get(i);
            if (!this.activeTasks.containsKey(p)) {
                this.activeTasks.put(p, new HashSet<TaskAssignment>());
            }
            this.activeTasks.get(p).add(ta);
        }
        return costChange;
    }

    public long getCost() {
        return this.cost;
    }

    public Machine getMachine() {
        return this.machine;
    }

    public Schedule getSchedule() {
        return this.schedule;
    }

    public long remove(final TaskAssignment ta) {
        long costChange = 0;
        for (int i = ta.getStartPeriod().getId(); i <= ta.getFinalPeriod().getId(); i++) {
            final Period p = Period.get(i);
            final Set<TaskAssignment> runningTasks = this.activeTasks.get(p);
            runningTasks.remove(ta);
            if (runningTasks.isEmpty()) {
                // no more tasks, machine is inactive during this period
                this.activeTasks.remove(p);
            }
        }
        if (this.activeTasks.size() == 0) {
            // the machine is never started or stopped; change the constraints
            costChange += ta.getExecutor().getCostOfRespin();
        }
        return costChange;
    }

}
