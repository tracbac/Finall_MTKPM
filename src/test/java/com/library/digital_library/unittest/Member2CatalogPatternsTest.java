package com.library.digital_library.unittest;

import com.library.digital_library.model.Book;
import com.library.digital_library.model.BookPrice;
import com.library.digital_library.model.model2.*;
import com.library.digital_library.repository.BookRepository;
import com.library.digital_library.repository.repository2.CatalogBookMetadata2Repository;
import com.library.digital_library.service.BookPriceService;
import com.library.digital_library.service.service2.BookCatalogService2;
import com.library.digital_library.service.service2.adapter.ExternalBookCatalogPort2;
import com.library.digital_library.service.service2.adapter.ExternalBookDto2;
import com.library.digital_library.service.service2.adapter.GoogleBooksAdapter2;
import com.library.digital_library.service.service2.adapter.GoogleBooksModel2;
import com.library.digital_library.service.service2.adapter.GoogleBooksRestClient2;
import com.library.digital_library.service.service2.factory.AudioBookCreator2;
import com.library.digital_library.service.service2.factory.EBookCreator2;
import com.library.digital_library.service.service2.factory.LibraryItemCreator2;
import com.library.digital_library.service.service2.factory.LibraryItemFactoryProvider2;
import com.library.digital_library.service.service2.factory.PhysicalBookCreator2;
import com.library.digital_library.service.service2.strategy.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Bộ kiểm thử toàn diện Lát cắt Kho sách & Tích hợp API ngoài (Thành viên 2).
 * Kiểm chứng 100% các nguyên lý kiến trúc và mẫu thiết kế:
 * 1. Factory Method Pattern + LSP + ISP
 * 2. Adapter Pattern (Object Adapter)
 * 3. Strategy Pattern
 * 4. Luồng tích hợp Service End-to-End
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Kiểm thử Lát cắt Kho sách & Tích hợp API ngoài (Thành viên 2)")
public class Member2CatalogPatternsTest {

    // =========================================================================
    // 1. KIỂM THỬ FACTORY METHOD PATTERN + NGUYÊN LÝ ISP & LSP
    // =========================================================================
    @Nested
    @DisplayName("1. Factory Method Pattern & Tuân thủ ISP / LSP")
    class FactoryMethodAndSolidPrinciplesTest {

        private LibraryItemFactoryProvider2 factoryProvider;

        @BeforeEach
        void setUp() {
            List<LibraryItemCreator2> creators = List.of(
                    new EBookCreator2(),
                    new AudioBookCreator2(),
                    new PhysicalBookCreator2()
            );
            factoryProvider = new LibraryItemFactoryProvider2(creators);
        }

