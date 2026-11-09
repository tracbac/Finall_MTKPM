package com.library.digital_library.service.service2.strategy;

import com.library.digital_library.model.Book;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Context trong Strategy Pattern.
 * Đóng vai trò môi trường ngữ cảnh thực thi các chiến lược tìm kiếm và lọc sách khác nhau.
 * Cho phép Client hoán đổi linh hoạt thuật toán lọc sách tại Runtime mà không làm thay đổi mã nguồn (OCP).
 */
@Component
public class BookSearchContext2 {

    private final Map<String, BookSearchStrategy2> strategyRegistry = new ConcurrentHashMap<>();

    public BookSearchContext2(List<BookSearchStrategy2> strategies) {
        if (strategies != null) {
            for (BookSearchStrategy2 strategy : strategies) {
                strategyRegistry.put(strategy.getStrategyKey().toUpperCase(Locale.ROOT), strategy);
            }
        }
    }

    /**
     * Lấy chiến lược tìm kiếm tương ứng với khóa.
     * Mặc định là ADVANCED nếu khóa null hoặc không tìm thấy.
     */
    public BookSearchStrategy2 getStrategy(String strategyKey) {
        if (strategyKey == null || strategyKey.isBlank()) {
            strategyKey = "ADVANCED";
        }
        String normalizedKey = strategyKey.trim().toUpperCase(Locale.ROOT);
        BookSearchStrategy2 strategy = strategyRegistry.get(normalizedKey);
        if (strategy == null) {
            // Fallback an toàn về chiến lược kết hợp nâng cao
            return strategyRegistry.getOrDefault("ADVANCED", new AdvancedCombinedSearchStrategy2());
        }
        return strategy;
    }

    /**
     * Thực thi chiến lược tìm kiếm tại Runtime.
     */
    public List<Book> executeSearch(String strategyKey, List<Book> books, BookSearchFilter2 filter) {
        BookSearchStrategy2 strategy = getStrategy(strategyKey);
        return strategy.executeSearch(books, filter);
    }

    /**
     * Danh sách tất cả các chiến lược tìm kiếm đã đăng ký trong hệ thống.
     */
    public Map<String, BookSearchStrategy2> getRegisteredStrategies() {
        return Collections.unmodifiableMap(strategyRegistry);
    }
}

