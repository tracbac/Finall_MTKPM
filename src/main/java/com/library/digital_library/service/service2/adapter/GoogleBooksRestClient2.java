package com.library.digital_library.service.service2.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adaptee trong Adapter Pattern.
 * Đóng vai trò là Client kỹ thuật giao tiếp với Google Books API thông qua giao thức HTTP REST.
 * Đi kèm cơ chế Fallback Online sang OpenLibrary API và Offline Mock khi Google Books API bị 429 Quota Exceeded.
 */
@Component
public class GoogleBooksRestClient2 {

    private static final Logger log = LoggerFactory.getLogger(GoogleBooksRestClient2.class);
    private static final String GOOGLE_BOOKS_API_URL = "https://www.googleapis.com/books/v1/volumes";
    private static final String OPEN_LIBRARY_API_URL = "https://openlibrary.org/search.json";

    @Value("${google.books.api.key:}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GoogleBooksRestClient2() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(8000);
        factory.setReadTimeout(12000);
        this.restTemplate = new RestTemplate(factory);
        this.objectMapper = new ObjectMapper();
    }

    public GoogleBooksRestClient2(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Phương thức đặc thù của Adaptee gọi Google Volumes API (trang mặc định là 1).
     */
    public GoogleBooksModel2.GoogleVolumesResponse2 queryGoogleVolumesApi(String q, int maxResults) {
        return queryGoogleVolumesApi(q, 1, maxResults);
    }

    /**
     * Phương thức đặc thù của Adaptee gọi Google Volumes API có hỗ trợ phân trang (page).
     * Tự động chuyển đổi sang OpenLibrary hoặc Offline Mock nếu Google Books chặn 429.
     */
    public GoogleBooksModel2.GoogleVolumesResponse2 queryGoogleVolumesApi(String q, int page, int maxResults) {
        int safePage = Math.max(page, 1);
        int safeMaxResults = Math.min(Math.max(maxResults, 1), 20);
        int startIndex = (safePage - 1) * safeMaxResults;
        String searchQuery = (q != null && !q.isBlank()) ? q.trim() : "Design Patterns";

        // 1. Thử gọi Google Books API thật
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(GOOGLE_BOOKS_API_URL)
                    .queryParam("q", searchQuery)
                    .queryParam("startIndex", startIndex)
                    .queryParam("maxResults", safeMaxResults);

            if (apiKey != null && !apiKey.isBlank()) {
                builder.queryParam("key", apiKey.trim());
            }

            URI uri = builder.build().encode().toUri();
            log.info("Gọi Google Books API: {}", uri);

            ResponseEntity<GoogleBooksModel2.GoogleVolumesResponse2> response =
                    restTemplate.getForEntity(uri, GoogleBooksModel2.GoogleVolumesResponse2.class);

            if (response.getStatusCode().is2xxSuccessful()
                    && response.getBody() != null
                    && response.getBody().getItems() != null
                    && !response.getBody().getItems().isEmpty()) {
                return response.getBody();
            }
        } catch (Exception ex) {
            log.warn("Google Books API trả về lỗi hoặc quá tải ({}) - Đang chuyển sang OpenLibrary trực tuyến...", ex.getMessage());
        }

        // 2. Thử gọi OpenLibrary API trực tuyến thật (hoàn toàn miễn phí, hỗ trợ phân trang chuẩn xác)
        try {
            URI openLibUri = UriComponentsBuilder.fromHttpUrl(OPEN_LIBRARY_API_URL)
                    .queryParam("q", searchQuery)
                    .queryParam("page", safePage)
                    .queryParam("limit", safeMaxResults)
                    .build()
                    .encode()
                    .toUri();

            log.info("Kết nối OpenLibrary API: {}", openLibUri);
            ResponseEntity<String> openLibRes = restTemplate.getForEntity(openLibUri, String.class);

            if (openLibRes.getStatusCode().is2xxSuccessful() && openLibRes.getBody() != null) {
                GoogleBooksModel2.GoogleVolumesResponse2 converted = parseOpenLibraryResponse(openLibRes.getBody(), safeMaxResults);
                if (converted != null && converted.getItems() != null && !converted.getItems().isEmpty()) {
                    return converted;
                }

                // Nếu tìm chính xác bị 0 kết quả (do gõ nhầm chính tả như 'clean achitecture'), tự động nới lỏng tìm kiếm bằng OR
                if (searchQuery.contains(" ")) {
                    String orQuery = searchQuery.replaceAll("\\s+", " OR ");
                    URI orUri = UriComponentsBuilder.fromHttpUrl(OPEN_LIBRARY_API_URL)
                            .queryParam("q", orQuery)
                            .queryParam("page", safePage)
                            .queryParam("limit", safeMaxResults)
                            .build()
                            .encode()
                            .toUri();
                    log.info("Thử tìm kiếm nới lỏng lỗi chính tả trên OpenLibrary: {}", orUri);
                    ResponseEntity<String> orRes = restTemplate.getForEntity(orUri, String.class);
                    if (orRes.getStatusCode().is2xxSuccessful() && orRes.getBody() != null) {
                        GoogleBooksModel2.GoogleVolumesResponse2 orConverted = parseOpenLibraryResponse(orRes.getBody(), safeMaxResults);
                        if (orConverted != null && orConverted.getItems() != null && !orConverted.getItems().isEmpty()) {
                            return orConverted;
                        }
                    }
                }

                // Kết nối Internet thành công nhưng từ khóa không có sách hoặc đã tới trang cuối
                return GoogleBooksModel2.GoogleVolumesResponse2.builder()
                        .kind("books#volumes")
                        .totalItems(0)
                        .items(List.of())
                        .build();
            }
        } catch (Exception ex) {
            log.warn("Không thể kết nối OpenLibrary ({}) - Kích hoạt cơ chế Offline Mock dự phòng.", ex.getMessage());
        }

        // 3. Fallback an toàn khi mất mạng hoặc không thể truy cập internet
        return buildOfflineMockResponse(searchQuery, safePage, safeMaxResults);
    }

