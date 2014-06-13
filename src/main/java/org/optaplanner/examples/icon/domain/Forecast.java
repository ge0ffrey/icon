package org.optaplanner.examples.icon.domain;

import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;

import java.math.BigDecimal;

public class Forecast {

    public static ForecastBuilder withPeriods(final int numPeriods) {
        return new ForecastBuilder(numPeriods);
    }

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

    private final Int2ObjectSortedMap<BigDecimal> forecasts;

    private Forecast(final Int2ObjectSortedMap<BigDecimal> forecasts) {
        this.forecasts = new Int2ObjectAVLTreeMap<BigDecimal>(forecasts);
    }

    public BigDecimal getForPeriod(final int period) {
        final BigDecimal forecast = this.forecasts.get(period);
        if (forecast == null) {
            throw new IllegalArgumentException("Unknown forecasting period: " + period);
        }
        return forecast;
    }

}
