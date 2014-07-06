package org.optaplanner.examples.icon.parser;

import java.io.File;
import java.io.IOException;

import org.optaplanner.examples.icon.domain.Forecast;
import org.optaplanner.examples.icon.domain.Schedule;

public class ProblemParser {

    public static Schedule parse(final File forecast, final File instance) throws IOException {
        final Forecast f = ForecastParser.parse(forecast);
        final InstanceParser i = InstanceParser.parse(instance, f);
        return new Schedule(i.getTimeResolution(), i.getResourceCount(), i.getMachines(), i.getTaskAssignments(), f);
    }

}
