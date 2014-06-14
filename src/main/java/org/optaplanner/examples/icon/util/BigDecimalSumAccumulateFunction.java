package org.optaplanner.examples.icon.util;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.math.BigDecimal;

import org.kie.api.runtime.rule.AccumulateFunction;

/**
 * Adapted from http://scattercode.co.uk/2013/01/17/a-bigdecimal-accumulator-for-drools/
 */
public class BigDecimalSumAccumulateFunction implements AccumulateFunction {

    /**
     * Session-specific data required by the accumulator is stored in a
     * an instance of this class.
     */
    private static class BigDecimalSum implements Serializable {

        /** Generated serialVersionUID */
        private static final long serialVersionUID = -3852330030144129793L;
        BigDecimal sum = BigDecimal.ZERO;

        void add(final BigDecimal augend) {
            this.sum = this.sum.add(augend);
        }

        void init() {
            this.sum = BigDecimal.ZERO;
        }

        void subtract(final BigDecimal subtrahend) {
            this.sum = this.sum.subtract(subtrahend);
        }
    }

    /**
     * Adds the value to the accumulator sum.
     */
    @Override
    public void accumulate(final Serializable context, final Object value) {
        final BigDecimalSum accumulator = (BigDecimalSum) context;
        accumulator.add((BigDecimal) value);
    }

    /**
     * Session-specific data required by the accumulator is stored in a {@link BigDecimalSum} context, which is
     * instantiated by this
     * method.
     */
    @Override
    public Serializable createContext() {
        return new BigDecimalSum();
    }

    /**
     * Returns the current 'sum' held in the accumulator.
     */
    @Override
    public Object getResult(final Serializable context) throws Exception {
        final BigDecimalSum accumulator = (BigDecimalSum) context;
        return accumulator.sum;
    }

    /**
     * Returns the class of the object returned by getResult.
     */
    @Override
    public Class<BigDecimal> getResultType() {
        return BigDecimal.class;
    }

    /**
     * Initializes the accumulator with an empty list of {@link BigDecimal}.
     */
    @Override
    public void init(final Serializable context) throws Exception {
        final BigDecimalSum accumulator = (BigDecimalSum) context;
        accumulator.init();
    }

    /**
     * Required to support {@link Externalizable} interface so that data can be
     * shared across sessions. However, we don't need to do that, so this method
     * is empty.
     */
    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
    }

    /**
     * Subtracts the value from the accumulator sum.
     */
    @Override
    public void reverse(final Serializable context, final Object value) throws Exception {
        final BigDecimalSum accumulator = (BigDecimalSum) context;
        accumulator.subtract((BigDecimal) value);
    }

    /**
     * Yes, this accumulator does implement the reverse method..
     */
    @Override
    public boolean supportsReverse() {
        return true;
    }

    /**
     * Required to support {@link Externalizable} interface so that data can be
     * shared across sessions. However, we don't need to do that, so this method
     * is empty.
     */
    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
    }

}