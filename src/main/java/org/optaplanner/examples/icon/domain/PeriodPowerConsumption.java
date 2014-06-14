package org.optaplanner.examples.icon.domain;

import java.math.BigDecimal;

public class PeriodPowerConsumption {

    private final Period period;

    private final BigDecimal powerConsumption;

    PeriodPowerConsumption(final Period period, final BigDecimal consumption) {
        this.period = period;
        this.powerConsumption = consumption;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof PeriodPowerConsumption)) {
            return false;
        }
        final PeriodPowerConsumption other = (PeriodPowerConsumption) obj;
        if (this.period == null) {
            if (other.period != null) {
                return false;
            }
        } else if (!this.period.equals(other.period)) {
            return false;
        }
        return true;
    }

    public BigDecimal getPowerConsumption() {
        return this.powerConsumption;
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
        final StringBuilder builder = new StringBuilder();
        builder.append("PeriodPowerConsumption [");
        if (this.period != null) {
            builder.append("period=").append(this.period).append(", ");
        }
        builder.append("powerConsumption=").append(this.powerConsumption);
        builder.append("]");
        return builder.toString();
    }

}
