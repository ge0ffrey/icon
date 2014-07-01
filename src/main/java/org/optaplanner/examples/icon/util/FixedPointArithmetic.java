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
        // to avoid long overflows and BigInteger, we deliberately lose some precision
        final int scaleLoss = 3; // the least loss that I think is enough to avoid long overflow 
        final int scaleLossFactor = (int) Math.pow(10, scaleLoss);
        final long actualFirst = first / scaleLossFactor;
        final long actualSecond = second / scaleLossFactor;
        final int actualFirstScale = firstScale - scaleLoss;
        final int actualSecondScale = secondScale - scaleLoss;
        // and now calculate the result and convert it to the proper scale
        final long result = actualFirst * actualSecond;
        // FIXME we should somehow do overflow detection here; just to be sure
        final int sourceScale = actualFirstScale + actualSecondScale;
        if (sourceScale == targetScale) {
            return result;
        } else {
            final long scaleAdjustment = (long) Math.pow(10, Math.abs(sourceScale - targetScale));
            if (sourceScale > targetScale) {
                return result / scaleAdjustment;
            } else {
                return result * scaleAdjustment;
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
