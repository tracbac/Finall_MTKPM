package com.library.digital_library.service.service2.adapter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Các lớp POJO ánh xạ chính xác cấu trúc JSON trả về của Google Books API (Adaptee Models).
 */
public class GoogleBooksModel2 {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GoogleVolumesResponse2 {
        private String kind;
        private Integer totalItems;
        private List<GoogleVolumeItem2> items;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GoogleVolumeItem2 {
        private String id;
        private String etag;
        private String selfLink;
        private VolumeInfo volumeInfo;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VolumeInfo {
        private String title;
        private List<String> authors;
        private String publisher;
        private String publishedDate;
        private String description;
        private List<IndustryIdentifier> industryIdentifiers;
        private Integer pageCount;
        private List<String> categories;
        private ImageLinks imageLinks;
        private String previewLink;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IndustryIdentifier {
        private String type; // ISBN_13, ISBN_10, OTHER
        private String identifier;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ImageLinks {
        private String smallThumbnail;
        private String thumbnail;
    }
}

