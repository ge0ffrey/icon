package org.optaplanner.examples.icon.solver.move;

import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionFilter;
import org.optaplanner.core.impl.heuristic.selector.move.generic.SwapMove;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplanner.examples.icon.domain.TaskAssignment;


public class ExecutorSwapMoveFilter implements SelectionFilter<SwapMove> {

    @Override
    public boolean accept(ScoreDirector scoreDirector, SwapMove selection) {
        TaskAssignment left = (TaskAssignment)selection.getLeftEntity();
        TaskAssignment right = (TaskAssignment)selection.getRightEntity();
        if (!left.getPossibleExecutors().contains(right.getExecutor())) {
            return false;
        }
        if (!right.getPossibleExecutors().contains(left.getExecutor())) {
            return false;
        }
        return true;
    }

}
