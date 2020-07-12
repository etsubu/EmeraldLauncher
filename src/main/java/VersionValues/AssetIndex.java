package VersionValues;

public class AssetIndex {
    private final String id;
    private final String sha1;
    private final int size;
    private final int totalSize;
    private final String url;

    public AssetIndex(String id, String sha1, int size, int totalSize, String url) {
        this.id = id;
        this.sha1 = sha1;
        this.size = size;
        this.totalSize = totalSize;
        this.url = url;
    }

    public String getId() { return this.id; }

    public String getSha1() { return this.sha1; }

    public int getSize() { return this.size; }

    public int getTotalSize() { return this.totalSize; }

    public String getUrl() { return this.url; }
}
