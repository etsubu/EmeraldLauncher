package Launcher;

import javax.swing.*;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Formatter;
import java.util.Locale;

public class OutputWrapper extends PrintStream {
    private PrintStream stdout;
    private JTextArea area;

    public OutputWrapper(OutputStream out) {
        super(out);
        this.area = new JTextArea();
        this.stdout = System.out;
    }

    public JTextArea getArea() {
        return this.area;
    }

    @Override
    public void write(byte buf[], int off, int len) {
        this.area.append(new String(Arrays.copyOfRange(buf, off, len), StandardCharsets.UTF_8));
        stdout.write(buf, off, len);
    }

    @Override
    public void print(boolean b) {
        area.append(String.valueOf(b));
        stdout.print(b);
    }

    @Override
    public void print(char c) {
        area.append(String.valueOf(c));
        stdout.print(c);
    }

    @Override
    public void print(int i) {
        area.append(String.valueOf(i));
        stdout.print(i);
    }

    @Override
    public void print(long l) {
        area.append(String.valueOf(l));
        stdout.print(l);
    }

    @Override
    public void print(float f) {
        area.append(String.valueOf(f));
        stdout.print(f);
    }

    @Override
    public void print(double d) {
        area.append(String.valueOf(d));
        stdout.print(d);
    }

    @Override
    public void print(char s[]) {
        area.append(String.valueOf(s));
        stdout.print(s);
    }

    @Override
    public void print(String s) {
        area.append(String.valueOf(s));
        stdout.print(s);
    }

    @Override
    public void print(Object obj) {
        area.append(String.valueOf(obj));
        stdout.print(obj);
    }

    @Override
    public void println() {
        area.append("\n");
        stdout.println();
    }

    @Override
    public void println(boolean x) {
        synchronized (this) {
            area.append(String.valueOf(x) + "\n");
            stdout.println(x);
        }
    }

    @Override
    public void println(char x) {
        synchronized (this) {
            area.append(String.valueOf(x) + "\n");
            stdout.println(x);
        }
    }

    @Override
    public void println(int x) {
        synchronized (this) {
            area.append(String.valueOf(x) + "\n");
            stdout.println(x);
        }
    }

    @Override
    public void println(long x) {
        synchronized (this) {
            area.append(String.valueOf(x) + "\n");
            stdout.println(x);
        }
    }

    @Override
    public void println(float x) {
        synchronized (this) {
            area.append(String.valueOf(x) + "\n");
            stdout.println(x);
        }
    }

    @Override
    public void println(double x) {
        synchronized (this) {
            area.append(String.valueOf(x) + "\n");
            stdout.println(x);
        }
    }

    @Override
    public void println(char x[]) {
        synchronized (this) {
            area.append(String.valueOf(x) + "\n");
            stdout.println(x);
        }
    }

    @Override
    public void println(String x) {
        synchronized (this) {
            area.append(String.valueOf(x) + "\n");
            stdout.println(x);
        }
    }

    @Override
    public void println(Object x) {
        String s = String.valueOf(x);
        synchronized (this) {
            area.append(String.valueOf(x) + "\n");
            stdout.println(x);
        }
    }

    @Override
    public PrintStream append(CharSequence csq) {
        area.append(String.valueOf(csq));
        stdout.append(csq);
        return this;
    }

    @Override
    public PrintStream append(CharSequence csq, int start, int end) {
        if (csq == null) csq = "null";
        area.append(String.valueOf(csq.subSequence(start, end)));
        return stdout.append(csq);
    }

    @Override
    public PrintStream append(char c) {
        area.append(String.valueOf(c));
        stdout.append(c);
        return this;
    }
}
