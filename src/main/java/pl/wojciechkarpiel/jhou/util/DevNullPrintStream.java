package pl.wojciechkarpiel.jhou.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class DevNullPrintStream extends PrintStream {
    public static final DevNullPrintStream INSTANCE = new DevNullPrintStream();

    private DevNullPrintStream() {
        super(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
            }
        });
    }
}
