package Launcher;

import java.util.List;

public class VersionManifest {
    private Latest latest;
    private List<VersionEntry> versions;

    public VersionManifest(Latest latest, List<VersionEntry> versions) {
        this.latest = latest;
        this.versions = versions;
    }

    public List<VersionEntry> getVersions() { return this.versions; }
}

class Latest {
    private String release;
    private String snapshot;

    public Latest(String release, String snapshot) {
        this.release = release;
        this.snapshot = snapshot;
    }
}