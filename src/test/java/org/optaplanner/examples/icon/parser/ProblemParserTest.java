package org.optaplanner.examples.icon.parser;

import java.io.File;
import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.optaplanner.examples.icon.domain.Schedule;

public class ProblemParserTest extends AbstractParserTest {

    public ProblemParserTest(final File fileUnderTest) {
        super(fileUnderTest);
    }

    @Test
    public void testParsing() {
        try {
            final Schedule s = ProblemParser.parse(new File(this.getFileUnderTest(), "forecast.txt"), new File(this.getFileUnderTest(), "instance.txt"));
            Assertions.assertThat(s).isNotNull();
            Assertions.assertThat(s.getMachines()).isNotEmpty();
            Assertions.assertThat(s.getTaskAssignments()).isNotEmpty();
        } catch (final IOException e) {
            Assertions.fail("Failed parsing problem.", e);
        }
    }

}
