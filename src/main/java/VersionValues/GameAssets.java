package VersionValues;

import java.util.Map;

public class GameAssets {
    private final Map<String, AssetEntry> objects;

    public GameAssets(Map<String, AssetEntry> objects) {
        this.objects = objects;
    }

    public Map<String, AssetEntry> getObjects() { return objects; }
}