    /**
     * Chuyển đổi kết quả OpenLibrary sang chuẩn Google Books Model để Adapter xử lý.
     */
    private GoogleBooksModel2.GoogleVolumesResponse2 parseOpenLibraryResponse(String jsonString, int limit) {
        try {
            JsonNode root = objectMapper.readTree(jsonString);
            JsonNode docs = root.get("docs");
            if (docs == null || !docs.isArray() || docs.isEmpty()) {
                return null;
            }

            List<GoogleBooksModel2.GoogleVolumeItem2> items = new ArrayList<>();
            for (JsonNode doc : docs) {
                if (items.size() >= limit) break;

                String title = doc.has("title") ? doc.get("title").asText() : "Tài liệu";
                List<String> authors = new ArrayList<>();
                if (doc.has("author_name") && doc.get("author_name").isArray()) {
                    doc.get("author_name").forEach(a -> authors.add(a.asText()));
                }
                if (authors.isEmpty()) authors.add("Nhiều tác giả");

                String publisher = doc.has("publisher") && doc.get("publisher").isArray() && !doc.get("publisher").isEmpty()
                        ? doc.get("publisher").get(0).asText() : "Nhà xuất bản Quốc tế";

                int pages = doc.has("number_of_pages_median") ? doc.get("number_of_pages_median").asInt() : 350;

                String isbnVal = null;
                if (doc.has("isbn") && doc.get("isbn").isArray() && !doc.get("isbn").isEmpty()) {
                    isbnVal = doc.get("isbn").get(0).asText();
                }

                String coverUrl = null;
                if (doc.has("cover_i")) {
                    coverUrl = "https://covers.openlibrary.org/b/id/" + doc.get("cover_i").asText() + "-M.jpg";
                } else {
                    coverUrl = "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=300";
                }

                items.add(GoogleBooksModel2.GoogleVolumeItem2.builder()
                        .id("OL-" + (doc.has("key") ? doc.get("key").asText().replace("/works/", "") : System.currentTimeMillis()))
                        .volumeInfo(GoogleBooksModel2.VolumeInfo.builder()
                                .title(title)
                                .authors(authors)
                                .publisher(publisher)
                                .publishedDate(doc.has("first_publish_year") ? doc.get("first_publish_year").asText() : "2022")
                                .description("Tài liệu tra cứu trực tuyến quốc tế từ OpenLibrary về chủ đề " + title + ".")
                                .pageCount(pages > 0 ? pages : 320)
                                .categories(List.of("Công nghệ & Khoa học"))
                                .industryIdentifiers(List.of(
                                        GoogleBooksModel2.IndustryIdentifier.builder().type("ISBN_13").identifier(isbnVal != null ? isbnVal : "978" + (long)(Math.random() * 10000000000L)).build()
                                ))
                                .imageLinks(GoogleBooksModel2.ImageLinks.builder()
                                        .thumbnail(coverUrl)
                                        .smallThumbnail(coverUrl)
                                        .build())
                                .previewLink("https://openlibrary.org" + (doc.has("key") ? doc.get("key").asText() : ""))
                                .build())
                        .build());
            }

            int total = (root.has("numFound") && root.get("numFound").isInt())
                    ? root.get("numFound").asInt()
                    : items.size();

            return GoogleBooksModel2.GoogleVolumesResponse2.builder()
                    .kind("books#volumes")
                    .totalItems(total)
                    .items(items)
                    .build();
        } catch (Exception e) {
            log.error("Lỗi khi chuyển đổi dữ liệu từ OpenLibrary: {}", e.getMessage());
            return null;
        }
    }

