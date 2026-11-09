package com.library.digital_library.service.service2;

import com.library.digital_library.model.Book;
import com.library.digital_library.model.BookPrice;
import com.library.digital_library.model.model2.*;
import com.library.digital_library.repository.BookRepository;
import com.library.digital_library.repository.repository2.CatalogBookMetadata2Repository;
import com.library.digital_library.service.BookPriceService;
import com.library.digital_library.service.service2.adapter.ExternalBookCatalogPort2;
import com.library.digital_library.service.service2.adapter.ExternalBookDto2;
import com.library.digital_library.service.service2.factory.LibraryItemFactoryProvider2;
import com.library.digital_library.service.service2.strategy.BookSearchContext2;
import com.library.digital_library.service.service2.strategy.BookSearchFilter2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service điều phối Lát cắt Kho sách & Tích hợp API ngoài (Thành viên 2).
 * Tích hợp liền mạch cả 3 Design Patterns:
 * 1. Factory Method Pattern (LibraryItemFactoryProvider2)
 * 2. Adapter Pattern (ExternalBookCatalogPort2 / GoogleBooksAdapter2)
 * 3. Strategy Pattern (BookSearchContext2)
 */
@Service
public class BookCatalogService2 {

    private final BookRepository bookRepository;
    private final BookPriceService bookPriceService;
    private final CatalogBookMetadata2Repository metadataRepository;
    private final LibraryItemFactoryProvider2 factoryProvider;
    private final ExternalBookCatalogPort2 externalCatalogPort;
    private final BookSearchContext2 searchContext;

    public BookCatalogService2(BookRepository bookRepository,
                               BookPriceService bookPriceService,
                               CatalogBookMetadata2Repository metadataRepository,
                               LibraryItemFactoryProvider2 factoryProvider,
                               ExternalBookCatalogPort2 externalCatalogPort,
                               BookSearchContext2 searchContext) {
        this.bookRepository = bookRepository;
        this.bookPriceService = bookPriceService;
        this.metadataRepository = metadataRepository;
        this.factoryProvider = factoryProvider;
        this.externalCatalogPort = externalCatalogPort;
        this.searchContext = searchContext;
    }

