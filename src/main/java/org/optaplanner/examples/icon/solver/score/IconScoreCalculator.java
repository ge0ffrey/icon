package org.optaplanner.examples.icon.solver.score;

import org.optaplanner.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import org.optaplanner.core.impl.score.director.easy.EasyScoreCalculator;
import org.optaplanner.examples.icon.domain.Schedule;

public class IconScoreCalculator implements EasyScoreCalculator<Schedule> {

    @Override
    public HardSoftLongScore calculateScore(final Schedule sched) {
        final InternalCalculator calc = new InternalCalculator(sched);
        return HardSoftLongScore.valueOf(calc.hardScore(), calc.softScore());
    }

}
