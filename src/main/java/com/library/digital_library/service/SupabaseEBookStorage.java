package com.library.digital_library.service;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class SupabaseEBookStorage {

    private static final long MAX_FILE_SIZE = 50L * 1024 * 1024;

    private final RestTemplate restTemplate;
    private final String supabaseUrl;
    private final String serviceKey;
    private final String bucketName;

    public SupabaseEBookStorage(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${supabase.url}") String supabaseUrl,
            @Value("${supabase.key}") String serviceKey,
            @Value("${supabase.ebooks-bucket:ebooks}") String bucketName) {
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(30))
                .build();
        this.supabaseUrl = supabaseUrl;
        this.serviceKey = serviceKey;
        this.bucketName = bucketName;
    }

    public void upload(String storagePath, MultipartFile file) {
        validateFile(file);
        ensureConfigured();

        URI uploadUri = objectUri(storagePath);
        HttpHeaders headers = storageHeaders(MediaType.APPLICATION_PDF);
        headers.set("x-upsert", "true");
        try {
            restTemplate.exchange(
                    uploadUri,
                    HttpMethod.POST,
                    new HttpEntity<>(file.getBytes(), headers),
                    String.class);
        } catch (RestClientResponseException exception) {
            throw new EBookStorageException(
                    "Supabase rejected the E-Book upload with HTTP " + exception.getStatusCode().value(),
                    exception);
        } catch (RestClientException exception) {
            throw new EBookStorageException("Could not connect to Supabase Storage for the E-Book upload", exception);
        } catch (IOException exception) {
            throw new EBookStorageException("Could not read the E-Book PDF", exception);
        }
    }

    public byte[] download(String storagePath) {
        ensureConfigured();
        URI downloadUri = objectUri(storagePath);
        HttpHeaders headers = storageHeaders(MediaType.APPLICATION_PDF);
        try {
            byte[] content = restTemplate.exchange(
                    downloadUri,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    byte[].class).getBody();
            if (content == null || content.length == 0) {
                throw new EBookStorageException("Supabase returned an empty E-Book PDF", null);
            }
            return content;
        } catch (RestClientResponseException exception) {
            throw new EBookStorageException(
                    "Supabase rejected the E-Book download with HTTP "
                            + exception.getStatusCode().value(),
                    exception);
        } catch (RestClientException exception) {
            throw new EBookStorageException("Could not connect to Supabase Storage to download the E-Book", exception);
        }
    }

    public void delete(String storagePath) {
        ensureConfigured();
        URI deleteUri = UriComponentsBuilder.fromUriString(supabaseUrl)
                .pathSegment("storage", "v1", "object", bucketName)
                .build()
                .encode()
                .toUri();
        HttpHeaders headers = storageHeaders(MediaType.APPLICATION_JSON);
        Map<String, Object> request = Map.of("prefixes", List.of(storagePath));
        try {
            restTemplate.exchange(
                    deleteUri,
                    HttpMethod.DELETE,
                    new HttpEntity<>(request, headers),
                    String.class);
        } catch (RestClientResponseException exception) {
            throw new EBookStorageException(
                    "Supabase rejected E-Book cleanup with HTTP " + exception.getStatusCode().value(),
                    exception);
        } catch (RestClientException exception) {
            throw new EBookStorageException("Could not connect to Supabase Storage to remove the E-Book", exception);
        }
    }

    @SuppressWarnings("unchecked")
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Chọn tệp PDF trước khi tải lên.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("PDF không được vượt quá 50 MB.");
        }
        String contentType = file.getContentType();
        if (contentType != null && !contentType.isBlank()
                && !MediaType.APPLICATION_PDF_VALUE.equalsIgnoreCase(contentType)) {
            throw new IllegalArgumentException("Tệp E-Book phải có định dạng PDF.");
        }
        try (var input = file.getInputStream()) {
            byte[] signature = input.readNBytes(5);
            if (!"%PDF-".equals(new String(signature, java.nio.charset.StandardCharsets.US_ASCII))) {
                throw new IllegalArgumentException("Tệp tải lên không phải PDF hợp lệ.");
            }
        } catch (IOException exception) {
            throw new EBookStorageException("Could not inspect the E-Book PDF", exception);
        }
    }

    private void ensureConfigured() {
        if (serviceKey == null || serviceKey.isBlank()) {
            throw new EBookStorageException("Supabase service key is not configured", null);
        }
    }

    private URI objectUri(String storagePath) {
        return UriComponentsBuilder.fromUriString(supabaseUrl)
                .pathSegment("storage", "v1", "object", bucketName, storagePath)
                .build()
                .encode()
                .toUri();
    }

    private HttpHeaders storageHeaders(MediaType contentType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(serviceKey);
        headers.set("apikey", serviceKey);
        headers.setContentType(contentType);
        return headers;
    }
}
