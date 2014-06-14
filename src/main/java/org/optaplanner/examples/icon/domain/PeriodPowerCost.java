package org.optaplanner.examples.icon.domain;

import java.math.BigDecimal;

public class PeriodPowerCost {

    private final BigDecimal cost;

    private final Period period;

    PeriodPowerCost(final Period period, final BigDecimal consumption) {
        this.period = period;
        this.cost = consumption;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof PeriodPowerCost)) {
            return false;
        }
        final PeriodPowerCost other = (PeriodPowerCost) obj;
        if (this.period == null) {
            if (other.period != null) {
                return false;
            }
        } else if (!this.period.equals(other.period)) {
            return false;
        }
        return true;
    }

    public BigDecimal getCost() {
        return this.cost;
    }

    public Period getPeriod() {
        return this.period;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.period == null) ? 0 : this.period.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PeriodPowerCost [");
        if (period != null) {
            builder.append("period=").append(period).append(", ");
        }
        if (cost != null) {
            builder.append("cost=").append(cost);
        }
        builder.append("]");
        return builder.toString();
    }

}
