package Launcher;

import VersionValues.*;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class VersionLauncher implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(VersionLauncher.class);
    public static final String RESOURCES_BASE_URL = "https://resources.download.minecraft.net/";
    private Map<String, String> argumentMap;
    private Gson gson;
    private JProgressBar progressBar;
    private JProgressBar totalProgressBar;
    private Path workingDir;
    private VersionEntry entry;
    private Thread instance;
    private String name;
    private OutputWrapper wrapper;
    private int totalSizeDownload;
    private int currentSizeDownload;
    private Launcher launcher;
    private String jvmArgs;
    private HttpApi httpApi;

    public VersionLauncher(JProgressBar progressBar, JProgressBar totalProgressBar, Path workingDir, OutputWrapper wrapper, Launcher launcher) {
        this.progressBar = progressBar;
        this.totalProgressBar = totalProgressBar;
        this.workingDir = workingDir;
        this.gson = new Gson();
        this.entry = null;
        this.wrapper = wrapper;
        this.launcher = launcher;
        this.httpApi = new HttpApi();
        this.instance = new Thread(this);
    }

    private void initArgumentMap(String name, VersionEntry versionEntry) {
        this.argumentMap = new HashMap<>();
        this.argumentMap.put("${auth_player_name}", name);
        this.argumentMap.put("${version_name}", versionEntry.getId());
        this.argumentMap.put("${game_directory}", workingDir.toString());
        this.argumentMap.put("${assets_root}", workingDir.resolve("assets").toString());
        this.argumentMap.put("${assets_index_name}", "");
        this.argumentMap.put("${auth_uuid}", "5bd9ffdfcdc33708b707fb261455719e");
        this.argumentMap.put("${auth_access_token}", "1337535510N");
        this.argumentMap.put("${user_type}", "legacy");
        this.argumentMap.put("${version_type}", versionEntry.getType());
        this.argumentMap.put("${launcher_name}", "EmeraldLauncher");
        this.argumentMap.put("${launcher_version}", "1.0");
    }

    @Override
    public void run() {
        try {
            if (doesVersionExist()) {
                log.info("Version: " + entry.getId() + " exists already. Starting up..");
                startClient();
            } else {
                log.info("Version: {} does not exist locally. Downloading... {}", entry.getId(), entry.getDownloadUrl());
                String versionJson = httpApi.sendGet(entry.getDownloadUrl());
                System.out.println(entry.getDownloadUrl());
                GameVersionEntry version = gson.fromJson(versionJson, GameVersionEntry.class);
                //filterLibraries(version);
                log.info("Downloaded game version information");
                if (!calculateDownloadSize(version)) {
                    log.error("Failed to query game file meta data");
                    return;
                }
                if (installGame(versionJson, version)) {
                    if (installLibraries(version)) {
                        if (downloadAssets(version)) {
                            if (startClient()) {
                                log.info("Minecraft " + entry.getId() + " " + entry.getType() + " Launched. Enjoy the games!");
                            } else {
                                log.error("Failed to start game client");
                            }
                        } else {
                            log.error("Failed to download game assets");
                        }
                    } else {
                        log.error("Failed to install game libraries");
                    }
                } else {
                    log.error("Failed to install game client");
                }
            }
        } catch (IOException e) {
            log.error("Failed to get version: " + entry.getId() + " json information", e);
            e.printStackTrace();
        } catch (JsonSyntaxException e) {
            log.error("Failed to parse version: " + entry.getId() + " json syntax", e);
            e.printStackTrace();
        } catch (InterruptedException e) {
            log.error("HTTP error: ", e);
            e.printStackTrace();
        }
        log.info("Done");
    }

    private boolean calculateDownloadSize(GameVersionEntry version) throws IOException, InterruptedException {
        Path installPath = workingDir.resolve(Paths.get("assets"));
        int size = version.getAssetIndex().getSize();
        String url = version.getAssetIndex().getUrl();
        Path assetPath = installPath.resolve(Paths.get("indexes", version.getAssetIndex().getId() + ".json"));
        if (!httpApi.downloadToFile(url, assetPath, progressBar, size)) {
            log.error("Failed to download asset indexes info");
            return false;
        }
        totalSizeDownload += size;
        String assetIndexJson = new String(Files.readAllBytes(assetPath), StandardCharsets.UTF_8);
        GameAssets assets = gson.fromJson(assetIndexJson, GameAssets.class);
        for (String key : assets.getObjects().keySet()) {
            totalSizeDownload += assets.getObjects().get(key).getSize();
        }
        for (LibraryEntry entry : version.getLibraries()) {
            totalSizeDownload += entry.getDownloads().getArtifact().getSize();
            //entry.getDownloads().getClassifiers().flatMap(LibraryClassifiers::getNativesHost).ifPresent(y -> totalSizeDownload += y.getSize());
        }
        totalSizeDownload += version.getDownloads().getClient().getSize();
        progressBar.setValue(0);
        totalProgressBar.setValue(0);
        totalProgressBar.setMaximum(totalSizeDownload);
        return true;
    }

    private void updateProgress(int size) {
        currentSizeDownload += size;
        totalProgressBar.setValue(currentSizeDownload);
    }

    private boolean doesVersionExist() {
        if (entry == null) {
            return false;
        }
        return workingDir.resolve(Paths.get("versions", entry.getId())).toFile().exists();
    }

    private boolean installGame(String json, GameVersionEntry version) throws IOException, InterruptedException {
        log.info("Installing game client: {}", entry.getId());
        Path installPath = workingDir.resolve(Paths.get("versions", entry.getId()));
        if (!installPath.toFile().exists() && !installPath.toFile().mkdirs()) {
            log.error("Failed to create folder: {}", installPath);
            return false;
        }
        Files.write(installPath.resolve(Paths.get(entry.getId() + ".json")), json.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
        return downloadGame(installPath, version);
    }

    private boolean downloadAssets(GameVersionEntry version) throws IOException, InterruptedException {
        log.info("Installing assets for {}", entry.getId());
        Path installPath = workingDir.resolve(Paths.get("assets"));
        int size = version.getAssetIndex().getSize();
        String url = version.getAssetIndex().getUrl();
        Path assetPath = installPath.resolve(Paths.get("indexes", version.getAssetIndex().getId() + ".json"));
        if (!httpApi.downloadToFile(url, assetPath, progressBar, size)) {
            log.error("Failed to download asset indexes info");
            return false;
        }
        updateProgress(size);
        String assetIndexJson = Files.readString(assetPath);
        GameAssets assets = gson.fromJson(assetIndexJson, GameAssets.class);
        Path assetObjects = installPath.resolve("objects");
        var objects = assets.getObjects();
        for (AssetEntry asset : objects.values()) {
            //log.info("Downloading asset " + counter + " / " + assets.getObjects().size());
            String hash = asset.getHash();
            int assetSize = asset.getSize();
            String assetUrl = hash.substring(0, 2) + "/" + hash;
            Path assetObjectPath = assetObjects.resolve(assetUrl);
            url = RESOURCES_BASE_URL + assetUrl;
            if (!httpApi.downloadToFile(url, assetObjectPath, progressBar, asset.getSize())) {
                log.error("Failed to download asset indexes info");
                return false;
            }
            updateProgress(assetSize);
        }
        return true;
    }

    private boolean installLibraries(GameVersionEntry version) throws IOException, InterruptedException {
        log.info("Installing libraries for {}", entry.getId());
        Path installPath = workingDir.resolve(Paths.get("libraries"));

        if (!installPath.toFile().exists() && !installPath.toFile().mkdirs()) {
            log.error("Failed to create folder: {}", installPath);
            return false;
        }
        List<LibraryEntry> libraries = version.getLibraries();
        if (libraries == null || libraries.isEmpty()) {
            log.error("Could not parse libraries");
            return false;
        }
        for (LibraryEntry library : libraries) {
            try {
                if (library.doesBelongToOS()) {
                    var artifact = library.getDownloads().getArtifact();
                    var path = artifact.getPath();
                    var url = artifact.getUrl();
                    var size = artifact.getSize();
                    var libraryPath = installPath.resolve(path);
                    if (!libraryPath.getParent().toFile().exists()) {
                        if (!libraryPath.getParent().toFile().mkdirs()) {
                            log.error("Failed to create folder: " + libraryPath.getParent().toString());
                            return false;
                        }
                    }
                    if (!httpApi.downloadToFile(url, libraryPath, progressBar, size)) {
                        log.error("Failed to download library: " + url);
                        return false;
                    }
                    updateProgress(size);
                }
            } catch (NullPointerException e) {
                log.error("Failed to parse library entry: " + e.getMessage());
                return false;
            }
        }
        return true;
    }

    private boolean downloadGame(Path versionPath, GameVersionEntry entry) throws IOException, InterruptedException {
        try {
            String url = entry.getDownloads().getClient().getUrl();
            int size = entry.getDownloads().getClient().getSize();
            log.info("Starting client download..");
            if (!httpApi.downloadToFile(url, versionPath.resolve(Paths.get("client.jar")), progressBar, size)) {
                log.error("Failed to download game client");
                return false;
            }
            updateProgress(size);
        } catch (NullPointerException e) {
            log.error("Error while parsing game client download info ", e);
            return false;
        }
        return true; //downloadGameClient(url, sha1, entry);
    }

    private String formatArg(String arg) {
        for (Map.Entry<String, String> entry : this.argumentMap.entrySet()) {
            arg = arg.replace(entry.getKey(), entry.getValue());
        }
        return arg;
    }

    private int isHigherVersion(String version1, String version2) {
        if (version1.contains(":-natives")) {
            version1 = version1.substring(version1.lastIndexOf(':'));
        }
        if (version2.contains(":-natives")) {
            version2 = version2.substring(version1.lastIndexOf(':'));
        }
        String[] parts1 = version1.split("\\.");
        String[] parts2 = version2.split("\\.");
        for (int i = 0; i < Math.min(parts1.length, parts2.length); i++) {
            try {
                int versionNum1 = Integer.parseInt(parts1[i]);
                int versionNum2 = Integer.parseInt(parts2[i]);
                if (versionNum1 > versionNum2) {
                    return 1;
                } else if (versionNum2 > versionNum1) {
                    return -1;
                }
            } catch (NumberFormatException e) {
                log.warn("Failed to compare version numbers: {} == {}", version1, version2);
                return 1;
            }
        }
        return 0;
    }

    private boolean startClient() {
        Path librariesPath = workingDir.resolve(Paths.get("libraries"));
        Path versionsPath = workingDir.resolve(Paths.get("versions", entry.getId()));
        Path clientPath = versionsPath.resolve(Paths.get("client.jar"));
        Path nativesPath = versionsPath.resolve("natives");
        if (!nativesPath.toFile().exists()) {
            if (!nativesPath.toFile().mkdirs()) {
                log.error("Failed to create native files path: " + nativesPath.toString());
                return false;
            }
        }
        Path startupArgs = workingDir.resolve(Paths.get("versions", entry.getId(), entry.getId() + ".json"));
        GameVersionEntry version;
        try {
            version = gson.fromJson(new String(Files.readAllBytes(startupArgs), StandardCharsets.UTF_8), GameVersionEntry.class);
        } catch (IOException e) {
            log.error("Failed to read game meta data from: " + startupArgs.toString() + " ", e);
            return false;
        }

        this.argumentMap.put("${assets_index_name}", version.getAssetIndex().getId());
        StringBuilder nativeLibraryBuilder = new StringBuilder();
        //parseNative(version, nativeLibraryBuilder);

        this.argumentMap.put("${natives_directory}", System.getProperty("java.io.tmpdir"));//nativeLibraryBuilder.toString());
        ProcessBuilder builder = new ProcessBuilder();
        List<String> args = new LinkedList<>();
        args.add("java");
        Collections.addAll(args, jvmArgs.split(" "));

        StringBuilder libraries = new StringBuilder();
        for (LibraryEntry entry : version.getLibraries()) {
            if(entry.doesBelongToOS()) {
                Path libraryPath = librariesPath.resolve(entry.getDownloads().getArtifact().getPath());
                libraries.append(libraryPath.toString());
                libraries.append(File.pathSeparatorChar);
            }
        }
        libraries.append(clientPath.toString());
        this.argumentMap.put("${classpath}", libraries.toString());

        for (JvmArgument jvmArg : version.getArguments().getJvmArguments()) {
            boolean match = true;
            for (JvmArgumentRule rule : jvmArg.getRules()) {
                if (!rule.getOs().doesApply()) {
                    match = false;
                    break;
                }
            }
            if (match) {
                jvmArg.getValue().forEach(x -> args.add(formatArg(x)));
            }
        }
        //args.add(libraries.toString());
        args.add(version.getMainClass());
        for (String gameArg : version.getArguments().getGameArguments()) {
            args.add(formatArg(gameArg));
        }

        StringBuilder sb = new StringBuilder();
        for (String s : args) {
            sb.append(s);
            sb.append(" ");
        }
        System.out.println(sb.toString());
        builder.command(args);
        //builder.inheritIO();
        try {
            log.info("Starting game process...");
            Process p = builder.start();
            new Thread(new ProcessPiper(wrapper, p.getInputStream(), launcher)).start();
            new Thread(new ProcessPiper(wrapper, p.getErrorStream(), launcher)).start();
        } catch (IOException e) {
            log.error("Failed to start game: ", e);
        }
        return true;
    }

    public void launch(VersionEntry entry, String name, String jvmArgs) {
        if (!this.instance.isAlive()) {
            this.jvmArgs = jvmArgs;
            this.currentSizeDownload = 0;
            this.totalSizeDownload = 0;
            this.entry = entry;
            this.name = name;
            initArgumentMap(name, entry);
            this.instance = new Thread(this);
            this.instance.start();
        }
    }
}
