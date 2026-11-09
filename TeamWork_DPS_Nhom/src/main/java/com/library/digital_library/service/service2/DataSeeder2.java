package com.library.digital_library.service.service2;

import com.library.digital_library.model.Book;
import com.library.digital_library.model.BookPrice;
import com.library.digital_library.model.User;
import com.library.digital_library.model.model3.EBookFile3;
import com.library.digital_library.repository.BookPriceRepository;
import com.library.digital_library.repository.BookRepository;
import com.library.digital_library.repository.UserRepository;
import com.library.digital_library.repository.repository3.EBookFile3Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Tự động khởi tạo dữ liệu mẫu phong phú (tài khoản, kho sách, giá mượn/giá bìa, E-Book DRM)
 * khi cơ sở dữ liệu chưa có dữ liệu.
 */
@RestController
@RequestMapping("/api/books/seed")
public class DataSeeder2 implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder2.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BookRepository bookRepository;
    private final BookPriceRepository bookPriceRepository;
    private final EBookFile3Repository eBookFile3Repository;

    public DataSeeder2(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            BookRepository bookRepository,
            BookPriceRepository bookPriceRepository,
            EBookFile3Repository eBookFile3Repository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.bookRepository = bookRepository;
        this.bookPriceRepository = bookPriceRepository;
        this.eBookFile3Repository = eBookFile3Repository;
    }

    @Override
    public void run(String... args) {
        seedUsers();
        seedBooks();
    }

    @PostMapping
    @GetMapping
    public ResponseEntity<String> triggerSeed() {
        seedUsers();
        int count = seedBooks();
        return ResponseEntity.ok("Kho sách hiện có " + count + " cuốn sách mẫu sẵn sàng.");
    }

    @Transactional
    public void seedUsers() {
        if (userRepository.count() == 0) {
            log.info("[DataSeeder2] Khởi tạo tài khoản người dùng mặc định...");

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

            log.info("[DataSeeder2] Đã tạo 2 tài khoản: admin / admin123 (ADMIN) và reader / 123456 (USER)");
        }
    }

    @Transactional
    public int seedBooks() {
        if (bookRepository.count() > 0) {
            log.info("[DataSeeder2] Kho sách đã có {} cuốn, không tạo lại.", bookRepository.count());
            return (int) bookRepository.count();
        }

        log.info("[DataSeeder2] Đang nạp danh mục sách mẫu phong phú vào cơ sở dữ liệu...");

        List<SeedBookData> sampleBooks = List.of(
            // Công nghệ thông tin
            new SeedBookData("Clean Architecture", "Robert C. Martin", "Công nghệ thông tin", 10,
                "https://images.unsplash.com/photo-1532012164546-f432f2e3edd4?w=500", 280000, 25000, true),
            new SeedBookData("Design Patterns: Reusable Elements", "Erich Gamma, Richard Helm", "Công nghệ thông tin", 8,
                "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=500", 350000, 32000, true),
            new SeedBookData("Clean Code: A Handbook of Agile Software", "Robert C. Martin", "Công nghệ thông tin", 12,
                "https://images.unsplash.com/photo-1512820790803-83ca734da794?w=500", 250000, 22000, true),
            new SeedBookData("The Pragmatic Programmer", "David Thomas, Andrew Hunt", "Công nghệ thông tin", 7,
                "https://images.unsplash.com/photo-1516979187457-637abb4f9353?w=500", 320000, 28000, false),
            new SeedBookData("Refactoring: Improving Code Design", "Martin Fowler", "Công nghệ thông tin", 6,
                "https://images.unsplash.com/photo-1589829085413-56de8ae18c73?w=500", 310000, 27000, false),
            new SeedBookData("Introduction to Algorithms (CLRS)", "Thomas H. Cormen", "Công nghệ thông tin", 5,
                "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=500", 450000, 39000, false),

            // Văn học & Nghệ thuật
            new SeedBookData("Nhà Giả Kim (The Alchemist)", "Paulo Coelho", "Văn học", 20,
                "https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=500", 95000, 10000, true),
            new SeedBookData("Rừng Na Uy (Norwegian Wood)", "Haruki Murakami", "Văn học", 15,
                "https://images.unsplash.com/photo-1497633762265-9d179a990aa6?w=500", 140000, 14000, false),
            new SeedBookData("Cây Cam Ngọt Của Tôi", "José Mauro de Vasconcelos", "Văn học", 14,
                "https://images.unsplash.com/photo-1495446815901-a7297e633e8d?w=500", 110000, 12000, false),
            new SeedBookData("Hoàng Tử Bé (The Little Prince)", "Antoine de Saint-Exupéry", "Văn học", 18,
                "https://images.unsplash.com/photo-1476275466078-4007374efbbe?w=500", 85000, 9000, true),
            new SeedBookData("Chiến Binh Cầu Vồng", "Andrea Hirata", "Văn học", 10,
                "https://images.unsplash.com/photo-1506880018603-83d5b814b5a6?w=500", 125000, 13000, false),

            // Kỹ năng & Tâm lý
            new SeedBookData("Đắc Nhân Tâm", "Dale Carnegie", "Kỹ năng sống", 25,
                "https://images.unsplash.com/photo-1543002588-bfa74002ed7e?w=500", 100000, 10000, true),
            new SeedBookData("Atomic Habits: Thay Đổi Tí Hon", "James Clear", "Kỹ năng sống", 16,
                "https://images.unsplash.com/photo-1512820790803-83ca734da794?w=500", 185000, 18000, true),
            new SeedBookData("Tư Duy Nhanh Và Chậm", "Daniel Kahneman", "Kỹ năng sống", 9,
                "https://images.unsplash.com/photo-1457369804613-52c61a468e7d?w=500", 220000, 20000, false),
            new SeedBookData("Lối Sống Tối Giản Của Người Nhật", "Sasaki Fumio", "Kỹ năng sống", 12,
                "https://images.unsplash.com/photo-1499244571948-7ccddb3583f1?w=500", 115000, 12000, false),

            // Kinh tế & Khởi nghiệp
            new SeedBookData("Khởi Nghiệp Tinh Gọn (The Lean Startup)", "Eric Ries", "Kinh tế", 11,
                "https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=500", 190000, 18000, false),
            new SeedBookData("Cha Giàu Cha Nghèo", "Robert Kiyosaki", "Kinh tế", 15,
                "https://images.unsplash.com/photo-1554224155-8d04cb21cd6c?w=500", 130000, 13000, true),
            new SeedBookData("Từ Không Đến Một (Zero to One)", "Peter Thiel", "Kinh tế", 10,
                "https://images.unsplash.com/photo-1522202176988-66273c2fd55f?w=500", 160000, 15000, false),

            // Lịch sử & Triết học
            new SeedBookData("Sapiens: Lược Sử Loài Người", "Yuval Noah Harari", "Lịch sử & Triết học", 14,
                "https://images.unsplash.com/photo-1461360370896-922624d12aa1?w=500", 260000, 25000, false),
            new SeedBookData("Lược Sử Thời Gian", "Stephen Hawking", "Lịch sử & Triết học", 8,
                "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=500", 175000, 16000, false)
        );

        for (SeedBookData item : sampleBooks) {
            Book book = new Book();
            book.setTitle(item.title);
            book.setAuthor(item.author);
            book.setCategory(item.category);
            book.setQuantity(item.quantity);
            book.setImageUrl(item.imageUrl);
            Book savedBook = bookRepository.save(book);

            BookPrice price = new BookPrice();
            price.setBook(savedBook);
            price.setCoverPrice(BigDecimal.valueOf(item.coverPrice));
            price.setRentalPrice(BigDecimal.valueOf(item.rentalPrice));
            bookPriceRepository.save(price);

            if (item.hasEBook) {
                String safeName = item.title.toLowerCase().replaceAll("[^a-z0-9]", "-") + ".pdf";
                EBookFile3 ebook = EBookFile3.builder()
                    .book(savedBook)
                    .storagePath("samples/" + safeName)
                    .fileName(safeName)
                    .fileSize(10485760L)
                    .updatedAt(LocalDateTime.now())
                    .build();
                eBookFile3Repository.save(ebook);
            }
        }

        log.info("[DataSeeder2] Hoàn tất nạp {} cuốn sách mẫu vào kho sách!", sampleBooks.size());
        return sampleBooks.size();
    }

    private static class SeedBookData {
        final String title;
        final String author;
        final String category;
        final int quantity;
        final String imageUrl;
        final double coverPrice;
        final double rentalPrice;
        final boolean hasEBook;

        SeedBookData(String title, String author, String category, int quantity, String imageUrl,
                     double coverPrice, double rentalPrice, boolean hasEBook) {
            this.title = title;
            this.author = author;
            this.category = category;
            this.quantity = quantity;
            this.imageUrl = imageUrl;
            this.coverPrice = coverPrice;
            this.rentalPrice = rentalPrice;
            this.hasEBook = hasEBook;
        }
    }
}
