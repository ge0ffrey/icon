package org.optaplanner.examples.icon;

import java.io.File;
import java.io.IOException;

import org.optaplanner.core.api.domain.solution.Solution;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.examples.icon.io.IconSolutionFileIO;

public class Main {

    private static final File INPUT = new File("data/icon/input");

    public static void main(final String... args) throws IOException {
        // read stuff
        final String problem = args[0];
        final File inputFolder = new File(Main.INPUT, problem);
        IconSolutionFileIO iconSolutionFileIO = new IconSolutionFileIO();
        final Solution solution = iconSolutionFileIO.read(inputFolder);
        // instantiate solver
        final SolverFactory solverFactory = SolverFactory.createFromXmlResource("org/optaplanner/examples/icon/solver/iconSolverConfig.xml");
        final Solver solver = solverFactory.buildSolver();
        solver.solve(solution);
    }

}
