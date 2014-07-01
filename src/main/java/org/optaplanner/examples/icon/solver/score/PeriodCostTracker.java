package org.optaplanner.examples.icon.solver.score;

import java.util.HashMap;
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
        return 0;
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
        return 0;
    }

}
