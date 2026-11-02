package com.library.digital_library.service;

import java.net.URI;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class SupabaseBookCoverStorage implements BookCoverStorage {

    private final RestTemplate restTemplate;
    private final String supabaseUrl;
    private final String serviceKey;
    private final String bucketName;

    public SupabaseBookCoverStorage(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${supabase.url}") String supabaseUrl,
            @Value("${supabase.key}") String serviceKey,
            @Value("${supabase.bucket:book_prices}") String bucketName) {
        this.restTemplate = restTemplateBuilder.build();
        this.supabaseUrl = supabaseUrl;
        this.serviceKey = serviceKey;
        this.bucketName = bucketName;
    }

    @Override
    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Cover image must not be empty");
        }
        if (serviceKey.isBlank()) {
            throw new BookCoverStorageException("Supabase service key is not configured", null);
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new IllegalArgumentException("Cover must be an image");
        }

        String fileName = UUID.randomUUID() + extensionFor(contentType);
        URI uploadUri = UriComponentsBuilder.fromUriString(supabaseUrl)
                .pathSegment("storage", "v1", "object", bucketName, fileName)
                .build()
                .encode()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(serviceKey);
        headers.set("apikey", serviceKey);
        headers.setContentType(MediaType.parseMediaType(contentType));

        try {
            restTemplate.postForEntity(uploadUri, new HttpEntity<>(file.getBytes(), headers), String.class);
        } catch (RestClientResponseException exception) {
            throw new BookCoverStorageException(
                    "Supabase rejected cover upload with HTTP " + exception.getStatusCode().value(),
                    exception);
        } catch (java.io.IOException exception) {
            throw new BookCoverStorageException("Could not read the cover image", exception);
        }

        return UriComponentsBuilder.fromUriString(supabaseUrl)
                .pathSegment("storage", "v1", "object", "public", bucketName, fileName)
                .build()
                .encode()
                .toUriString();
    }

    private String extensionFor(String contentType) {
        return switch (contentType.toLowerCase(Locale.ROOT)) {
            case "image/jpeg", "image/jpg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> throw new IllegalArgumentException("Supported cover types are JPEG, PNG, WebP and GIF");
        };
    }
}
