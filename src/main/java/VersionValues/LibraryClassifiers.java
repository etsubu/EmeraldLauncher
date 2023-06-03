package VersionValues;

import com.google.gson.annotations.SerializedName;

import java.util.Optional;

public class LibraryClassifiers {
    @SerializedName("javadoc")
    private final LibraryArtifact javadoc;
    @SerializedName("natives-linux")
    private final LibraryArtifact nativesLinux;
    @SerializedName("natives-macos")
    private final LibraryArtifact nativesMacos;
    @SerializedName("natives-windows")
    private final LibraryArtifact nativesWindows;
    @SerializedName("sources")
    private final LibraryArtifact sources;

    public LibraryClassifiers(LibraryArtifact javadoc, LibraryArtifact nativesLinux, LibraryArtifact nativesMacos,
                              LibraryArtifact nativesWindows, LibraryArtifact sources) {
        this.javadoc = javadoc;
        this.nativesLinux = nativesLinux;
        this.nativesMacos = nativesMacos;
        this.nativesWindows = nativesWindows;
        this.sources = sources;
    }
}
