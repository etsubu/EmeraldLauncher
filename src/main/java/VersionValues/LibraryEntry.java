package VersionValues;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class LibraryEntry {
    private static final Logger log = LoggerFactory.getLogger(LibraryEntry.class);
    private final LibraryDownloadEntry downloads;
    private final String name;
    private final List<LibraryRules> rules;

    public LibraryEntry(LibraryDownloadEntry downloads, String name, List<LibraryRules> rules) {
        this.downloads = downloads;
        this.name = name;
        this.rules = rules;
    }

    public LibraryDownloadEntry getDownloads() { return this.downloads; }

    public String getName() { return this.name; }

    public boolean doesBelongToOS() {
        if(rules == null) {
            return true;
        }
        if(name.contains("x86") && OperatingSystem.bitness.equals("64")) {
            //System.out.println("Skip 32 bit lib");
            return false;
        }
        if(name.contains("arm64") && !OperatingSystem.HOST_ARCH.contains("arm64")) {
            //System.out.println("Skip arm64 library");
            return false;
        }
        if(OperatingSystem.HOST_OS.contains("windows")) {
            return rules.stream().anyMatch(x -> x.getAction().equals("allow") && x.getOs().getName().equals("windows"));
        }
        else if(OperatingSystem.HOST_OS.contains("mac os") || OperatingSystem.HOST_OS.contains("macos") || OperatingSystem.HOST_OS.contains("darwin")) {
            return rules.stream().anyMatch(x -> x.getAction().equals("allow") && x.getOs().getName().equals("osx"));
        }
        else if(OperatingSystem.HOST_OS.contains("linux")) {
            return rules.stream().anyMatch(x -> x.getAction().equals("allow") && x.getOs().getName().equals("linux"));
        } else {
            log.error("Unknown OS {}", OperatingSystem.HOST_OS);
            return false;
        }
    }
}
