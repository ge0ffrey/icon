package org.optaplanner.examples.icon.solver.move;

import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionFilter;
import org.optaplanner.core.impl.heuristic.selector.move.generic.SwapMove;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplanner.examples.icon.domain.TaskAssignment;


public class PeriodSwapMoveFilter implements SelectionFilter<SwapMove> {

    @Override
    public boolean accept(ScoreDirector scoreDirector, SwapMove selection) {
        TaskAssignment left = (TaskAssignment)selection.getLeftEntity();
        TaskAssignment right = (TaskAssignment)selection.getRightEntity();
        if (!left.getStartPeriodValueRange().contains(right.getStartPeriod())) {
            return false;
        }
        if (!right.getStartPeriodValueRange().contains(left.getStartPeriod())) {
            return false;
        }
        return true;
    }

}
