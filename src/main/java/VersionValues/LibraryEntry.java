package VersionValues;

import java.util.List;

public class LibraryEntry {
    private final LibraryDownloadEntry downloads;
    private final String name;

    public LibraryEntry(LibraryDownloadEntry downloads, String name) {
        this.downloads = downloads;
        this.name = name;
    }

    public LibraryDownloadEntry getDownloads() { return this.downloads; }

    public String getName() { return this.name; }
}
