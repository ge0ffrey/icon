package org.optaplanner.examples.icon.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.optaplanner.core.api.domain.solution.Solution;
import org.optaplanner.examples.icon.domain.Schedule;
import org.optaplanner.examples.icon.domain.Task;
import org.optaplanner.examples.icon.domain.TaskAssignment;
import org.optaplanner.examples.icon.parser.ProblemParser;
import org.optaplanner.persistence.common.api.domain.solution.SolutionFileIO;

public class IconSolutionFileIO implements SolutionFileIO {

    public static final String FILE_EXTENSION = "txt";
    public static final String FORECAST_FILENAME = "forecast";
    public static final String INSTANCE_FILENAME = "instance";

    @Override
    public String getInputFileExtension() {
        return FILE_EXTENSION;
    }

    @Override
    public String getOutputFileExtension() {
        return FILE_EXTENSION;
    }

    @Override
    public Solution read(File inputSolutionDirectory) {
        if (inputSolutionDirectory == null || !inputSolutionDirectory.isDirectory()) {
            throw new IllegalArgumentException("Invalid input solution directory (" + inputSolutionDirectory + ").");
        }
        File forecastInputSolutionFile = new File(inputSolutionDirectory, FORECAST_FILENAME + "." + FILE_EXTENSION);
        File instanceInputSolutionFile = new File(inputSolutionDirectory, INSTANCE_FILENAME + "." + FILE_EXTENSION);

        Solution solution = null;
        try {
            solution = ProblemParser.parse(forecastInputSolutionFile, instanceInputSolutionFile);
        } catch (IOException ex) {
            throw new IllegalStateException("Exception while loading input solution files.", ex);
        }
        return solution;
    }

    @Override
    public void write(Solution solution, File outputSolutionFile) {
        Schedule schedule = (Schedule) solution;
        Set<TaskAssignment> tasks = schedule.getTaskAssignments();
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputSolutionFile), "UTF-8"));
            for (TaskAssignment task : tasks) {
                // TODO extend the output if needed
                writer.write(String.valueOf(task.getTask().getId()));
                writer.write(" ");
                writer.write(String.valueOf(task.getExecutor().getId()));
                writer.write(" ");
                writer.write(String.valueOf(task.getStartPeriod().getId()));
                writer.newLine();
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Exception while writing solution to file (" + outputSolutionFile + ").");
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }
}