    private GoogleBooksModel2.GoogleVolumesResponse2 buildOfflineMockResponse(String query, int limit) {
        return buildOfflineMockResponse(query, 1, limit);
    }

    /**
     * Dữ liệu mô phỏng dự phòng đa dạng khi hoàn toàn mất mạng (Offline Fallback có phân trang).
     */
    private GoogleBooksModel2.GoogleVolumesResponse2 buildOfflineMockResponse(String query, int page, int limit) {
        List<GoogleBooksModel2.GoogleVolumeItem2> allCatalog = new ArrayList<>();

        // --- NHÓM SÁCH JAVA ---
        allCatalog.add(createMockItem("JAVA-01", "Effective Java (3rd Edition)", List.of("Joshua Bloch"),
                "Addison-Wesley", "2018", 412, "Lập trình Java / Software Engineering", "9780134685991",
                "https://images.unsplash.com/photo-1532012164546-f432f2e37b73?w=300",
                "Cẩm nang kinh điển thực hành lập trình Java tốt nhất với 90 quy tắc kiến trúc và thiết kế code chất lượng cao."));

        allCatalog.add(createMockItem("JAVA-02", "Head First Java: A Brain-Friendly Guide", List.of("Kathy Sierra", "Bert Bates"),
                "O'Reilly Media", "2022", 720, "Lập trình Java / Beginners", "9781491910771",
                "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=300",
                "Phương pháp tiếp cận trực quan, sinh động giúp nắm vững nền tảng ngôn ngữ Java, OOP, Streams và Concurrency."));

        allCatalog.add(createMockItem("JAVA-03", "Core Java Volume I: Fundamentals", List.of("Cay S. Horstmann"),
                "Prentice Hall", "2020", 890, "Lập trình Java / Advanced", "9780135166307",
                "https://images.unsplash.com/photo-1512820790803-83ca734da794?w=300",
                "Bộ sách toàn diện và chi tiết nhất về cốt lõi Java, xử lý biệt lệ, Generics, Collections Framework."));

        allCatalog.add(createMockItem("JAVA-04", "Java Concurrency in Practice", List.of("Brian Goetz", "Tim Peierls"),
                "Addison-Wesley", "2016", 432, "Lập trình Java / Concurrency", "9780321349606",
                "https://images.unsplash.com/photo-1506126613408-eca07ce68773?w=300",
                "Tác phẩm đỉnh cao về lập trình đa luồng (multithreading), đồng bộ hóa dữ liệu và thread safety trong Java."));

        allCatalog.add(createMockItem("JAVA-05", "Spring Boot in Action", List.of("Craig Walls"),
                "Manning", "2018", 280, "Lập trình Java / Spring Boot", "9781617292545",
                "https://images.unsplash.com/photo-1497633762265-9d179a990aa6?w=300",
                "Hướng dẫn phát triển ứng dụng web, microservices và RESTful API với framework Spring Boot hiện đại."));

        // --- NHÓM DESIGN PATTERNS & KIẾN TRÚC ---
        allCatalog.add(createMockItem("PAT-01", "Head First Design Patterns", List.of("Eric Freeman", "Elisabeth Robson"),
                "O'Reilly Media", "2020", 680, "Thiết kế phần mềm / Design Patterns", "9781492078005",
                "https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?w=300",
                "Cuốn sách kinh điển giải thích trực quan về các mẫu thiết kế: Factory, Strategy, Adapter, Decorator, Observer."));

        allCatalog.add(createMockItem("PAT-02", "Design Patterns: Elements of Reusable Object-Oriented Software (GoF)", List.of("Erich Gamma", "Richard Helm", "Ralph Johnson", "John Vlissides"),
                "Addison-Wesley", "1994", 416, "Thiết kế phần mềm / OOP", "9780201633610",
                "https://images.unsplash.com/photo-1495446815901-a7297e633e8d?w=300",
                "Tác phẩm bất hủ của nhóm Gang of Four (GoF) khai sinh ra 23 mẫu thiết kế phần mềm kinh điển."));

        allCatalog.add(createMockItem("PAT-03", "Clean Architecture: A Craftsman's Guide", List.of("Robert C. Martin"),
                "Prentice Hall", "2017", 432, "Kiến trúc phần mềm / SOLID", "9780134494166",
                "https://images.unsplash.com/photo-1512820790803-83ca734da794?w=300",
                "Hướng dẫn toàn diện về kiến trúc phần mềm sạch, nguyên lý SOLID và phân tách tầng kiến trúc hệ thống."));

        allCatalog.add(createMockItem("PAT-04", "Clean Code: A Handbook of Agile Software Craftsmanship", List.of("Robert C. Martin"),
                "Prentice Hall", "2008", 464, "Kỹ nghệ phần mềm / Best Practices", "9780132350884",
                "https://images.unsplash.com/photo-1532012164546-f432f2e37b73?w=300",
                "Quy tắc viết mã nguồn rõ ràng, dễ bảo trì và loại bỏ code smell trong lập trình chuyên nghiệp."));

        allCatalog.add(createMockItem("PAT-05", "Refactoring: Improving the Design of Existing Code", List.of("Martin Fowler", "Kent Beck"),
                "Addison-Wesley", "2018", 448, "Tái cấu trúc mã / OOP", "9780201485677",
                "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=300",
                "Phương pháp và kỹ thuật cải thiện thiết kế mã nguồn mà không làm thay đổi hành vi bên ngoài."));

        // Lọc theo từ khóa
        String lowerQuery = query.toLowerCase(Locale.ROOT);
        List<GoogleBooksModel2.GoogleVolumeItem2> filtered = allCatalog.stream()
                .filter(item -> {
                    GoogleBooksModel2.VolumeInfo info = item.getVolumeInfo();
                    if (info == null) return false;
                    boolean titleMatch = info.getTitle() != null && info.getTitle().toLowerCase(Locale.ROOT).contains(lowerQuery);
                    boolean authorMatch = info.getAuthors() != null && info.getAuthors().stream().anyMatch(a -> a.toLowerCase(Locale.ROOT).contains(lowerQuery));
                    boolean categoryMatch = info.getCategories() != null && info.getCategories().stream().anyMatch(c -> c.toLowerCase(Locale.ROOT).contains(lowerQuery));
                    boolean descMatch = info.getDescription() != null && info.getDescription().toLowerCase(Locale.ROOT).contains(lowerQuery);
                    return titleMatch || authorMatch || categoryMatch || descMatch;
                })
                .toList();

        if (filtered.isEmpty()) {
            filtered = allCatalog;
        }

        int fromIndex = Math.min((page - 1) * limit, filtered.size());
        int toIndex = Math.min(fromIndex + limit, filtered.size());
        List<GoogleBooksModel2.GoogleVolumeItem2> pagedItems = (fromIndex < toIndex)
                ? filtered.subList(fromIndex, toIndex)
                : List.of();

        return GoogleBooksModel2.GoogleVolumesResponse2.builder()
                .kind("books#volumes")
                .totalItems(filtered.size())
                .items(pagedItems)
                .build();
    }

    private GoogleBooksModel2.GoogleVolumeItem2 createMockItem(String id, String title, List<String> authors,
                                                             String publisher, String year, int pages,
                                                             String category, String isbn, String imgUrl, String desc) {
        return GoogleBooksModel2.GoogleVolumeItem2.builder()
                .id(id)
                .volumeInfo(GoogleBooksModel2.VolumeInfo.builder()
                        .title(title)
                        .authors(authors)
                        .publisher(publisher)
                        .publishedDate(year)
                        .description(desc)
                        .pageCount(pages)
                        .categories(List.of(category))
                        .industryIdentifiers(List.of(
                                GoogleBooksModel2.IndustryIdentifier.builder().type("ISBN_13").identifier(isbn).build()
                        ))
                        .imageLinks(GoogleBooksModel2.ImageLinks.builder()
                                .thumbnail(imgUrl)
                                .smallThumbnail(imgUrl)
                                .build())
                        .previewLink("https://books.google.com/books?isbn=" + isbn)
                        .build())
                .build();
    }
}
