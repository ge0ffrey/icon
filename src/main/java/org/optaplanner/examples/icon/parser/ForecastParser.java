package org.optaplanner.examples.icon.parser;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.optaplanner.examples.icon.domain.Forecast;
import org.optaplanner.examples.icon.domain.Forecast.ForecastBuilder;

final class ForecastParser {

    private static BigDecimal getMultiplier(final int numOfPeriods) {
        final BigDecimal bd1440 = BigDecimal.valueOf(1440);
        final BigDecimal periods = BigDecimal.valueOf(numOfPeriods);
        final BigDecimal bd60 = BigDecimal.valueOf(60);
        return bd1440.divide(periods).divide(bd60, 10, RoundingMode.HALF_EVEN);
    }

    public static Forecast parse(final File forecasts) throws IOException {
        final List<String> lines = FileUtils.readLines(forecasts);
        final int numOfPeriods = Integer.valueOf(lines.get(0));
        final BigDecimal multiplier = ForecastParser.getMultiplier(numOfPeriods);
        final ForecastBuilder builder = Forecast.withPeriods(numOfPeriods);
        for (int i = 1; i < lines.size(); i++) {
            final String line = lines.get(i);
            final String[] parts = line.split("\\Q \\E");
            builder.addForecast(parts[0], parts[1], multiplier);
        }
        return builder.build();
    }

}