        @Test
        @DisplayName("ISP: Mỗi Concrete Product chỉ implement đúng interface chuyên biệt")
        void testInterfaceSegregationPrinciple() {
            CatalogItemRequest2 ebookReq = CatalogItemRequest2.builder()
                    .title("Clean Architecture")
                    .author("Robert C. Martin")
                    .itemType("EBOOK")
                    .fileFormat("PDF")
                    .fileSizeMb(12.5)
                    .downloadUrl("https://library.test/clean-arch.pdf")
                    .build();

            CatalogItemRequest2 audioReq = CatalogItemRequest2.builder()
                    .title("Head First Design Patterns")
                    .author("Eric Freeman")
                    .itemType("AUDIOBOOK")
                    .durationMinutes(320)
                    .narrator("Thanh Bình")
                    .streamUrl("https://stream.library.test/hfdp.mp3")
                    .build();

            CatalogItemRequest2 physicalReq = CatalogItemRequest2.builder()
                    .title("Design Patterns GoF")
                    .author("Gang of Four")
                    .itemType("PHYSICAL")
                    .shelfLocation("Kệ A-05 Tầng 2")
                    .quantity(5)
                    .build();

            LibraryItem2 ebookItem = factoryProvider.createItem(ebookReq);
            LibraryItem2 audioItem = factoryProvider.createItem(audioReq);
            LibraryItem2 physicalItem = factoryProvider.createItem(physicalReq);

            // 1. Kiểm tra E-Book
            assertInstanceOf(DigitalDownloadable2.class, ebookItem, "EBook phải cài đặt DigitalDownloadable2");
            assertFalse(ebookItem instanceof AudioStreamable2, "EBook tuyệt đối không bị ép cài đặt AudioStreamable2");
            assertFalse(ebookItem instanceof PhysicalShelvable2, "EBook tuyệt đối không bị ép cài đặt PhysicalShelvable2");

            DigitalDownloadable2 downloadable = (DigitalDownloadable2) ebookItem;
            assertEquals("PDF", downloadable.getFileFormat());
            assertEquals(12.5, downloadable.getFileSizeMb());
            assertEquals("https://library.test/clean-arch.pdf", downloadable.getDownloadUrl());

            // 2. Kiểm tra Audio Book
            assertInstanceOf(AudioStreamable2.class, audioItem, "AudioBook phải cài đặt AudioStreamable2");
            assertFalse(audioItem instanceof DigitalDownloadable2, "AudioBook tuyệt đối không bị ép cài đặt DigitalDownloadable2");
            assertFalse(audioItem instanceof PhysicalShelvable2, "AudioBook tuyệt đối không bị ép cài đặt PhysicalShelvable2");

            AudioStreamable2 streamable = (AudioStreamable2) audioItem;
            assertEquals(320, streamable.getDurationMinutes());
            assertEquals("Thanh Bình", streamable.getNarrator());
            assertEquals("https://stream.library.test/hfdp.mp3", streamable.getStreamUrl());

            // 3. Kiểm tra Physical Book
            assertInstanceOf(PhysicalShelvable2.class, physicalItem, "PhysicalBook phải cài đặt PhysicalShelvable2");
            assertFalse(physicalItem instanceof DigitalDownloadable2, "PhysicalBook tuyệt đối không bị ép cài đặt DigitalDownloadable2");
            assertFalse(physicalItem instanceof AudioStreamable2, "PhysicalBook tuyệt đối không bị ép cài đặt AudioStreamable2");

            PhysicalShelvable2 shelvable = (PhysicalShelvable2) physicalItem;
            assertEquals("Kệ A-05 Tầng 2", shelvable.getShelfLocation());
            assertTrue(shelvable.isAvailableForPhysicalLoan());
        }

        @Test
        @DisplayName("LSP: Mọi lớp con thay thế hoàn hảo cho LibraryItem2 mà KHÔNG ném UnsupportedOperationException")
        void testLiskovSubstitutionPrinciple() {
            List<LibraryItem2> items = List.of(
                    factoryProvider.createItem(CatalogItemRequest2.builder().title("Book 1").author("Author 1").itemType("EBOOK").build()),
                    factoryProvider.createItem(CatalogItemRequest2.builder().title("Book 2").author("Author 2").itemType("AUDIOBOOK").build()),
                    factoryProvider.createItem(CatalogItemRequest2.builder().title("Book 3").author("Author 3").itemType("PHYSICAL").build())
            );

            for (LibraryItem2 item : items) {
                // Design by Contract: Không bao giờ ném UnsupportedOperationException
                assertDoesNotThrow(item::getItemType);
                assertDoesNotThrow(item::getUsageInstruction);
                assertDoesNotThrow(item::toBookEntity);

                Book book = item.toBookEntity();
                assertNotNull(book);
                assertEquals(item.getTitle(), book.getTitle());

                CatalogBookMetadata2 metadata = assertDoesNotThrow(() -> item.toMetadataEntity(book));
                assertNotNull(metadata);
                assertEquals(item.getItemType(), metadata.getItemType());
            }
        }

