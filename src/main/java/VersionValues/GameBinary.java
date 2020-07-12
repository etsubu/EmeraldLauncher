package VersionValues;

public class GameBinary {
    private String sha1;
    private int size;
    private String url;

    public GameBinary(String sha1, int size, String url) {
        this.sha1 = sha1;
        this.size = size;
        this.url = url;
    }

    public String getSha1() { return this.sha1; }

    public int getSize() { return this.size; }

    public String getUrl() { return this.url; }
}
