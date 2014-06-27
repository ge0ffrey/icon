package org.optaplanner.examples.icon.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.io.IOUtils;
import org.optaplanner.core.api.domain.solution.Solution;
import org.optaplanner.examples.icon.domain.Schedule;
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
        List<TaskAssignment> taskAssignmentList = new ArrayList<TaskAssignment>(schedule.getTaskAssignments());
        Map<Integer, List<TaskAssignment>> machineTaskMap = buildMachineTaskAssignmentMap(taskAssignmentList);
        Map<Integer, List<MachineOnOffEventHolder>> machineOnOffEventMap = getMachineOnOffMapping(machineTaskMap);
        
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputSolutionFile), "UTF-8"));
            writer.write(String.valueOf(machineTaskMap.keySet().size())); // number of machines
            writer.newLine();
            for (Entry<Integer, List<MachineOnOffEventHolder>> entry : machineOnOffEventMap.entrySet()) {
                writer.write(String.valueOf(entry.getKey())); // machine id
                writer.newLine();
                if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                    writer.write(String.valueOf(entry.getValue().size())); // number of on/off events
                    writer.newLine();
                    for (MachineOnOffEventHolder eventHolder : entry.getValue()) {
                        writer.write(eventHolder.isOn() ? String.valueOf(1) : String.valueOf(0)); // on/off event
                        writer.write(" ");
                        writer.write(String.valueOf(eventHolder.getTime())); // time of event
                        writer.newLine();
                    }
                } else {
                    writer.write(String.valueOf(0));
                    writer.newLine();
                }
            }
            writer.write(String.valueOf(taskAssignmentList.size())); // number of tasks
            writer.newLine();
            for (TaskAssignment taskAssignment : taskAssignmentList) {
                writer.write(String.valueOf(taskAssignment.getTask().getId())); // task id
                writer.write(" ");
                writer.write(String.valueOf(taskAssignment.getExecutor().getId())); // machine id
                writer.write(" ");
                writer.write(String.valueOf(taskAssignment.getStartPeriod().getId())); // start period
                writer.newLine();
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Exception while writing solution to file (" + outputSolutionFile + ").");
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }

    private Map<Integer, List<TaskAssignment>> buildMachineTaskAssignmentMap(List<TaskAssignment> taskList) {
        Map<Integer, List<TaskAssignment>> machineTaskAssignmentMap = new HashMap<Integer, List<TaskAssignment>>();
        for (TaskAssignment taskAssignment : taskList) {
            if (machineTaskAssignmentMap.containsKey(taskAssignment.getExecutor().getId())) {
                machineTaskAssignmentMap.get(taskAssignment.getExecutor().getId()).add(taskAssignment);
            } else {
                List<TaskAssignment> taskAssignmentList = new ArrayList<TaskAssignment>();
                taskAssignmentList.add(taskAssignment);
                machineTaskAssignmentMap.put(taskAssignment.getExecutor().getId(), taskAssignmentList);
            }
        }
        return machineTaskAssignmentMap;
    }

    private Map<Integer, List<MachineOnOffEventHolder>> getMachineOnOffMapping(Map<Integer, List<TaskAssignment>> machineTaskMap) {
        Map<Integer, List<MachineOnOffEventHolder>> machineOnOffEventMap = new TreeMap<Integer, List<MachineOnOffEventHolder>>();
        TaskAssignmentStartingPeriodComparator comparator = new TaskAssignmentStartingPeriodComparator();
        for (Entry<Integer, List<TaskAssignment>> entry : machineTaskMap.entrySet()) {
            if (entry.getValue() == null && entry.getValue().isEmpty()) {
                continue;
            }
            machineOnOffEventMap.put(entry.getKey(), new ArrayList<MachineOnOffEventHolder>());
            // sort task assignments by starting period
            Collections.sort(entry.getValue(), comparator);

            TaskAssignment latestEndingTimeAssignment = null;
            Iterator<TaskAssignment> taskAssignmentIterator = entry.getValue().iterator();
            while (taskAssignmentIterator.hasNext()) {
                TaskAssignment taskAssignment = taskAssignmentIterator.next();
                if (latestEndingTimeAssignment != null && latestEndingTimeAssignment.getShutdownPossible()
                        && taskAssignment.getStartPeriod().getId() > latestEndingTimeAssignment.getFinalPeriod().getId() + 1) {
                    machineOnOffEventMap.get(entry.getKey()).add(new MachineOnOffEventHolder(false, latestEndingTimeAssignment.getFinalPeriod().getId()));
                    machineOnOffEventMap.get(entry.getKey()).add(new MachineOnOffEventHolder(true, taskAssignment.getStartPeriod().getId()));
                }
                // init
                if (latestEndingTimeAssignment == null) {
                    latestEndingTimeAssignment = taskAssignment;
                    machineOnOffEventMap.get(entry.getKey()).add(new MachineOnOffEventHolder(true, taskAssignment.getStartPeriod().getId()));
                }
                // set new latest-ending task
                if (taskAssignment.getFinalPeriod().getId() > latestEndingTimeAssignment.getFinalPeriod().getId()) {
                    latestEndingTimeAssignment = taskAssignment;
                }
                // prefer task that may lead to machine's shutdown
                if (taskAssignment.getFinalPeriod().getId() == latestEndingTimeAssignment.getFinalPeriod().getId() && taskAssignment.getShutdownPossible()) {
                    latestEndingTimeAssignment = taskAssignment;
                }
                // make sure machines are switched off
                if (!taskAssignmentIterator.hasNext()) {
                    if (taskAssignment.getFinalPeriod().getId() < latestEndingTimeAssignment.getFinalPeriod().getId()) {
                        machineOnOffEventMap.get(entry.getKey()).add(new MachineOnOffEventHolder(false, latestEndingTimeAssignment.getFinalPeriod().getId()));
                    } else {
                        machineOnOffEventMap.get(entry.getKey()).add(new MachineOnOffEventHolder(false, taskAssignment.getFinalPeriod().getId()));
                    }
                }
            }
        }
        return machineOnOffEventMap;
    }

    private static class MachineOnOffEventHolder {

        private boolean on;
        private int time;

        public MachineOnOffEventHolder(boolean on, int time) {
            this.on = on;
            this.time = time;
        }

        public boolean isOn() {
            return on;
        }

        public void setOn(boolean on) {
            this.on = on;
        }

        public int getTime() {
            return time;
        }

        public void setTime(int time) {
            this.time = time;
        }
    }

    private static class TaskAssignmentStartingPeriodComparator implements Comparator<TaskAssignment> {

        @Override
        public int compare(TaskAssignment o1, TaskAssignment o2) {
            return o1.getStartPeriod().getId() - o2.getStartPeriod().getId();
        }
    }

}
