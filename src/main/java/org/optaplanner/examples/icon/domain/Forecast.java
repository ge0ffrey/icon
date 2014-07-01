package org.optaplanner.examples.icon.domain;

import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectSortedMap;

import java.math.BigDecimal;
import java.util.Collection;

public class Forecast {

    public static final class ForecastBuilder {

        private final Object2ObjectMap<Period, BigDecimal> forecasts = new Object2ObjectOpenHashMap<Period, BigDecimal>();
        private final int maxValue;

        private ForecastBuilder(final int numPeriods) {
            this.maxValue = numPeriods;
        }

        /**
         *
         * @param period
         * @param forecast
         *            The forecasted power cost.
         * @param multiplier
         *            Q/60, see constraints 13 and 14. Accounting for this in power costs here prevents us to multiply
         *            everything by this number during scoring.
         * @return
         */
        public ForecastBuilder addForecast(final String period, final String forecast, final BigDecimal multiplier) {
            if (this.forecasts.size() == this.maxValue) {
                throw new IllegalStateException("Already seen the expected number of forecasts.");
            }
            final int periodId = Integer.valueOf(period);
            if (this.forecasts.containsKey(periodId)) {
                throw new IllegalArgumentException("Already seen forecast for period: " + periodId);
            }
            this.forecasts.put(Period.get(Integer.valueOf(period)), new BigDecimal(forecast).multiply(multiplier));
            return this;
        }

        public Forecast build() {
            return new Forecast(this.forecasts);
        }

    }

    public static ForecastBuilder withPeriods(final int numPeriods) {
        return new ForecastBuilder(numPeriods);
    }

    private final Object2ObjectSortedMap<Period, PeriodPowerCost> forecasts = new Object2ObjectAVLTreeMap<Period, PeriodPowerCost>();

    private Forecast(final Object2ObjectMap<Period, BigDecimal> forecasts) {
        for (final Entry<Period, BigDecimal> entry : forecasts.object2ObjectEntrySet()) {
            final Period slot = entry.getKey();
            this.forecasts.put(slot, new PeriodPowerCost(slot, entry.getValue()));
        }
    }

    public Collection<PeriodPowerCost> getAll() {
        return this.forecasts.values();
    }

    public PeriodPowerCost getForPeriod(final Period period) {
        final PeriodPowerCost forecast = this.forecasts.get(period);
        if (forecast == null) {
            throw new IllegalArgumentException("Unknown forecasting period: " + period);
        }
        return forecast;
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
