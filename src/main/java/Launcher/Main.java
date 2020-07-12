package Launcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    private static final OutputWrapper wrapper;
    private static final Logger log;

    static {
        wrapper = new OutputWrapper(System.out);
        System.setOut(wrapper);
        System.setErr(wrapper);
        log = LoggerFactory.getLogger(Main.class);
    }

    public static void printSystemInfo() {
        log.info("Starting up...");
        log.info("java.version = " + System.getProperty("java.version"));
        log.info("java.class.version = " + System.getProperty("java.class.version"));
        log.info("OS=" + System.getProperty("os.name") + " " + System.getProperty("os.version")  + " " +System.getProperty("os.arch"));
    }

    public static void main(String[] args) {
        printSystemInfo();
        Path path = Paths.get(System.getProperty("user.dir"),".minecraft");
        if (!path.toFile().exists()) {
            log.info("Minecraft folder does not exist. Creating: " + path.toString());
            if (!path.toFile().mkdir()) {
                log.error("Failed to create .minecraft folder. Maybe we lack permissions?");
                JOptionPane.showMessageDialog(null, "Failed to create .minecraft folder probably due to insufficient permissions." +
                        " Try changing the launcher location or run as administrator", "Failed to create .minecraft folder", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
            log.info("Created .minecraft folder");
        }
        javax.swing.SwingUtilities.invokeLater(() -> new Launcher(path, wrapper));
    }
}
