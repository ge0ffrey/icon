package org.optaplanner.examples.icon.util;

import java.math.BigDecimal;

public class FixedPointArithmetic {

    // the least value where the scores reported by us and by the solution checker are equal to within 0.0001.
    private static final int DEFAULT_SCALE = 11;

    public static long fromBigDecimal(final BigDecimal value) {
        return FixedPointArithmetic.fromBigDecimal(value, FixedPointArithmetic.DEFAULT_SCALE);
    }

    public static long fromBigDecimal(final BigDecimal value, final int targetScale) {
        return value.multiply(BigDecimal.TEN.pow(targetScale)).longValue();
    }

    public static long multiply(final long first, final int firstScale, final long second, final int secondScale, final int targetScale) {
        final double firstDouble = first / Math.pow(10, firstScale);
        final double secondDouble = second / Math.pow(10, secondScale);
        final double result = firstDouble * secondDouble;
        return Math.round(result * Math.pow(10, targetScale));
    }

    public static long multiply(final long first, final long second) {
        return FixedPointArithmetic.multiply(first, second, FixedPointArithmetic.DEFAULT_SCALE);
    }

    public static long multiply(final long first, final long second, final int targetScale) {
        return FixedPointArithmetic.multiply(first, FixedPointArithmetic.DEFAULT_SCALE, second, FixedPointArithmetic.DEFAULT_SCALE, targetScale);
    }

}
