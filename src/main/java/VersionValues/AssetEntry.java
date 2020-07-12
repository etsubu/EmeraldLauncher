package VersionValues;

public class AssetEntry {
    private final String hash;
    private final int size;

    public AssetEntry(String hash, int size) {
        this.hash = hash;
        this.size = size;
    }

    public String getHash() { return hash; }

    public int getSize() { return size; }
}
