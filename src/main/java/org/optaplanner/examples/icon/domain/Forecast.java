package org.optaplanner.examples.icon.domain;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.math.BigDecimal;
import java.util.Collection;

public class Forecast {

    public static final class ForecastBuilder {

        private final Object2ObjectMap<Period, BigDecimal> forecasts = new Object2ObjectOpenHashMap<Period, BigDecimal>();
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
            this.forecasts.put(Period.get(Integer.valueOf(period)), new BigDecimal(forecast));
            return this;
        }

        public Forecast build() {
            return new Forecast(this.forecasts);
        }

    }

    public static ForecastBuilder withPeriods(final int numPeriods) {
        return new ForecastBuilder(numPeriods);
    }

    private final Object2ObjectMap<Period, PeriodPowerConsumption> forecasts = new Object2ObjectOpenHashMap<Period, PeriodPowerConsumption>();

    private Forecast(final Object2ObjectMap<Period, BigDecimal> forecasts) {
        for (final Entry<Period, BigDecimal> entry : forecasts.object2ObjectEntrySet()) {
            final Period slot = entry.getKey();
            this.forecasts.put(slot, new PeriodPowerConsumption(slot, entry.getValue()));
        }
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

    public Collection<PeriodPowerConsumption> getAll() {
        return this.forecasts.values();
    }

    public PeriodPowerConsumption getForPeriod(final Period period) {
        final PeriodPowerConsumption forecast = this.forecasts.get(period);
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
