package VersionValues;

import java.util.List;

public class GameVersionEntry {
    private final GameArguments arguments;
    private final AssetIndex assetIndex;
    private final String assets;
    private final GameDownloads downloads;
    private final List<LibraryEntry> libraries;
    private final Object logging;
    private final String mainClass;
    private final String minimumLauncherVersion;
    private final String releaseTime;
    private final String time;
    private final String type;

    public GameVersionEntry(GameArguments arguments, AssetIndex assetIndex, String assets, GameDownloads downloads, List<LibraryEntry> libraries,
                            Object logging, String mainClass, String minimumLauncherVersion, String releaseTime, String time, String type) {
        this.arguments = arguments;
        this.assetIndex = assetIndex;
        this.assets = assets;
        this.downloads = downloads;
        this.libraries = libraries;
        this.logging = logging;
        this.mainClass = mainClass;
        this.minimumLauncherVersion = minimumLauncherVersion;
        this.releaseTime = releaseTime;
        this.time = time;
        this.type = type;
    }

    public GameArguments getArguments() { return arguments; }

    public AssetIndex getAssetIndex() { return assetIndex; }

    public String getAssets() { return assets; }

    public GameDownloads getDownloads() { return this.downloads; }

    public List<LibraryEntry> getLibraries() { return this.libraries; }

    public Object getLogging() { return this.logging; }

    public String getMainClass() { return this.mainClass; }

    public String getMinimumLauncherVersion() { return this.minimumLauncherVersion; }

    public String getReleaseTime() { return this.releaseTime; }

    public String getTime() { return this.time; }

    public String getType() { return this.type; }
}
