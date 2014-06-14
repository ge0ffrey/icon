package org.optaplanner.examples.icon.util;

import java.util.Iterator;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.optaplanner.examples.icon.domain.Period;

public class PeriodValueRangeTest {

    @Test(expected = IllegalArgumentException.class)
    public void testEmpty() {
        new PeriodValueRange(0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegative() {
        new PeriodValueRange(-1, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReverse() {
        new PeriodValueRange(1, 0);
    }

    @Test
    public void testValid() {
        final PeriodValueRange r = new PeriodValueRange(0, 1);
        Assertions.assertThat(r.contains(Period.get(0))).isTrue();
        Assertions.assertThat(r.get(0)).isEqualTo(Period.get(0));
        final Iterator<Period> p = r.createOriginalIterator();
        Assertions.assertThat(p.hasNext()).isTrue();
        Assertions.assertThat(p.next()).isEqualTo(Period.get(0));
    }
}
