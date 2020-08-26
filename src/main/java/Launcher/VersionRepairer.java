package Launcher;

import VersionValues.*;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.channels.OverlappingFileLockException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class VersionRepairer implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(VersionRepairer.class);
    private MessageDigest digest;
    private final JProgressBar progressBar;
    private final JProgressBar totalProgressBar;
    private final Path workingDir;
    private Thread instance;
    private VersionEntry entry;
    private HttpApi httpApi;
    private int totalSizeDownload;
    private int currentSizeDownload;
    private Gson gson;

    public VersionRepairer(JProgressBar progressBar, JProgressBar totalProgressBar, Path workingDir) {
        try {
            this.digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            log.error("Failed to get SHA-1 instance ", e);
        }
        this.progressBar = progressBar;
        this.totalProgressBar = totalProgressBar;
        this.workingDir = workingDir;
        this.gson = new Gson();
        this.httpApi = new HttpApi();
    }

    public void repair(VersionEntry entry) {
        if(instance != null && instance.isAlive()) {
            log.info("Repair is already in progress");
        } else {
            this.entry = entry;
            this.currentSizeDownload = 0;
            this.totalSizeDownload = 0;
            this.progressBar.setValue(0);
            this.totalProgressBar.setValue(0);
            this.instance = new Thread(this);
            log.info("Starting repair " + entry);
            this.instance.start();
        }
    }

    private boolean calculateDownloadSize(GameVersionEntry version) throws IOException, InterruptedException {
        Path installPath = workingDir.resolve(Paths.get("assets"));
        int size = version.getAssetIndex().getSize();
        String url = version.getAssetIndex().getUrl();
        Path assetPath = installPath.resolve(Paths.get("indexes", version.getAssetIndex().getId() + ".json"));
        if(!httpApi.downloadToFile(url, assetPath, progressBar, size)) {
            log.error("Failed to download asset indexes info");
            return false;
        }
        totalSizeDownload += size;
        String assetIndexJson = new String(Files.readAllBytes(assetPath), StandardCharsets.UTF_8);
        GameAssets assets = gson.fromJson(assetIndexJson, GameAssets.class);
        for(String key : assets.getObjects().keySet()) {
            totalSizeDownload += assets.getObjects().get(key).getSize();
        }
        for(LibraryEntry entry : version.getLibraries()) {
            totalSizeDownload += entry.getDownloads().getArtifact().getSize();
            entry.getDownloads().getClassifiers().flatMap(LibraryClassifiers::getNativesHost).ifPresent(y -> totalSizeDownload += y.getSize());
        }
        totalSizeDownload += version.getDownloads().getClient().getSize();
        // assets
        progressBar.setValue(0);
        totalProgressBar.setValue(0);
        totalProgressBar.setMaximum(totalSizeDownload);
        return true;
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    private boolean doesChecksumMatch(Path file, String checksum, int size) {
        File f = file.toFile();
        if(!f.exists() || size != f.length()) {
            return false;
        }
        try {
            return Arrays.equals(digest.digest(Files.readAllBytes(file)), hexStringToByteArray(checksum));
        } catch (IOException e) {
            log.error("Failed to read file " + file.toString() + " ", e);
        }
        return true;
    }

    private boolean checkClient(GameVersionEntry version) throws IOException, InterruptedException {
        log.info("Checking game client " + entry.getId());
        Path installPath = workingDir.resolve(Paths.get("versions", entry.getId()));
        String url = version.getDownloads().getClient().getUrl();
        int size = version.getDownloads().getClient().getSize();
        String sha1 = version.getDownloads().getClient().getSha1();
        Path clientPath = installPath.resolve(Paths.get("client.jar"));
        if(!doesChecksumMatch(clientPath, sha1, size)) {
            log.info("Corrupted: " + clientPath.toString());
            if(!httpApi.downloadToFile(url, installPath.resolve(Paths.get("client.jar")), progressBar, size, true)) {
                log.error("Failed to download game client");
                return false;
            }
        }
        this.currentSizeDownload += size;
        this.progressBar.setValue(this.progressBar.getMaximum());
        this.totalProgressBar.setValue(this.currentSizeDownload);
        return true;
    }

    private void updateProgress(int size) {
        currentSizeDownload += size;
        totalProgressBar.setValue(currentSizeDownload);
    }

    private boolean checkLibraries(GameVersionEntry version) throws IOException, InterruptedException {
        log.info("Checking libraries for " + entry.getId());
        Path installPath = workingDir.resolve(Paths.get("libraries"));
        List<LibraryEntry> libraries = version.getLibraries();
        if(libraries == null || libraries.isEmpty()) {
            log.error("Could not parse libraries");
            return false;
        }
        for (LibraryEntry library : libraries) {
            //log.info("Downloading library: " + (i + 1) + "/" + libraries.size());
            try {
                String path = library.getDownloads().getArtifact().getPath();
                String url = library.getDownloads().getArtifact().getUrl();
                String sha1 = library.getDownloads().getArtifact().getSha1();
                int size = library.getDownloads().getArtifact().getSize();
                Path libraryPath = installPath.resolve(path);
                if (!libraryPath.getParent().toFile().exists()) {
                    if (!libraryPath.getParent().toFile().mkdirs()) {
                        log.error("Failed to create folder: " + libraryPath.getParent().toString());
                        return false;
                    }
                }
                if (!doesChecksumMatch(libraryPath, sha1, size)) {
                    log.info("Corrupted file: " + libraryPath.toString());
                    if (!httpApi.downloadToFile(url, libraryPath, progressBar, size, true)) {
                        log.error("Failed to download library: " + url);
                        return false;
                    }
                }
                updateProgress(size);
                if (library.getDownloads().getClassifiers().isPresent()) {
                    Optional<LibraryArtifact> nativeArtifact = library.getDownloads().getClassifiers().get().getNativesHost();
                    if (nativeArtifact.isPresent()) {
                        path = nativeArtifact.get().getPath();
                        url = nativeArtifact.get().getUrl();
                        sha1 = nativeArtifact.get().getSha1();
                        size = nativeArtifact.get().getSize();
                        libraryPath = installPath.resolve(path);
                        if (!libraryPath.getParent().toFile().exists()) {
                            if (!libraryPath.getParent().toFile().mkdirs()) {
                                log.error("Failed to create folder: " + libraryPath.getParent().toString());
                                return false;
                            }
                        }
                        //log.info("Downloading native library: " + name);
                        if (!doesChecksumMatch(libraryPath, sha1, size)) {
                            log.info("Corrupted file: " + libraryPath.toString());
                            if (!httpApi.downloadToFile(url, libraryPath, progressBar, size, true)) {
                                log.error("Failed to download library: " + url);
                                return false;
                            }
                        }
                        updateProgress(size);
                    }
                }
            } catch (NullPointerException e) {
                log.error("Failed to parse library entry: " + e.getMessage());
                return false;
            }
        }
        return true;
    }

    private boolean checkAssets(GameVersionEntry version) throws IOException, InterruptedException {
        log.info("Checking assets for " + entry.getId());
        Path installPath = workingDir.resolve(Paths.get("assets"));
        int size = version.getAssetIndex().getSize();
        String url = version.getAssetIndex().getUrl();
        Path assetPath = installPath.resolve(Paths.get("indexes", version.getAssetIndex().getId() + ".json"));
        if(!httpApi.downloadToFile(url, assetPath, progressBar, size, true)) {
            log.error("Failed to download asset indexes info");
            return false;
        }
        String assetIndexJson = Files.readString(assetPath);
        GameAssets assets = gson.fromJson(assetIndexJson, GameAssets.class);
        Path assetObjects = installPath.resolve("objects");
        int counter = 0;
        var objects = assets.getObjects();
        updateProgress(size);
        for(AssetEntry asset : objects.values()) {
            //log.info("Downloading asset " + counter + " / " + assets.getObjects().size());
            String hash = asset.getHash();
            int assetSize = asset.getSize();
            String assetUrl = hash.substring(0, 2) + "/" + hash;
            Path assetObjectPath = assetObjects.resolve(assetUrl);
            if(!doesChecksumMatch(assetObjectPath, hash, assetSize)) {
                log.info("Corrupted file: " + assetObjectPath.toString());
                url = VersionLauncher.RESOURCES_BASE_URL + assetUrl;
                if (!httpApi.downloadToFile(url, assetObjectPath, progressBar, asset.getSize(), true)) {
                    log.error("Failed to download asset indexes info");
                    return false;
                }
            }
            updateProgress(assetSize);
            counter++;
        }
        return true;
    }

    @Override
    public void run() {
        try {
            String versionJson = httpApi.sendGet(entry.getDownloadUrl());
            GameVersionEntry version = gson.fromJson(versionJson, GameVersionEntry.class);
            calculateDownloadSize(version);
            if(!checkClient(version) || !checkLibraries(version) || !checkAssets(version)) {
                log.error("Repair failed");
            } else {
                log.info("Repair completed");
            }
            this.totalProgressBar.setValue(this.totalProgressBar.getMaximum());
            this.progressBar.setValue(this.progressBar.getMaximum());
        } catch (IOException | InterruptedException e) {
            log.error("Failed to request version metadata " + e);
            this.totalProgressBar.setValue(this.totalProgressBar.getMaximum());
            this.progressBar.setValue(this.progressBar.getMaximum());
        }
    }
}
