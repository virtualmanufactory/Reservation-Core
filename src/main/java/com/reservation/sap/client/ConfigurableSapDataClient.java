package com.reservation.sap.client;

import com.reservation.sap.config.SapSyncProperties;
import com.reservation.sap.dto.OddzialDto;
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
    public List<OddzialDto> fetchOddzialy() {
        SapSyncProperties.Source source = properties.getSource();
        return switch (source.getType()) {
            case FILE -> readFromFile(source.getFilePath());
            case HTTP -> readFromHttp(source);
        };
    }

    private List<OddzialDto> readFromFile(String filePath) {
        Path path = Path.of(filePath);
        log.info("SAP feed: odczyt oddziałów z pliku {}", path.toAbsolutePath());
        try {
            if (!Files.exists(path)) {
                throw new IllegalStateException("Plik źródłowy SAP nie istnieje: " + path.toAbsolutePath());
            }
            return parse(Files.readAllBytes(path));
        } catch (IOException ex) {
            throw new IllegalStateException("Nie udało się odczytać pliku SAP: " + path, ex);
        }
    }

    private List<OddzialDto> readFromHttp(SapSyncProperties.Source source) {
        if (source.getUrl() == null || source.getUrl().isBlank()) {
            throw new IllegalStateException("app.sap.source.url nie jest ustawiony");
        }
        log.info("SAP feed: pobieranie oddziałów z {}", source.getUrl());
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
                throw new IllegalStateException("SAP HTTP zwrócił status " + response.statusCode());
            }
            return parse(response.body());
        } catch (IOException | InterruptedException ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new IllegalStateException("Nie udało się pobrać danych SAP z HTTP", ex);
        }
    }

    private List<OddzialDto> parse(byte[] bytes) {
        try {
            List<OddzialDto> rows = objectMapper.readValue(bytes, new TypeReference<>() {
            });
            if (rows == null) {
                throw new IllegalStateException("Pusty dokument JSON ze źródła SAP");
            }
            return rows;
        } catch (RuntimeException ex) {
            throw new IllegalStateException("Niepoprawny format danych SAP (oczekiwano tablicy JSON oddziałów)", ex);
        }
    }
}
