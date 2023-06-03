package VersionValues;

import java.util.Optional;

public class LibraryDownloadEntry {
    private final LibraryArtifact artifact;

    public LibraryDownloadEntry(LibraryArtifact artifact) {
        this.artifact = artifact;
    }

    public LibraryArtifact getArtifact() { return this.artifact; }

}
