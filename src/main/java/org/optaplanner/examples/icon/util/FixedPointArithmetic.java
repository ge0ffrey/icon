package org.optaplanner.examples.icon.util;

import java.math.BigDecimal;
import java.math.BigInteger;

public class FixedPointArithmetic {

    // the least value where the score reported by us and by the solution checker are equal to within 0.0001.
    private static final int DEFAULT_SCALE = 11;

    public static long fromBigDecimal(final BigDecimal value) {
        return FixedPointArithmetic.fromBigDecimal(value, FixedPointArithmetic.DEFAULT_SCALE);
    }

    public static long fromBigDecimal(final BigDecimal value, final int targetScale) {
        return value.multiply(BigDecimal.TEN.pow(targetScale)).longValue();
    }

    public static long multiply(final long first, final int firstScale, final long second, final int secondScale, final int targetScale) {
        /*
         * need to work with BigInteger to avoid long overflows in the default scale. this is a performance hit, but
         * it's still much better than when all the sums were done in BigDecimal as well.
         */
        final BigInteger i = BigInteger.valueOf(first);
        final BigInteger j = BigInteger.valueOf(second);
        final BigInteger result = i.multiply(j);
        final int sourceScale = firstScale + secondScale;
        if (sourceScale == targetScale) {
            return result.longValue();
        } else {
            final long scaleAdjustment = (long) Math.pow(10, Math.abs(sourceScale - targetScale));
            if (sourceScale > targetScale) {
                return result.divide(BigInteger.valueOf(scaleAdjustment)).longValue();
            } else {
                return result.multiply(BigInteger.valueOf(scaleAdjustment)).longValue();
            }
        }
    }

    public static long multiply(final long first, final long second) {
        return FixedPointArithmetic.multiply(first, second, FixedPointArithmetic.DEFAULT_SCALE);
    }

    public static long multiply(final long first, final long second, final int targetScale) {
        return FixedPointArithmetic.multiply(first, FixedPointArithmetic.DEFAULT_SCALE, second, FixedPointArithmetic.DEFAULT_SCALE, targetScale);
    }

}
