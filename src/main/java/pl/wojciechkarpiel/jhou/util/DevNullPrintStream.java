package pl.wojciechkarpiel.jhou.util;

import java.io.OutputStream;
import java.io.PrintStream;

public class DevNullPrintStream extends PrintStream {
    public static final DevNullPrintStream INSTANCE = new DevNullPrintStream();

    private DevNullPrintStream() {
        super(new OutputStream() {
            @Override
            public void write(int b) {
            }
        });
    }

    @Override
    public void println() {
    }

    @Override
    public void println(String x) {
    }

    @Override
    public void print(String x) {
    }

    @Override
    public void println(Object o) {
    }

    @Override
    public void print(Object o) {
    }
}
