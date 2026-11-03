package com.library.digital_library.service.service2;

import com.library.digital_library.model.User;
import com.library.digital_library.model.model2.CatalogItemRequest2;
import com.library.digital_library.repository.BookRepository;
import com.library.digital_library.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Tự động khởi tạo dữ liệu mẫu phong phú khi chạy ứng dụng (Thành viên 2).
 * Tận dụng chính Factory Method Pattern để sinh ra các loại E-Book, Audio Book và Sách giấy,
 * giúp giao diện hiển thị ngay lập tức khi chạy Offline/Local mà không cần kết nối Supabase.
 */
@Component
public class DataSeeder2 implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder2.class);

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final BookCatalogService2 catalogService;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder2(BookRepository bookRepository,
                       UserRepository userRepository,
                       BookCatalogService2 catalogService,
                       PasswordEncoder passwordEncoder) {
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.catalogService = catalogService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUsersIfEmpty();
        seedBooksIfEmpty();
    }

    private void seedUsersIfEmpty() {
        if (userRepository.count() > 0) {
            return;
        }

        log.info("[DataSeeder2] Đang nạp tài khoản người dùng mẫu...");

        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@library.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole("ADMIN");
        userRepository.save(admin);

        User reader = new User();
        reader.setUsername("reader");
        reader.setEmail("reader@library.com");
        reader.setPassword(passwordEncoder.encode("123456"));
        reader.setRole("USER");
        userRepository.save(reader);

        log.info("[DataSeeder2] Đã tạo 2 tài khoản mẫu: admin / admin123 (ADMIN) và reader / 123456 (USER)");
    }

    private void seedBooksIfEmpty() {
        if (bookRepository.count() > 0) {
            return;
        }

        log.info("[DataSeeder2] Đang nạp danh mục sách mẫu qua Factory Method Pattern...");

        // 1. E-Book mẫu 1: Head First Design Patterns
        catalogService.createBookViaFactory(CatalogItemRequest2.builder()
                .title("Head First Design Patterns")
                .author("Eric Freeman, Elisabeth Robson")
                .category("Công nghệ thông tin")
                .isbn("9781492078005")
                .quantity(30)
                .coverPrice(BigDecimal.valueOf(280000))
                .rentalPrice(BigDecimal.valueOf(25000))
                .imageUrl("https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=600")
                .description("Cẩm nang kinh điển trực quan và hài hước về 23 mẫu thiết kế hướng đối tượng: Factory, Strategy, Adapter, Decorator...")
                .itemType("EBOOK")
                .fileFormat("PDF")
                .fileSizeMb(14.8)
                .downloadUrl("https://digital-library.demo/ebooks/head-first-design-patterns.pdf")
                .build());

        // 2. E-Book mẫu 2: Clean Architecture
        catalogService.createBookViaFactory(CatalogItemRequest2.builder()
                .title("Clean Architecture: A Craftsman's Guide")
                .author("Robert C. Martin (Uncle Bob)")
                .category("Kiến trúc phần mềm")
                .isbn("9780134494166")
                .quantity(25)
                .coverPrice(BigDecimal.valueOf(320000))
                .rentalPrice(BigDecimal.valueOf(30000))
                .imageUrl("https://images.unsplash.com/photo-1512820790803-83ca734da794?w=600")
                .description("Quy tắc cấu trúc và thiết kế hệ thống phần mềm chuyên nghiệp, giải thích chuyên sâu nguyên lý SOLID.")
                .itemType("EBOOK")
                .fileFormat("EPUB")
                .fileSizeMb(8.2)
                .downloadUrl("https://digital-library.demo/ebooks/clean-architecture.epub")
                .build());

        // 3. Audio Book mẫu 1: Atomic Habits
        catalogService.createBookViaFactory(CatalogItemRequest2.builder()
                .title("Atomic Habits - Thói Quen Nguyên Tử")
                .author("James Clear")
                .category("Kỹ năng sống")
                .isbn("9780735211292")
                .quantity(50)
                .coverPrice(BigDecimal.valueOf(189000))
                .rentalPrice(BigDecimal.valueOf(15000))
                .imageUrl("https://images.unsplash.com/photo-1506126613408-eca07ce68773?w=600")
                .description("Thay đổi tí hon mang lại kết quả bất ngờ. Chiến lược hình thành thói quen tốt và từ bỏ thói quen xấu.")
                .itemType("AUDIOBOOK")
                .durationMinutes(335)
                .narrator("Giọng đọc phát thanh viên Mai Linh")
                .streamUrl("https://digital-library.demo/audio/atomic-habits.mp3")
                .build());

        // 4. Audio Book mẫu 2: Đắc Nhân Tâm
        catalogService.createBookViaFactory(CatalogItemRequest2.builder()
                .title("Đắc Nhân Tâm (How to Win Friends and Influence People)")
                .author("Dale Carnegie")
                .category("Tâm lý - Xã hội")
                .isbn("9780671027032")
                .quantity(40)
                .coverPrice(BigDecimal.valueOf(120000))
                .rentalPrice(BigDecimal.valueOf(12000))
                .imageUrl("https://images.unsplash.com/photo-1497633762265-9d179a990aa6?w=600")
                .description("Cuốn sách nghệ thuật thu phục lòng người bán chạy nhất mọi thời đại.")
                .itemType("AUDIOBOOK")
                .durationMinutes(270)
                .narrator("Nghệ sĩ ưu tú Minh Trí")
                .streamUrl("https://digital-library.demo/audio/dac-nhan-tam.mp3")
                .build());

        // 5. Sách giấy mẫu 1: GoF Design Patterns
        catalogService.createBookViaFactory(CatalogItemRequest2.builder()
                .title("Design Patterns: Elements of Reusable Object-Oriented Software")
                .author("Erich Gamma, Richard Helm, Ralph Johnson, John Vlissides")
                .category("Mẫu thiết kế")
                .isbn("9780201633610")
                .quantity(10)
                .coverPrice(BigDecimal.valueOf(450000))
                .rentalPrice(BigDecimal.valueOf(35000))
                .imageUrl("https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?w=600")
                .description("Tác phẩm gốc của nhóm Gang of Four (GoF) định nghĩa 23 mẫu thiết kế hướng đối tượng nền tảng.")
                .itemType("PHYSICAL")
                .shelfLocation("Kho chính - Kệ Công nghệ A-01 (Tầng 1)")
                .build());

        // 6. Sách giấy mẫu 2: Effective Java
        catalogService.createBookViaFactory(CatalogItemRequest2.builder()
                .title("Effective Java (3rd Edition)")
                .author("Joshua Bloch")
                .category("Lập trình Java")
                .isbn("9780134685991")
                .quantity(8)
                .coverPrice(BigDecimal.valueOf(380000))
                .rentalPrice(BigDecimal.valueOf(30000))
                .imageUrl("https://images.unsplash.com/photo-1532012164546-f432f2e37b73?w=600")
                .description("Cẩm nang thực hành lập trình Java tốt nhất với 90 quy tắc kiến trúc và thiết kế code chất lượng cao.")
                .itemType("PHYSICAL")
                .shelfLocation("Kho chính - Kệ Java B-03 (Tầng 2)")
                .build());

        // 7. Sách giấy mẫu 3: Nhà Giả Kim
        catalogService.createBookViaFactory(CatalogItemRequest2.builder()
                .title("Nhà Giả Kim (The Alchemist)")
                .author("Paulo Coelho")
                .category("Văn học - Tiểu thuyết")
                .isbn("9780062315007")
                .quantity(15)
                .coverPrice(BigDecimal.valueOf(89000))
                .rentalPrice(BigDecimal.valueOf(8000))
                .imageUrl("https://images.unsplash.com/photo-1495446815901-a7297e633e8d?w=600")
                .description("Hành trình theo đuổi vận mệnh và giấc mơ của chàng chăn cừu Santiago qua sa mạc Bắc Phi.")
                .itemType("PHYSICAL")
                .shelfLocation("Khu Văn học - Kệ C-08 (Tầng 1)")
                .build());

        log.info("[DataSeeder2] Khởi tạo thành công 7 cuốn sách mẫu (E-Book, AudioBook, PhysicalBook) vào cơ sở dữ liệu!");
    }
}

