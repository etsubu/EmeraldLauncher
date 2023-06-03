package VersionValues;

public class LibraryArtifact {
    private final String path;
    private final String sha1;
    private final int size;
    private final String url;

    public LibraryArtifact(String path, String sha1, int size, String url) {
        this.path = path;
        this.sha1 = sha1;
        this.size = size;
        this.url = url;
    }

    public String getPath() { return this.path; }

    public String getSha1() { return this.sha1; }

    public int getSize() { return this.size; }

    public String getUrl() { return this.url; }
}
