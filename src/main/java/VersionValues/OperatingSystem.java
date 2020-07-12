package VersionValues;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

public class OperatingSystem {
    private static final Logger log = LoggerFactory.getLogger(OperatingSystem.class);
    public static final String HOST_OS = System.getProperty("os.name").toLowerCase();
    private static final String HOST_OS_VERSION = System.getProperty("os.version").toLowerCase();
    private static final String HOST_ARCH = System.getProperty("os.arch").toLowerCase();
    private final String name;
    private final String version;
    private final String arch;

    public OperatingSystem(String name, String version, String arch) {
        this.name = name;
        this.version = version;
        this.arch = arch;
    }

    private boolean checkOs() {
        if(arch != null && !arch.equalsIgnoreCase(HOST_ARCH)) {
            return false;
        }
        if(name == null || name.isEmpty())
            return true;
        if(name.equalsIgnoreCase("windows")) {
            if(HOST_OS.contains("windows")) {
                return true;
            }
            return false;
        } else if(name.equalsIgnoreCase("osx")) {
            if(HOST_OS.contains("mac os") || HOST_OS.contains("macos") || HOST_OS.contains("darwin")) {
                return true;
            }
            return false;
        } else if(name.equalsIgnoreCase("linux")) {
            if(HOST_OS.contains("linux")) {
                return true;
            }
            return false;
        }
        log.warn("Did not resolve host os: " + HOST_OS);
        return false;
    }

    public boolean doesApply() {
        if(checkOs()) {
            if(version != null && !version.isEmpty()) {
                boolean b = Pattern.matches(version, HOST_OS_VERSION);
                if(b) {
                    log.info("Matched host os version specific rule");
                }
                return b;
            }
            return true;
        }
        return false;
    }

    public String getName() { return name; }

    public String getVersion() { return version; }
}