        @Test
        @DisplayName("Factory Method Template: Kiểm tra Validation dữ liệu đầu vào")
        void testFactoryMethodValidation() {
            EBookCreator2 creator = new EBookCreator2();

            // Null request
            assertThrows(IllegalArgumentException.class, () -> creator.buildValidatedItem(null));

            // Tiêu đề rỗng
            CatalogItemRequest2 noTitle = CatalogItemRequest2.builder().title("").author("Author").build();
            assertThrows(IllegalArgumentException.class, () -> creator.buildValidatedItem(noTitle));

            // Tác giả rỗng
            CatalogItemRequest2 noAuthor = CatalogItemRequest2.builder().title("Title").author("  ").build();
            assertThrows(IllegalArgumentException.class, () -> creator.buildValidatedItem(noAuthor));

            // Số lượng âm
            CatalogItemRequest2 negativeQty = CatalogItemRequest2.builder().title("Title").author("Author").quantity(-5).build();
            assertThrows(IllegalArgumentException.class, () -> creator.buildValidatedItem(negativeQty));

            // Giá bìa âm
            CatalogItemRequest2 negativePrice = CatalogItemRequest2.builder()
                    .title("Title").author("Author").coverPrice(BigDecimal.valueOf(-10)).build();
            assertThrows(IllegalArgumentException.class, () -> creator.buildValidatedItem(negativePrice));
        }
    }

    // =========================================================================
    // 2. KIỂM THỬ ADAPTER PATTERN (OBJECT ADAPTER)
    // =========================================================================
    @Nested
    @DisplayName("2. Adapter Pattern - Tích hợp Google Books API")
    class AdapterPatternTest {

        @Test
        @DisplayName("Adapter chuyển đổi chính xác dữ liệu từ Adaptee sang Target DTO")
        void testGoogleBooksAdapterMapping() {
            GoogleBooksModel2.GoogleVolumesResponse2 fakeResponse = GoogleBooksModel2.GoogleVolumesResponse2.builder()
                    .totalItems(1)
                    .items(List.of(
                            GoogleBooksModel2.GoogleVolumeItem2.builder()
                                    .id("GOOG-12345")
                                    .volumeInfo(GoogleBooksModel2.VolumeInfo.builder()
                                            .title("Refactoring: Improving the Design of Existing Code")
                                            .authors(List.of("Martin Fowler", "Kent Beck"))
                                            .categories(List.of("Computers / Software"))
                                            .industryIdentifiers(List.of(
                                                    GoogleBooksModel2.IndustryIdentifier.builder().type("ISBN_13").identifier("9780201485677").build(),
                                                    GoogleBooksModel2.IndustryIdentifier.builder().type("ISBN_10").identifier("0201485672").build()
                                            ))
                                            .pageCount(448)
                                            .imageLinks(GoogleBooksModel2.ImageLinks.builder().thumbnail("https://img.test/cover.jpg").build())
                                            .build())
                                    .build()
                    ))
                    .build();

            // Dùng Test Double cho Adaptee để kiểm chứng quá trình chuyển đổi (Adapt)
            GoogleBooksRestClient2 stubRestClient = new GoogleBooksRestClient2() {
                @Override
                public GoogleBooksModel2.GoogleVolumesResponse2 queryGoogleVolumesApi(String q, int maxResults) {
                    return fakeResponse;
                }
            };

            ExternalBookCatalogPort2 adapter = new GoogleBooksAdapter2(stubRestClient);
            List<ExternalBookDto2> results = adapter.searchBooks("Refactoring", 5);

            assertNotNull(results);
            assertEquals(1, results.size());

            ExternalBookDto2 dto = results.get(0);
            assertEquals("GOOG-12345", dto.getGoogleVolumeId());
            assertEquals("Refactoring: Improving the Design of Existing Code", dto.getTitle());
            assertEquals("Martin Fowler, Kent Beck", dto.getAuthor(), "Tác giả mảng phải được adapt thành chuỗi nối dấu phẩy");
            assertEquals("9780201485677", dto.getIsbn(), "Phải ưu tiên lấy ISBN_13");
            assertEquals(448, dto.getPageCount());
            assertNotNull(dto.getEstimatedCoverPrice());
            assertNotNull(dto.getEstimatedRentalPrice());
            assertEquals("https://img.test/cover.jpg", dto.getImageUrl());
        }

