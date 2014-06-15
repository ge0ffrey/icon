package org.optaplanner.examples.icon;

import java.io.File;
import java.io.IOException;

import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.examples.icon.domain.Schedule;
import org.optaplanner.examples.icon.parser.ProblemParser;

public class Main {

    private static final File INPUT = new File("data/icon/input");

    public static void main(final String... args) throws IOException {
        // read stuff
        final String problem = args[0];
        final File folder = new File(Main.INPUT, problem);
        final Schedule schedule = ProblemParser.parse(new File(folder, "forecast.txt"), new File(folder, "instance.txt"));
        // instantiate solver
        final SolverFactory f = SolverFactory.createFromXmlResource("org/optaplanner/examples/icon/solver/iconSolverConfig.xml");
        final Solver solver = f.buildSolver();
        solver.solve(schedule);
    }

}
