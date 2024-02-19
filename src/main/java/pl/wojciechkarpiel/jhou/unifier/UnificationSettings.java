package pl.wojciechkarpiel.jhou.unifier;

import java.io.PrintStream;

public class UnificationSettings {

    private int maxIterations = SolutionIterator.UNLIMITED_ITERATIONS;
    private PrintStream printStream = SolutionIterator.DEFAULT_PRINT_STREAM;
    private AllowedTypeInference allowedTypeInference = AllowedTypeInference.PERMISSIVE;

    public int getMaxIterations() {
        return maxIterations;
    }

    public void setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
    }

    public PrintStream getPrintStream() {
        return printStream;
    }

    public void setPrintStream(PrintStream printStream) {
        this.printStream = printStream;
    }

    public AllowedTypeInference getAllowedTypeInference() {
        return allowedTypeInference;
    }

    public void setAllowedTypeInference(AllowedTypeInference allowedTypeInference) {
        this.allowedTypeInference = allowedTypeInference;
    }
}
