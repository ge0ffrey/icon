package org.optaplanner.examples.icon.parser;

import java.io.File;
import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class InstanceParserTest extends AbstractParserTest {

    public InstanceParserTest(final File fileUnderTest) {
        super(new File(fileUnderTest, "instance.txt"));
    }

    @Test
    public void testParsing() {
        try {
            final InstanceParser p = InstanceParser.parse(this.getFileUnderTest());
            Assertions.assertThat(p).isNotNull();
            Assertions.assertThat(p.getMachines()).isNotEmpty();
            Assertions.assertThat(p.getTaskAssignments()).isNotEmpty();
        } catch (final IOException e) {
            Assertions.fail("Failed parsing instance file.", e);
        }
    }

}
