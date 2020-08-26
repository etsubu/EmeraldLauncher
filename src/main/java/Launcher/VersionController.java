package Launcher;

import VersionValues.GameVersionEntry;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class VersionController {
    private static final Logger log = LoggerFactory.getLogger(VersionController.class);
    private static final String VERSION_MANIFEST_URL = "https://launchermeta.mojang.com/mc/game/version_manifest.json";
    private VersionManifest versionManifest;
    private Path workingDir;
    private Gson gson;
    private HttpApi httpApi;

    public VersionController(Path workingDir) {
        this.workingDir = workingDir;
        this.httpApi = new HttpApi();
        this.gson = new Gson();
    }

    public boolean loadVersionList() {
        String rawJson;
        try {
            rawJson = httpApi.sendGet(VERSION_MANIFEST_URL);
            try {
                this.versionManifest = this.gson.fromJson(rawJson, VersionManifest.class);
                return true;
            } catch(JsonSyntaxException e) {
                log.error("Failed to parse version manifest json: ", e);
                return false;
            }
        } catch (IOException e) {
            log.error("Failed to load list of minecraft versions! No network connection available ");
        } catch (InterruptedException e) {
            log.error("Failed to load list of minecraft versions! HTTP error ", e);
        }
        // If no connection available the use existing ones
        return loadVersionListFromDisk();
    }

    public boolean loadVersionListFromDisk() {
        List<VersionEntry> entries = new LinkedList<>();
        Path versions = workingDir.resolve("versions");
        File folder = versions.toFile();
        if(folder.exists()) {
            File[] versionFolders = folder.listFiles();
            if(versionFolders != null) {
                for (File gameFolder : versionFolders) {
                    File gameInfo = versions.resolve(Paths.get(gameFolder.getName(), gameFolder.getName() + ".json")).toFile();
                    if(gameInfo.exists()) {
                        GameVersionEntry version;
                        try {
                            version = gson.fromJson(Files.readString(gameInfo.toPath()), GameVersionEntry.class);
                            entries.add(new VersionEntry(gameFolder.getName(), null, version.getType(), version.getTime(), version.getReleaseTime()));
                        } catch (IOException e) {
                            //
                        }
                    }
                }
            }
        }
        if(!entries.isEmpty()) {
            this.versionManifest = new VersionManifest(null, entries);
            return true;
        }
        return false;
    }

    public List<VersionEntry> getVersionEntries() {
        if(versionManifest == null) {
            return new LinkedList<>();
        }
        return this.versionManifest.getVersions();
    }
}
