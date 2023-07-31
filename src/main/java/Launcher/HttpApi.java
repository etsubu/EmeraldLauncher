package Launcher;

import java.io.*;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

/**
 * Tiny Api for HTTP Get requests
 *
 * @author etsubu
 * @version 28 Aug 2018
 */
public class HttpApi {
    private static final Logger log = LoggerFactory.getLogger(HttpApi.class);
    private final HttpClient client;

    public HttpApi() {
        this.client = HttpClient.newHttpClient();
    }

    /**
     * Sends HTTP Get request and returns the response in String
     *
     * @param url URL to request
     * @return Response in String
     * @throws IOException If there was an connection error
     */
    public String sendGet(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Invalid HTTP response: " + response.statusCode());
        }
        return response.body();
    }

    public boolean downloadToFile(String url, Path file, JProgressBar progressBar, int length) throws IOException, InterruptedException {
        return downloadToFile(url, file, progressBar, length, false);
    }

    public boolean downloadToFile(String url, Path file, JProgressBar progressBar, int length, boolean overwrite) throws IOException, InterruptedException {
        if (file.toFile().exists()) {
            if (overwrite) {
                if (!file.toFile().delete()) {
                    log.error("Failed to delete file before overwriting {}", file.toString());
                }
            } else {
                //log.info("File: " + file.toString() + " exists. Skipping download");
                return true;
            }
        }
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
        HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException("Invalid HTTP response: " + response.statusCode());
        }

        Optional<File> parent = Optional.ofNullable(file.getParent()).map(Path::toFile);
        if (!parent.map(File::exists).orElse(true)) {
            if (!parent.map(File::mkdirs).orElse(true)) {
                log.error("Failed to create folder: " + parent.map(x -> x.toString()).orElse("No parent available"));
                return false;
            }
        }
        String title = url;
        int index = url.lastIndexOf("/");
        if (index != -1 && index != url.length() - 1) {
            title = url.substring(index + 1);
        }
        // this will be useful to display download percentage
        // might be -1: server did not report the length
        progressBar.setString(title);
        progressBar.setMaximum(length);
        progressBar.setValue(0);
        InputStream input = response.body();
        byte[] buffer = new byte[4096];
        int n;
        int counter = 0;

        try (OutputStream output = new FileOutputStream(file.toFile())) {
            while ((n = input.read(buffer)) != -1) {
                output.write(buffer, 0, n);
                counter += n;
                progressBar.setValue(counter);
            }
        }
        progressBar.setValue(progressBar.getMaximum());
        return true;
    }
}
