package org.optaplanner.examples.icon.solver.solution.initializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.optaplanner.core.impl.phase.custom.CustomPhaseCommand;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplanner.examples.icon.domain.Machine;
import org.optaplanner.examples.icon.domain.Schedule;
import org.optaplanner.examples.icon.domain.Task;

public class RandomizedSolutionInitializer implements CustomPhaseCommand {

    private static final Random RANDOM = new Random(0);
    
    @Override
    public void changeWorkingSolution(final ScoreDirector scoreDirector) {
        this.initializeSchedule(scoreDirector, (Schedule) scoreDirector.getWorkingSolution());
    }

    private void initializeSchedule(final ScoreDirector scoreDirector, final Schedule schedule) {
        final List<Machine> machines = new ArrayList<Machine>(schedule.getMachines());
        for (final Task t : schedule.getTasks()) {
            final Machine m = machines.get(RANDOM.nextInt(machines.size())); // cycle through machines
            scoreDirector.beforeVariableChanged(t, "executor");
            t.setExecutor(m);
            scoreDirector.afterVariableChanged(t, "executor");
            scoreDirector.beforeVariableChanged(t, "startPeriod");
            t.setStartPeriod(t.getStartPeriodValueRange().createRandomIterator(RANDOM).next());
            scoreDirector.afterVariableChanged(t, "startPeriod");
        }
    }

}
