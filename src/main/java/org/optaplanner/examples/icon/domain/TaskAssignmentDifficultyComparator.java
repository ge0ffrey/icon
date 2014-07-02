package org.optaplanner.examples.icon.domain;

import java.util.Comparator;

public class TaskAssignmentDifficultyComparator implements Comparator<TaskAssignment> {

    @Override
    public int compare(final TaskAssignment o1, final TaskAssignment o2) {
        final Task t1 = o1.getTask();
        final long d1 = t1.getDifficulty();
        final Task t2 = o2.getTask();
        final long d2 = t2.getDifficulty();
        if (d1 > d2) {
            return 1;
        } else if (d1 == d2) {
            final int id1 = t1.getId();
            final int id2 = t2.getId();
            if (id1 > id2) {
                return -1;
            } else if (id1 == id2) {
                return 0;
            } else {
                return 1;
            }
        } else {
            return -1;
        }
    }

}
