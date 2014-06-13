package org.optaplanner.examples.icon.domain;

import java.math.BigDecimal;

import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;


public class Forecast {

    public static ForecastBuilder withPeriods(final int numPeriods) {
        return new ForecastBuilder(numPeriods);
    }
    
    public static final class ForecastBuilder {
        
        private final Int2ObjectSortedMap<BigDecimal> forecasts = new Int2ObjectRBTreeMap<>();
        private final int maxValue;
        
        private ForecastBuilder(final int numPeriods) {
            this.maxValue = numPeriods;
        }
        
        public ForecastBuilder addForecast(final String period, final String forecast) {
            if (forecasts.size() == maxValue) {
                throw new IllegalStateException("Already seen the expected number of forecasts.");
            }
            int periodId = Integer.valueOf(period);
            if (forecasts.containsKey(periodId)) {
                throw new IllegalArgumentException("Already seen forecast for period: " + periodId);
            }
            forecasts.put(periodId, new BigDecimal(forecast));
            return this;
        }
        
        public Forecast build() {
            return new Forecast(forecasts);
        }
        
    }
    
    private final Int2ObjectSortedMap<BigDecimal> forecasts;

    private Forecast(Int2ObjectSortedMap<BigDecimal> forecasts) {
        this.forecasts = new Int2ObjectAVLTreeMap<>(forecasts);
    }
    
    public BigDecimal getForPeriod(final int period) {
        BigDecimal forecast = forecasts.get(period);
        if (forecast == null) {
            throw new IllegalArgumentException("Unknown forecasting period: " + period);
        }
        return forecast;
    }
    
}
