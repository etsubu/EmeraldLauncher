package VersionValues;

import java.util.Optional;

public class LibraryDownloadEntry {
    private final LibraryArtifact artifact;
    private LibraryClassifiers classifiers;

    public LibraryDownloadEntry(LibraryArtifact artifact, LibraryClassifiers classifiers) {
        this.artifact = artifact;
        this.classifiers = classifiers;
    }

    public LibraryArtifact getArtifact() { return this.artifact; }

    public Optional<LibraryClassifiers> getClassifiers() { return Optional.ofNullable(classifiers); }
}
