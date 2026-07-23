package com.reservation.sap.client;

import com.reservation.sap.config.SapSyncProperties;
import com.reservation.sap.dto.BranchDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Base64;
import java.util.List;

@Component
public class ConfigurableSapDataClient implements SapDataClient {

    private static final Logger log = LoggerFactory.getLogger(ConfigurableSapDataClient.class);

    private final SapSyncProperties properties;
    private final ObjectMapper objectMapper;

    public ConfigurableSapDataClient(SapSyncProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<BranchDto> fetchBranches() {
        SapSyncProperties.Source source = properties.getSource();
        return switch (source.getType()) {
            case FILE -> readFromFile(source.getFilePath());
            case HTTP -> readFromHttp(source);
        };
    }

    private List<BranchDto> readFromFile(String filePath) {
        Path path = Path.of(filePath);
        log.info("SAP feed: reading branches from file {}", path.toAbsolutePath());
        try {
            if (!Files.exists(path)) {
                throw new IllegalStateException("SAP source file does not exist: " + path.toAbsolutePath());
            }
            return parse(Files.readAllBytes(path));
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read SAP file: " + path, ex);
        }
    }

    private List<BranchDto> readFromHttp(SapSyncProperties.Source source) {
        if (source.getUrl() == null || source.getUrl().isBlank()) {
            throw new IllegalStateException("app.sap.source.url is not set");
        }
        log.info("SAP feed: downloading branches from {}", source.getUrl());
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofMillis(source.getConnectTimeoutMs()))
                    .build();

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(source.getUrl()))
                    .timeout(Duration.ofMillis(source.getReadTimeoutMs()))
                    .GET();

            if (source.getUsername() != null && !source.getUsername().isBlank()) {
                String token = Base64.getEncoder().encodeToString(
                        (source.getUsername() + ":" + source.getPassword()).getBytes(StandardCharsets.UTF_8));
                builder.header("Authorization", "Basic " + token);
            }

            HttpResponse<byte[]> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("SAP HTTP returned status " + response.statusCode());
            }
            return parse(response.body());
        } catch (IOException | InterruptedException ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new IllegalStateException("Failed to download SAP data over HTTP", ex);
        }
    }

    private List<BranchDto> parse(byte[] bytes) {
        try {
            List<BranchDto> rows = objectMapper.readValue(bytes, new TypeReference<>() {
            });
            if (rows == null) {
                throw new IllegalStateException("Empty JSON document from SAP source");
            }
            return rows;
        } catch (RuntimeException ex) {
            throw new IllegalStateException("Invalid SAP data format (expected JSON array of branches)", ex);
        }
    }
}
