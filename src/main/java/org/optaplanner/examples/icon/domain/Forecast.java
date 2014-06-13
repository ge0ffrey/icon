package org.optaplanner.examples.icon.domain;

import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;

import java.math.BigDecimal;

public class Forecast {

    public static final class ForecastBuilder {

        private final Int2ObjectSortedMap<BigDecimal> forecasts = new Int2ObjectRBTreeMap<BigDecimal>();
        private final int maxValue;

        private ForecastBuilder(final int numPeriods) {
            this.maxValue = numPeriods;
        }

        public ForecastBuilder addForecast(final String period, final String forecast) {
            if (this.forecasts.size() == this.maxValue) {
                throw new IllegalStateException("Already seen the expected number of forecasts.");
            }
            final int periodId = Integer.valueOf(period);
            if (this.forecasts.containsKey(periodId)) {
                throw new IllegalArgumentException("Already seen forecast for period: " + periodId);
            }
            this.forecasts.put(periodId, new BigDecimal(forecast));
            return this;
        }

        public Forecast build() {
            return new Forecast(this.forecasts);
        }

    }

    public static ForecastBuilder withPeriods(final int numPeriods) {
        return new ForecastBuilder(numPeriods);
    }

    private final Int2ObjectSortedMap<BigDecimal> forecasts;

    private Forecast(final Int2ObjectSortedMap<BigDecimal> forecasts) {
        this.forecasts = new Int2ObjectAVLTreeMap<BigDecimal>(forecasts);
    }

    public int countPeriods() {
        return this.forecasts.size();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Forecast)) {
            return false;
        }
        final Forecast other = (Forecast) obj;
        if (this.forecasts == null) {
            if (other.forecasts != null) {
                return false;
            }
        } else if (!this.forecasts.equals(other.forecasts)) {
            return false;
        }
        return true;
    }

    public BigDecimal forPeriod(final int period) {
        final BigDecimal forecast = this.forecasts.get(period);
        if (forecast == null) {
            throw new IllegalArgumentException("Unknown forecasting period: " + period);
        }
        return forecast;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.forecasts == null) ? 0 : this.forecasts.hashCode());
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Forecast [");
        if (this.forecasts != null) {
            builder.append("forecasts=").append(this.forecasts);
        }
        builder.append("]");
        return builder.toString();
    }

}
