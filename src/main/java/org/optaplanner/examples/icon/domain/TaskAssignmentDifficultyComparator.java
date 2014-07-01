package org.optaplanner.examples.icon.domain;

import java.util.Comparator;

public class TaskAssignmentDifficultyComparator implements Comparator<TaskAssignment> {

    @Override
    public int compare(final TaskAssignment o1, final TaskAssignment o2) {
        final long d1 = o1.getTask().getDifficulty();
        final long d2 = o2.getTask().getDifficulty();
        if (d1 > d2) {
            return 1;
        } else if (d1 == d2) {
            return 0;
        } else {
            return -1;
        }
    }

}
