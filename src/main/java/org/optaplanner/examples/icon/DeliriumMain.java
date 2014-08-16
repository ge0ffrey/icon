/*
 * Copyright 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.examples.icon;

import java.io.File;
import java.util.Arrays;

import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import org.optaplanner.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.solver.termination.TerminationConfig;
import org.optaplanner.examples.cheaptime.domain.CheapTimeSolution;
import org.optaplanner.examples.cheaptime.persistence.CheapTimeSolutionFileIO;
import org.optaplanner.examples.icon.domain.Schedule;
import org.optaplanner.examples.icon.io.IconSolutionFileIO;

public class DeliriumMain {

    public static void main(String[] args) {
        if (args.length != 3) {
            throw new IllegalArgumentException("3 args expected: inputDir processIndex secondsSpentLimit\nGot: " + Arrays.toString(args));
        }
        File inputDir = new File(args[0]);
        if (!inputDir.exists()) {
            throw new IllegalArgumentException("Input file (" + inputDir + ") does not exist: " + inputDir.getAbsolutePath());
        }
        int processIndex;
        long secondsSpentLimit;
        try {
            processIndex = Integer.parseInt(args[1]);
            secondsSpentLimit = Long.parseLong(args[2]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("2 args expected: processIndex secondsSpentLimit", e);
        }
        if (processIndex % 2 == 0) {
            mainLukas(inputDir, processIndex, secondsSpentLimit);
        } else {
            mainGeoffrey(inputDir, processIndex, secondsSpentLimit);
        }
    }

    private static void mainLukas(File inputDir, int processIndex, long secondsSpentLimit) {
        Solver solver = buildDeliriumSolver("org/optaplanner/examples/icon/solver/iconSolverConfig.xml", processIndex, secondsSpentLimit);
        IconSolutionFileIO solutionFileIO = new IconSolutionFileIO();
        Schedule problem = (Schedule) solutionFileIO.read(inputDir);
        solver.solve(problem);
        Schedule bestSolution = (Schedule) solver.getBestSolution();
        HardSoftLongScore bestScore = bestSolution.getScore();
        File outputFile = determineOutputFile(inputDir, processIndex, bestScore.getHardScore(), bestScore.getSoftScore() / 100000000);
        solutionFileIO.write(bestSolution, outputFile);
    }
    private static void mainGeoffrey(File inputDir, int processIndex, long secondsSpentLimit) {
        Solver solver = buildDeliriumSolver("org/optaplanner/examples/cheaptime/solver/cheapTimeSolverConfig.xml", processIndex, secondsSpentLimit);
        CheapTimeSolutionFileIO solutionFileIO = new CheapTimeSolutionFileIO();
        CheapTimeSolution problem = (CheapTimeSolution) solutionFileIO.read(inputDir);
        solver.solve(problem);
        CheapTimeSolution bestSolution = (CheapTimeSolution) solver.getBestSolution();
        HardMediumSoftLongScore bestScore = bestSolution.getScore();
        File outputFile = determineOutputFile(inputDir, processIndex, bestScore.getHardScore(), bestScore.getMediumScore() / 100000000);
        solutionFileIO.write(bestSolution, outputFile);
    }

    private static Solver buildDeliriumSolver(String solverConfigResource, int processIndex, long secondsSpentLimit) {
        SolverFactory solverFactory = SolverFactory.createFromXmlResource(solverConfigResource);
        TerminationConfig terminationConfig = new TerminationConfig();
        terminationConfig.setSecondsSpentLimit(secondsSpentLimit);
        solverFactory.getSolverConfig().setTerminationConfig(terminationConfig);
        if (processIndex > 2) {
            solverFactory.getSolverConfig().setRandomSeed((long) (processIndex / 2));
        }
        return solverFactory.buildSolver();
    }

    private static File determineOutputFile(File inputDir, int processIndex, long hardScore, long softScore) {
        return new File(inputDir, "solution_"
                + String.format("%09d", - hardScore) + "_" + String.format("%010d", - softScore)
                +"_pid" + processIndex + ".txt");
    }


}
