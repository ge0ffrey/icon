package org.optaplanner.examples.icon.solver.score;

import org.optaplanner.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import org.optaplanner.core.impl.score.director.easy.EasyScoreCalculator;
import org.optaplanner.examples.icon.domain.Schedule;

/**
 * ONLY USE FOR SOLUTION VALIDATION!
 *
 * This will not set shutdown possibilities properly and therefore could not create a proper solution.
 *
 */
public class IconScoreCalculator implements EasyScoreCalculator<Schedule> {

    @Override
    public HardSoftLongScore calculateScore(final Schedule sched) {
        final InternalCalculator calc = new InternalCalculator(sched);
        return HardSoftLongScore.valueOf(calc.hardScore(), calc.softScore());
    }

}
