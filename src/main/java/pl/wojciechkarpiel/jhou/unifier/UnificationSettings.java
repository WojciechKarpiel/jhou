package pl.wojciechkarpiel.jhou.unifier;

import java.io.PrintStream;

public class UnificationSettings {

    private int maxIterations = SolutionIterator.UNLIMITED_ITERATIONS;
    private PrintStream printStream = SolutionIterator.DEFAULT_PRINT_STREAM;
    private AllowedTypeInference allowedTypeInference = AllowedTypeInference.PERMISSIVE;

    /**
     * the default settings
     */
    public UnificationSettings() {
    }

    public UnificationSettings(AllowedTypeInference allowedTypeInference) {
        this.allowedTypeInference = allowedTypeInference;
    }

    public UnificationSettings(int maxIterations) {
        this.maxIterations = maxIterations;
    }

    public UnificationSettings(AllowedTypeInference allowedTypeInference, int maxIterations) {
        this.allowedTypeInference = allowedTypeInference;
        this.maxIterations = maxIterations;
    }

    public UnificationSettings(AllowedTypeInference allowedTypeInference, int maxIterations, PrintStream printStream) {
        this.allowedTypeInference = allowedTypeInference;
        this.maxIterations = maxIterations;
        this.printStream = printStream;
    }

    public int getMaxIterations() {
        return maxIterations;
    }

    public PrintStream getPrintStream() {
        return printStream;
    }
    public AllowedTypeInference getAllowedTypeInference() {
        return allowedTypeInference;
    }
}
