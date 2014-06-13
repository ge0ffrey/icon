package org.optaplanner.examples.icon.parser.forecast;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.optaplanner.examples.icon.domain.Forecast;
import org.optaplanner.examples.icon.domain.Forecast.ForecastBuilder;

public class ForecastParser {

    public static Forecast parse(final File forecasts) throws IOException {
        final List<String> lines = FileUtils.readLines(forecasts);
        final int numOfPeriods = Integer.valueOf(lines.get(0));
        final ForecastBuilder builder = Forecast.withPeriods(numOfPeriods);
        for (int i = 1; i < lines.size(); i++) {
            final String line = lines.get(i);
            final String[] parts = line.split("\\Q \\E");
            builder.addForecast(parts[0], parts[1]);
        }
        return builder.build();
    }

}
