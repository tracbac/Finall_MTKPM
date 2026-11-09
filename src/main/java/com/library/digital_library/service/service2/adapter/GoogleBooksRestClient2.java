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
 * Đóng vai trò là Client kỹ thuật giao tiếp trực tiếp với Google Books API thông qua giao thức HTTP REST.
 * Đi kèm cơ chế Offline Mock dự phòng an toàn khi mất mạng hoặc chạy Unit Test.
 */
@Component
public class GoogleBooksRestClient2 {

    private static final Logger log = LoggerFactory.getLogger(GoogleBooksRestClient2.class);
    private static final String GOOGLE_BOOKS_API_URL = "https://www.googleapis.com/books/v1/volumes";

    @Value("${google.books.api.key:AIzaSyBPwKQnkI4wJHW0QFp_3imbJvrkB0It4zA}")
    private String apiKey = "AIzaSyBPwKQnkI4wJHW0QFp_3imbJvrkB0It4zA";

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
     * Phương thức đặc thù của Adaptee gọi Google Books API có hỗ trợ phân trang (page).
     */
    public GoogleBooksModel2.GoogleVolumesResponse2 queryGoogleVolumesApi(String q, int page, int maxResults) {
        int safePage = Math.max(page, 1);
        int safeMaxResults = Math.min(Math.max(maxResults, 1), 20);
        int startIndex = (safePage - 1) * safeMaxResults;
        String searchQuery = (q != null && !q.isBlank()) ? q.trim() : "Design Patterns";

        // 1. Gọi trực tiếp Google Books API chính thức
        try {
            String googleQuery = searchQuery;
            String lower = googleQuery.toLowerCase(Locale.ROOT);
            if (lower.startsWith("title:")) {
                googleQuery = "intitle:" + googleQuery.substring("title:".length()).trim();
            } else if (lower.startsWith("author:")) {
                googleQuery = "inauthor:" + googleQuery.substring("author:".length()).trim();
            } else if (lower.startsWith("category:") || lower.startsWith("subject:")) {
                googleQuery = "subject:" + googleQuery.substring(googleQuery.indexOf(":") + 1).trim();
            } else if (lower.startsWith("isbn:")) {
                googleQuery = "isbn:" + googleQuery.substring("isbn:".length()).trim();
            }

            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(GOOGLE_BOOKS_API_URL)
                    .queryParam("q", googleQuery)
                    .queryParam("startIndex", startIndex)
                    .queryParam("maxResults", safeMaxResults);

            if (apiKey != null && !apiKey.isBlank()) {
                builder.queryParam("key", apiKey.trim());
            }

            URI uri = builder.build().encode().toUri();
            log.info("Gọi Google Books API trang {} (startIndex: {}) với từ khóa: '{}'", safePage, startIndex, googleQuery);

            ResponseEntity<GoogleBooksModel2.GoogleVolumesResponse2> response =
                    restTemplate.getForEntity(uri, GoogleBooksModel2.GoogleVolumesResponse2.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                GoogleBooksModel2.GoogleVolumesResponse2 body = response.getBody();
                if (body.getItems() == null) {
                    body.setItems(List.of());
                }
                return body;
            }
        } catch (Exception ex) {
            log.warn("Không thể gọi Google Books API ({}) - Kích hoạt Mock dự phòng.", ex.getMessage());
        }

        // 2. Fallback Offline Mock an toàn khi mất mạng hoặc trong môi trường test cô lập
        return buildOfflineMockResponse(searchQuery, safePage, safeMaxResults);
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

        // --- NHÓM Y TẾ, SỨC KHỎE & TÂM LÝ ---
        allCatalog.add(createMockItem("HLT-01", "Why We Sleep: Unlocking the Power of Sleep and Dreams", List.of("Matthew Walker"),
                "Scribner", "2017", 368, "Y tế & Sức khỏe", "9781501144318",
                "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=300",
                "Nghiên cứu khoa học đột phá về tầm quan trọng sống còn của giấc ngủ đối với thể chất, tinh thần và tuổi thọ."));

        allCatalog.add(createMockItem("HLT-02", "Health Psychology: An Introduction to Behavior and Health", List.of("Linda Brannon", "Jess Feist"),
                "Wadsworth Publishing", "2017", 560, "Y tế & Tâm lý", "9781337094641",
                "https://images.unsplash.com/photo-1532012164546-f432f2e37b73?w=300",
                "Giáo trình toàn diện về mối tương quan giữa hành vi, tâm lý và sức khỏe con người, kiểm soát stress và bệnh tật."));

        allCatalog.add(createMockItem("HLT-03", "Nudge: Improving Decisions About Health, Wealth, and Happiness", List.of("Richard H. Thaler", "Cass R. Sunstein"),
                "Yale University Press", "2008", 312, "Kinh tế & Quản trị", "9780143115266",
                "https://images.unsplash.com/photo-1512820790803-83ca734da794?w=300",
                "Kiến trúc lựa chọn và kinh tế học hành vi giúp con người đưa ra quyết định tốt hơn về sức khỏe, tài chính và cuộc sống."));

        // Lọc theo từ khóa và trường chỉ định (hỗ trợ intitle:, inauthor:, subject:, isbn:)
        String lowerQuery = (query != null) ? query.toLowerCase(Locale.ROOT).trim() : "";
        String targetScope = "ALL";
        String cleanQuery = lowerQuery;

        if (cleanQuery.startsWith("intitle:") || cleanQuery.startsWith("title:")) {
            targetScope = "TITLE";
            cleanQuery = cleanQuery.replaceFirst("^(intitle:|title:)", "").trim();
        } else if (cleanQuery.startsWith("inauthor:") || cleanQuery.startsWith("author:")) {
            targetScope = "AUTHOR";
            cleanQuery = cleanQuery.replaceFirst("^(inauthor:|author:)", "").trim();
        } else if (cleanQuery.startsWith("subject:")) {
            targetScope = "CATEGORY";
            cleanQuery = cleanQuery.replaceFirst("^subject:", "").trim();
        } else if (cleanQuery.startsWith("isbn:")) {
            targetScope = "ISBN";
            cleanQuery = cleanQuery.replaceFirst("^isbn:", "").trim();
        }

        final String finalScope = targetScope;
        final String finalKeyword = cleanQuery;

        List<GoogleBooksModel2.GoogleVolumeItem2> filtered = allCatalog.stream()
                .filter(item -> {
                    GoogleBooksModel2.VolumeInfo info = item.getVolumeInfo();
                    if (info == null) return false;
                    boolean titleMatch = info.getTitle() != null && info.getTitle().toLowerCase(Locale.ROOT).contains(finalKeyword);
                    boolean authorMatch = info.getAuthors() != null && info.getAuthors().stream().anyMatch(a -> a.toLowerCase(Locale.ROOT).contains(finalKeyword));
                    boolean categoryMatch = info.getCategories() != null && info.getCategories().stream().anyMatch(c -> c.toLowerCase(Locale.ROOT).contains(finalKeyword));
                    boolean descMatch = info.getDescription() != null && info.getDescription().toLowerCase(Locale.ROOT).contains(finalKeyword);
                    boolean isbnMatch = info.getIndustryIdentifiers() != null && info.getIndustryIdentifiers().stream().anyMatch(id -> id.getIdentifier() != null && id.getIdentifier().contains(finalKeyword));

                    return switch (finalScope) {
                        case "TITLE" -> titleMatch;
                        case "AUTHOR" -> authorMatch;
                        case "CATEGORY" -> categoryMatch;
                        case "ISBN" -> isbnMatch;
                        default -> titleMatch || authorMatch || categoryMatch || descMatch || isbnMatch;
                    };
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