        @Test
        @DisplayName("Offline Fallback hoạt động bền bỉ khi mất mạng hoặc không có kết nối")
        void testOfflineFallbackSafety() {
            GoogleBooksRestClient2 clientWithFallback = new GoogleBooksRestClient2();
            GoogleBooksAdapter2 adapter = new GoogleBooksAdapter2(clientWithFallback);

            // Cho dù gọi bất kỳ từ khóa nào, hệ thống đều an toàn và trả về ít nhất 1 sách giả lập chuẩn
            List<ExternalBookDto2> books = adapter.searchBooks("Design Patterns", 2);
            assertNotNull(books);
            assertFalse(books.isEmpty(), "Fallback offline phải luôn trả về dữ liệu mẫu để hệ thống không bị crash");
            assertTrue(books.get(0).getTitle().contains("Patterns") || books.get(0).getTitle().contains("Clean") || books.get(0).getTitle().contains("Java"));
        }

        @Test
        @DisplayName("Adapter hỗ trợ phân trang (Pagination) mượt mà qua các trang")
        void testAdapterPagination() {
            GoogleBooksRestClient2 client = new GoogleBooksRestClient2();
            GoogleBooksAdapter2 adapter = new GoogleBooksAdapter2(client);

            List<ExternalBookDto2> page1 = adapter.searchBooks("java", 1, 3);
            List<ExternalBookDto2> page2 = adapter.searchBooks("java", 2, 3);

            assertNotNull(page1);
            assertNotNull(page2);
            assertFalse(page1.isEmpty());
            assertFalse(page2.isEmpty());
            // Trang 1 và trang 2 trả về các đầu sách khác nhau
            assertNotEquals(page1.get(0).getTitle(), page2.get(0).getTitle());
        }
    }

    // =========================================================================
    // 3. KIỂM THỬ STRATEGY PATTERN
    // =========================================================================
    @Nested
    @DisplayName("3. Strategy Pattern - Tìm kiếm & Lọc danh mục sách")
    class StrategyPatternTest {

        private BookSearchContext2 searchContext;
        private List<Book> mockBooks;

        @BeforeEach
        void setUp() {
            List<BookSearchStrategy2> strategies = List.of(
                    new TitleSearchStrategy2(),
                    new AuthorSearchStrategy2(),
                    new CategorySearchStrategy2(),
                    new AdvancedCombinedSearchStrategy2()
            );
            searchContext = new BookSearchContext2(strategies);

            mockBooks = new ArrayList<>();
            mockBooks.add(new Book(1L, "Design Patterns: Elements of Reusable Object-Oriented Software", "Gang of Four", "Software Engineering", 5, "img1"));
            mockBooks.add(new Book(2L, "Clean Code: A Handbook of Agile Software Craftsmanship", "Robert C. Martin", "Programming", 0, "img2"));
            mockBooks.add(new Book(3L, "Effective Java", "Joshua Bloch", "Java", 12, "img3"));
            mockBooks.add(new Book(4L, "Clean Architecture", "Robert C. Martin", "Architecture", 3, "img4"));
        }

        @Test
        @DisplayName("TITLE Strategy: Tìm theo tiêu đề và sắp xếp theo bảng chữ cái")
        void testTitleSearchStrategy() {
            BookSearchFilter2 filter = BookSearchFilter2.builder().keyword("clean").build();
            List<Book> results = searchContext.executeSearch("TITLE", mockBooks, filter);

            assertEquals(2, results.size());
            // Sắp xếp Alphabet: "Clean Architecture" đứng trước "Clean Code"
            assertEquals("Clean Architecture", results.get(0).getTitle());
            assertEquals("Clean Code: A Handbook of Agile Software Craftsmanship", results.get(1).getTitle());
        }