    /**
     * Tìm kiếm và lọc danh mục sách kết hợp dữ liệu toàn diện (Dùng Strategy Pattern).
     */
    @Transactional(readOnly = true)
    public List<CatalogBookDetailResponse2> searchCatalog(String keyword, String author, String category,
                                                         Boolean onlyAvailable, String strategyKey) {
        List<Book> allBooks = bookRepository.findAll();

        BookSearchFilter2 filter = BookSearchFilter2.builder()
                .keyword(keyword)
                .author(author)
                .category(category)
                .onlyAvailable(onlyAvailable)
                .build();

        // 1. Áp dụng Strategy Pattern để lọc danh sách sách
        List<Book> matchedBooks = searchContext.executeSearch(strategyKey, allBooks, filter);

        if (matchedBooks.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> bookIds = matchedBooks.stream().map(Book::getId).toList();
        Map<Long, BookPrice> priceMap = bookPriceService.findByBookIds(bookIds);
        Map<Long, CatalogBookMetadata2> metadataMap = metadataRepository.findAllByBook_IdIn(bookIds).stream()
                .collect(Collectors.toMap(m -> m.getBook().getId(), Function.identity(), (a, b) -> a));

        return matchedBooks.stream().map(book -> {
            BookPrice price = priceMap.get(book.getId());
            CatalogBookMetadata2 metadata = metadataMap.get(book.getId());
            String instruction = buildInstruction(book, metadata);
            return CatalogBookDetailResponse2.from(book, price, metadata, instruction);
        }).toList();
    }

    /**
     * Xem trước danh sách sách từ Google Books API (Dùng Adapter Pattern, trang mặc định là 1).
     */
    public List<ExternalBookDto2> previewFromGoogleBooks(String query, int maxResults) {
        return externalCatalogPort.searchBooks(query, maxResults);
    }

    /**
     * Xem trước danh sách sách từ Google Books API có hỗ trợ phân trang (page).
     */
    public List<ExternalBookDto2> previewFromGoogleBooks(String query, int page, int maxResults) {
        if (page <= 1) {
            return previewFromGoogleBooks(query, maxResults);
        }
        return externalCatalogPort.searchBooks(query, page, maxResults);
    }

    /**
     * Import sách tự động từ Google Books API vào hệ thống:
     * Quy trình: Google Books -> Adapter Pattern -> Factory Method Pattern -> Lưu Database.
     */
    @Transactional
    public CatalogBookDetailResponse2 importFromGoogleBooks(GoogleImportRequest2 request) {
        if (request == null) {
            throw new IllegalArgumentException("Yêu cầu import không được để trống (null).");
        }

        ExternalBookDto2 externalBook = null;

        // Ưu tiên tra cứu theo ISBN nếu có
        if (request.getIsbn() != null && !request.getIsbn().isBlank()) {
            externalBook = externalCatalogPort.findBookByIsbn(request.getIsbn()).orElse(null);
        }

        // Nếu không tìm thấy theo ISBN, tra cứu theo từ khóa
        if (externalBook == null) {
            String query = (request.getQuery() != null && !request.getQuery().isBlank())
                    ? request.getQuery()
                    : request.getIsbn();
            if (query == null || query.isBlank()) {
                throw new IllegalArgumentException("Cần cung cấp ít nhất từ khóa (query) hoặc mã ISBN để import.");
            }
            List<ExternalBookDto2> results = externalCatalogPort.searchBooks(query, 1);
            if (!results.isEmpty()) {
                externalBook = results.get(0);
            }
        }

        if (externalBook == null) {
            throw new IllegalArgumentException("Không tìm thấy thông tin sách từ Google Books API với dữ liệu đã cung cấp.");
        }

        // Chuyển đổi dữ liệu từ Google sang DTO của Factory
        CatalogItemRequest2 itemRequest = CatalogItemRequest2.builder()
                .title(externalBook.getTitle())
                .author(externalBook.getAuthor())
                .category(externalBook.getCategory())
                .isbn(externalBook.getIsbn())
                .imageUrl(externalBook.getImageUrl())
                .description(externalBook.getDescription())
                .googleVolumeId(externalBook.getGoogleVolumeId())
                .quantity(request.getQuantity() != null ? request.getQuantity() : 10)
                .itemType(request.getItemType() != null ? request.getItemType() : "EBOOK")
                .coverPrice(request.getCustomCoverPrice() != null ? request.getCustomCoverPrice() : externalBook.getEstimatedCoverPrice())
                .rentalPrice(request.getCustomRentalPrice() != null ? request.getCustomRentalPrice() : externalBook.getEstimatedRentalPrice())
                .fileFormat(request.getFileFormat())
                .narrator(request.getNarrator())
                .shelfLocation(request.getShelfLocation())
                .build();

        return createBookViaFactory(itemRequest);
    }

    /**
     * Tạo mới sách bằng Factory Method Pattern và lưu đồng bộ dữ liệu vào cơ sở dữ liệu.
     */
    @Transactional
    public CatalogBookDetailResponse2 createBookViaFactory(CatalogItemRequest2 request) {
        // 1. Dùng Factory Method Pattern để khởi tạo và validate đối tượng tương ứng
        LibraryItem2 item = factoryProvider.createItem(request);

        // 2. Lưu Book vào bảng books cốt lõi
        Book bookEntity = item.toBookEntity();
        Book savedBook = bookRepository.save(bookEntity);

        // 3. Lưu giá vào bảng book_prices qua BookPriceService
        BookPrice savedPrice = bookPriceService.save(savedBook, item.getRentalPrice(), item.getCoverPrice());

        // 4. Lưu metadata chuyên biệt vào bảng catalog_book_metadata_2
        CatalogBookMetadata2 metadataEntity = item.toMetadataEntity(savedBook);
        if (request.getGoogleVolumeId() != null) {
            metadataEntity.setGoogleVolumeId(request.getGoogleVolumeId());
        }
        CatalogBookMetadata2 savedMetadata = metadataRepository.save(metadataEntity);

        return CatalogBookDetailResponse2.from(savedBook, savedPrice, savedMetadata, item.getUsageInstruction());
    }

    /**
     * Tra cứu chi tiết một tài liệu cùng toàn bộ metadata chuyên biệt theo ID.
     */
    @Transactional(readOnly = true)
    public CatalogBookDetailResponse2 getBookDetail(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy sách với ID = " + bookId));

        Map<Long, BookPrice> prices = bookPriceService.findByBookIds(List.of(bookId));
        BookPrice price = prices.get(bookId);

        CatalogBookMetadata2 metadata = metadataRepository.findByBook_Id(bookId).orElse(null);
        String instruction = buildInstruction(book, metadata);

        return CatalogBookDetailResponse2.from(book, price, metadata, instruction);
    }

    private String buildInstruction(Book book, CatalogBookMetadata2 metadata) {
        if (metadata == null) {
            return "Sách truyền thống trong thư viện (" + book.getQuantity() + " cuốn có sẵn).";
        }
        String type = metadata.getItemType() != null ? metadata.getItemType().toUpperCase() : "PHYSICAL";
        return switch (type) {
            case "EBOOK" -> "Sách điện tử định dạng " + (metadata.getFileFormat() != null ? metadata.getFileFormat() : "PDF")
                    + ". Có thể đọc trực tuyến hoặc tải về tại " + metadata.getDownloadUrl();
            case "AUDIOBOOK" -> "Sách nói thuyết minh bởi " + (metadata.getNarrator() != null ? metadata.getNarrator() : "AI")
                    + " (" + metadata.getAudioDurationMinutes() + " phút). Phát trực tuyến tại " + metadata.getStreamUrl();
            case "PHYSICAL" -> "Sách in tại vị trí kệ: [" + metadata.getShelfLocation() + "]. "
                    + (book.getQuantity() > 0 ? "Sẵn sàng cho mượn tại quầy." : "Tạm hết.");
            default -> "Tài liệu lưu trữ.";
        };
    }
}

