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

    public Optional<LibraryArtifact> getJavadoc() { return Optional.ofNullable(javadoc); }

    public Optional<LibraryArtifact> getNativesLinux() { return Optional.ofNullable(nativesLinux); }

    public Optional<LibraryArtifact> getNativesMacos() { return Optional.ofNullable(nativesMacos); }

    public Optional<LibraryArtifact> getNativesWindows() { return Optional.ofNullable(nativesWindows); }

    public Optional<LibraryArtifact> getSources() { return Optional.ofNullable(sources); }

    public Optional<LibraryArtifact> getNativesHost() {
        if(OperatingSystem.HOST_OS.contains("windows")) {
            return getNativesWindows();
        }
         else if(OperatingSystem.HOST_OS.contains("mac os") || OperatingSystem.HOST_OS.contains("macos") || OperatingSystem.HOST_OS.contains("darwin")) {
            return getNativesMacos();
        }
        else if(OperatingSystem.HOST_OS.contains("linux")) {
            return getNativesLinux();
        }
        return Optional.empty();
    }
}
