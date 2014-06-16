package org.optaplanner.examples.icon.solver.solution.initializer;

import java.util.ArrayList;
import java.util.List;

import org.optaplanner.core.impl.phase.custom.CustomPhaseCommand;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplanner.examples.icon.domain.Machine;
import org.optaplanner.examples.icon.domain.Period;
import org.optaplanner.examples.icon.domain.Schedule;
import org.optaplanner.examples.icon.domain.Task;

public class FirstAvailableSolutionInitializer implements CustomPhaseCommand {

    @Override
    public void changeWorkingSolution(final ScoreDirector scoreDirector) {
        this.initializeSchedule(scoreDirector, (Schedule) scoreDirector.getWorkingSolution());
    }

    private void initializeSchedule(final ScoreDirector scoreDirector, final Schedule schedule) {
        final List<Machine> machines = new ArrayList<Machine>(schedule.getMachines());
        for (final Task t : schedule.getTasks()) {
            int id = t.getId();
            final Machine m = machines.get(id % machines.size()); // cycle through machines
            scoreDirector.beforeVariableChanged(t, "executor");
            t.setExecutor(m);
            scoreDirector.afterVariableChanged(t, "executor");
            scoreDirector.beforeVariableChanged(t, "startPeriod");
            if (id % 2 == 0) {
                t.setStartPeriod(t.getEarliestStart()); // schedule task as early as possible
            } else {
                Period latestEnd = t.getLatestEnd();
                int targetId = latestEnd.getId() - t.getDuration();
                t.setStartPeriod(Period.get(targetId)); // schedule task as late as possible
            }
            scoreDirector.afterVariableChanged(t, "startPeriod");
        }
    }

}
