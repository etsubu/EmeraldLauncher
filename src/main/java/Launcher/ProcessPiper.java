package Launcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ProcessPiper implements Runnable {
    private static Logger log = LoggerFactory.getLogger(ProcessPiper.class);
    private OutputWrapper wrapper;
    private InputStream stream;
    private Launcher callback;

    public ProcessPiper(OutputWrapper wrapper, InputStream stream, Launcher callback) {
        this.wrapper = wrapper;
        this.stream = stream;
        this.callback = callback;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[128];
        int len;
        try {
            while ((len = stream.read(buffer)) >= 0) {
                String content = new String(Arrays.copyOf(buffer, len), StandardCharsets.UTF_8);
                wrapper.print(content);
            }
        } catch (IOException e) {
            log.error("Failed to read game output from stream " , e);
        }
        log.info("Game exited!");
    }
}
