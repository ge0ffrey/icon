package org.optaplanner.examples.icon.parser;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.optaplanner.examples.icon.domain.Machine;
import org.optaplanner.examples.icon.domain.Task;

public final class SchedulingParser {

    public Set<Task> getTasks() {
        return this.tasks;
    }

    public int getTimeResolution() {
        return this.timeResolution;
    }

    public Set<Machine> getMachines() {
        return this.machines;
    }

    public static SchedulingParser parse(final File instance) throws IOException {
        final List<String> lines = FileUtils.readLines(instance);
        final int timeResolution = Integer.valueOf(lines.get(0));
        final int resourceCount = Integer.valueOf(lines.get(1));
        final SchedulingParser p = new SchedulingParser(timeResolution);
        // parse the machine list
        final int machineCount = Integer.valueOf(lines.get(2));
        int line = 3;
        while (line < (3 + machineCount * 2)) {
            final String[] costs = lines.get(line).split("\\Q \\E");
            final String[] capacities = lines.get(line + 1).split("\\Q \\E");
            if (capacities.length != resourceCount) {
                throw new IllegalStateException("Invalid amount of resources: " + lines.get(line + 1));
            }
            final List<Integer> capacity = new LinkedList<Integer>();
            for (final String cap : capacities) {
                capacity.add(Integer.valueOf(cap));
            }
            p.addMachine(new Machine(Integer.valueOf(costs[0]), new BigDecimal(costs[1]), new BigDecimal(costs[2]), new BigDecimal(costs[3]), capacity));
            line += 2;
        }
        // parse task list
        final int taskCount = Integer.valueOf(lines.get(line));
        final int maxTime = ((24 * 60) / timeResolution) + 1;
        for (int i = line + 1; i < line + taskCount * 2; i += 2) {
            final String[] properties = lines.get(i).split("\\Q \\E");
            final int duration = Integer.valueOf(properties[1]);
            if (duration < 0 || duration > maxTime) {
                throw new IllegalStateException("Invalid duration: " + duration);
            }
            final int earliestStart = Integer.valueOf(properties[2]);
            if (earliestStart < 0 || earliestStart > maxTime) {
                throw new IllegalStateException("Invalid earliest start: " + earliestStart);
            }
            final int deadline = Integer.valueOf(properties[3]);
            if (deadline < 0 || deadline > maxTime) {
                throw new IllegalStateException("Invalid deadline: " + deadline);
            }
            final String[] consumptions = lines.get(i + 1).split("\\Q \\E");
            if (consumptions.length != resourceCount) {
                throw new IllegalStateException("Invalid amount of resources: " + lines.get(i + 1));
            }
            final List<Integer> capacity = new LinkedList<Integer>();
            for (final String cap : consumptions) {
                capacity.add(Integer.valueOf(cap));
            }
            p.addTask(new Task(Integer.valueOf(properties[0]), duration, earliestStart, deadline, new BigDecimal(properties[4]), capacity));
        }
        return p;
    }

    private final int timeResolution;
    private final Set<Machine> machines = new LinkedHashSet<Machine>();
    private final Set<Task> tasks = new LinkedHashSet<Task>();

    private SchedulingParser(final int timeResolution) {
        this.timeResolution = timeResolution;
    }

    private SchedulingParser addMachine(final Machine m) {
        this.machines.add(m);
        return this;
    }

    private SchedulingParser addTask(final Task t) {
        this.tasks.add(t);
        return this;
    }

}
