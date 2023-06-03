package VersionValues;

import java.util.List;

public class VersionLibraries {
    private final List<LibraryEntry> libraries;

    public VersionLibraries(List<LibraryEntry> libraries) {
        this.libraries = libraries;
    }

    public List<LibraryEntry> getLibraries() { return this.libraries; }
}