        @Test
        @DisplayName("AUTHOR Strategy: Tìm theo tên tác giả")
        void testAuthorSearchStrategy() {
            BookSearchFilter2 filter = BookSearchFilter2.builder().author("Robert C. Martin").build();
            List<Book> results = searchContext.executeSearch("AUTHOR", mockBooks, filter);

            assertEquals(2, results.size());
            assertTrue(results.stream().allMatch(b -> b.getAuthor().contains("Robert C. Martin")));
        }

        @Test
        @DisplayName("CATEGORY Strategy: Lọc theo thể loại sách")
        void testCategorySearchStrategy() {
            BookSearchFilter2 filter = BookSearchFilter2.builder().category("Java").build();
            List<Book> results = searchContext.executeSearch("CATEGORY", mockBooks, filter);

            assertEquals(1, results.size());
            assertEquals("Effective Java", results.get(0).getTitle());
        }

        @Test
        @DisplayName("ADVANCED Strategy: Kết hợp từ khóa và chỉ lấy sách còn tồn kho (quantity > 0)")
        void testAdvancedStrategyCombined() {
            // Sách của Robert C. Martin nhưng chỉ lấy sách còn trong kho (Clean Code có quantity = 0 nên bị loại)
            BookSearchFilter2 filter = BookSearchFilter2.builder()
                    .keyword("Robert")
                    .onlyAvailable(true)
                    .build();

            List<Book> results = searchContext.executeSearch("ADVANCED", mockBooks, filter);

            assertEquals(1, results.size());
            assertEquals("Clean Architecture", results.get(0).getTitle());
            assertTrue(results.get(0).getQuantity() > 0);
        }

        @Test
        @DisplayName("Runtime Switching: Chuyển đổi chiến lược linh hoạt và fallback an toàn")
        void testRuntimeStrategySwitching() {
            // Khóa lạ -> Tự động fallback về ADVANCED
            BookSearchStrategy2 fallbackStrategy = searchContext.getStrategy("UNKNOWN_KEY");
            assertEquals("ADVANCED", fallbackStrategy.getStrategyKey());

            // Đổi sang TITLE
            BookSearchStrategy2 titleStrategy = searchContext.getStrategy("TITLE");
            assertEquals("TITLE", titleStrategy.getStrategyKey());
        }
    }

    // =========================================================================
    // 4. KIỂM THỬ TÍCH HỢP END-TO-END SERVICE FLOW
    // =========================================================================
    @Nested
    @DisplayName("4. Luồng tích hợp Service End-to-End")
    class ServiceIntegrationFlowTest {

        @Mock
        private BookRepository bookRepository;
        @Mock
        private CatalogBookMetadata2Repository metadataRepository;
        @Mock
        private ExternalBookCatalogPort2 externalCatalogPort;

        private BookPriceService bookPriceService;
        private BookCatalogService2 catalogService;

        @BeforeEach
        void setUp() {
            // Dùng Test Double cho BookPriceService để đảm bảo độc lập tuyệt đối và chạy mượt trên mọi phiên bản JVM
            bookPriceService = new BookPriceService(null) {
                @Override
                public BookPrice save(Book book, BigDecimal rentalPrice, BigDecimal coverPrice) {
                    BookPrice p = new BookPrice();
                    p.setId(200L);
                    p.setBook(book);
                    p.setRentalPrice(rentalPrice);
                    p.setCoverPrice(coverPrice);
                    return p;
                }

                @Override
                public Map<Long, BookPrice> findByBookIds(Collection<Long> bookIds) {
                    if (bookIds.contains(1L)) {
                        BookPrice p = new BookPrice();
                        p.setId(201L);
                        p.setRentalPrice(BigDecimal.valueOf(15000));
                        p.setCoverPrice(BigDecimal.valueOf(120000));
                        return Map.of(1L, p);
                    }
                    return Collections.emptyMap();
                }
            };

            LibraryItemFactoryProvider2 factoryProvider = new LibraryItemFactoryProvider2(List.of(
                    new EBookCreator2(),
                    new AudioBookCreator2(),
                    new PhysicalBookCreator2()
            ));

            BookSearchContext2 searchContext = new BookSearchContext2(List.of(
                    new TitleSearchStrategy2(),
                    new AuthorSearchStrategy2(),
                    new CategorySearchStrategy2(),
                    new AdvancedCombinedSearchStrategy2()
            ));

            catalogService = new BookCatalogService2(
                    bookRepository,
                    bookPriceService,
                    metadataRepository,
                    factoryProvider,
                    externalCatalogPort,
                    searchContext
            );
        }

