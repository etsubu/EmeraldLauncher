package Launcher;

import java.util.List;

public class VersionManifest {
    private final Latest latest;
    private final List<VersionEntry> versions;

    public VersionManifest(Latest latest, List<VersionEntry> versions) {
        this.latest = latest;
        this.versions = versions;
    }

    public List<VersionEntry> getVersions() { return this.versions; }
}

class Latest {
    private final String release;
    private final String snapshot;

    public Latest(String release, String snapshot) {
        this.release = release;
        this.snapshot = snapshot;
    }

    public String getRelease() { return release; }

    public String getSnapshot() { return snapshot; }
}