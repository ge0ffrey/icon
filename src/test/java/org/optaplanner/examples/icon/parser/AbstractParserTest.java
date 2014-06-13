package org.optaplanner.examples.icon.parser;

import java.io.File;
import java.io.FileFilter;
import java.util.Collection;
import java.util.LinkedList;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public abstract class AbstractParserTest {

    private static final File INPUT = new File("data/icon/input/");
    private static final FileFilter INPUT_FILTER = new FileFilter() {

        @Override
        public boolean accept(final File pathname) {
            return pathname.isDirectory();
        }

    };

    public File getFileUnderTest() {
        return this.fileUnderTest;
    }

    @Parameters(name = "{0}")
    public static Collection<Object[]> getInputData() {
        final Collection<Object[]> problems = new LinkedList<Object[]>();
        for (final File f : AbstractParserTest.INPUT.listFiles(AbstractParserTest.INPUT_FILTER)) {
            problems.add(new File[]{f});
        }
        return problems;
    }

    private final File fileUnderTest;

    public AbstractParserTest(final File fileUnderTest) {
        this.fileUnderTest = fileUnderTest;
    }

}
