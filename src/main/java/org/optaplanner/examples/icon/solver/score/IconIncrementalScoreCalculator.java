package org.optaplanner.examples.icon.solver.score;

import org.optaplanner.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import org.optaplanner.core.impl.score.director.incremental.IncrementalScoreCalculator;
import org.optaplanner.examples.icon.domain.Schedule;

public class IconIncrementalScoreCalculator implements IncrementalScoreCalculator<Schedule> {

    private Schedule schedule;

    @Override
    public void afterEntityAdded(final Object entity) {
        // TODO Auto-generated method stub

    }

    @Override
    public void afterEntityRemoved(final Object entity) {
        // TODO Auto-generated method stub

    }

    @Override
    public void afterVariableChanged(final Object entity, final String variableName) {
        // TODO Auto-generated method stub

    }

    @Override
    public void beforeEntityAdded(final Object entity) {
        // TODO Auto-generated method stub

    }

    @Override
    public void beforeEntityRemoved(final Object entity) {
        // TODO Auto-generated method stub

    }

    @Override
    public void beforeVariableChanged(final Object entity, final String variableName) {
        // TODO Auto-generated method stub

    }

    @Override
    public String buildScoreCorruptionAnalysis(final IncrementalScoreCalculator uncorruptedIncrementalScoreCalculator) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public HardSoftLongScore calculateScore() {
        final InternalCalculator calc = new InternalCalculator(this.schedule);
        return HardSoftLongScore.valueOf(calc.hardScore(), calc.softScore());
    }

    @Override
    public void resetWorkingSolution(final Schedule workingSolution) {
        this.schedule = workingSolution;
    }

}
