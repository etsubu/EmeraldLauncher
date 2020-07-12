package VersionValues;

public class GameDownloads {
    private final GameBinary client;
    private final GameBinary server;
    private final String id;

    public GameDownloads(GameBinary client, GameBinary server, String id) {
        this.client = client;
        this.server = server;
        this.id = id;
    }

    public GameBinary getClient() { return this.client; }

    public GameBinary getServer() { return this.server; }
}