        @Test
        @DisplayName("Quy trình Import: Google Books -> Adapter -> Factory Method -> Lưu Database")
        void testEndToEndImportFromGoogleBooksFlow() {
            // 1. Giả lập Adapter trả về sách từ Google
            ExternalBookDto2 mockGoogleBook = ExternalBookDto2.builder()
                    .googleVolumeId("VOL-999")
                    .title("Domain-Driven Design")
                    .author("Eric Evans")
                    .category("Software Architecture")
                    .isbn("9780321125217")
                    .estimatedCoverPrice(BigDecimal.valueOf(180000))
                    .estimatedRentalPrice(BigDecimal.valueOf(25000))
                    .build();

            when(externalCatalogPort.findBookByIsbn("9780321125217")).thenReturn(Optional.of(mockGoogleBook));

            // 2. Giả lập Database lưu trữ
            when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> {
                Book b = invocation.getArgument(0);
                b.setId(100L);
                return b;
            });

            when(metadataRepository.save(any(CatalogBookMetadata2.class))).thenAnswer(invocation -> {
                CatalogBookMetadata2 m = invocation.getArgument(0);
                m.setId(300L);
                return m;
            });

            // 3. Thực thi import
            GoogleImportRequest2 importRequest = GoogleImportRequest2.builder()
                    .isbn("9780321125217")
                    .itemType("EBOOK")
                    .quantity(15)
                    .fileFormat("EPUB")
                    .build();

            CatalogBookDetailResponse2 response = catalogService.importFromGoogleBooks(importRequest);

            // 4. Kiểm tra kết quả
            assertNotNull(response);
            assertEquals(100L, response.getId());
            assertEquals("Domain-Driven Design", response.getTitle());
            assertEquals("Eric Evans", response.getAuthor());
            assertEquals("EBOOK", response.getItemType());
            assertEquals("EPUB", response.getFileFormat());
            assertEquals("9780321125217", response.getIsbn());
            assertEquals(15, response.getQuantity());

            // 5. Xác minh các tương tác giữa các thành phần
            verify(externalCatalogPort, times(1)).findBookByIsbn("9780321125217");
            verify(bookRepository, times(1)).save(any(Book.class));
            verify(metadataRepository, times(1)).save(any(CatalogBookMetadata2.class));
        }

        @Test
        @DisplayName("Quy trình tìm kiếm: Kết hợp Strategy Pattern và nạp dữ liệu đa bảng")
        void testSearchCatalogWithStrategy() {
            Book book1 = new Book(1L, "Design Patterns", "GoF", "Computers", 5, "img1");
            Book book2 = new Book(2L, "Clean Architecture", "Robert Martin", "Computers", 0, "img2");
            when(bookRepository.findAll()).thenReturn(List.of(book1, book2));

            when(metadataRepository.findAllByBook_IdIn(anyList())).thenReturn(List.of(
                    CatalogBookMetadata2.builder().book(book1).itemType("PHYSICAL").shelfLocation("Kệ 01").build()
            ));

            List<CatalogBookDetailResponse2> results = catalogService.searchCatalog(
                    "Patterns", null, null, false, "TITLE");

            assertEquals(1, results.size());
            assertEquals("Design Patterns", results.get(0).getTitle());
            assertEquals("PHYSICAL", results.get(0).getItemType());
            assertEquals("Kệ 01", results.get(0).getShelfLocation());
        }
    }
}
