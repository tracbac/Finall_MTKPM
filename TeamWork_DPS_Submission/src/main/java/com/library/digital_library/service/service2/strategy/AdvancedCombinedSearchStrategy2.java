package com.library.digital_library.service.service2.strategy;

import com.library.digital_library.model.Book;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Concrete Strategy 4: Tìm kiếm & Lọc kết hợp nâng cao đa tiêu chí (ADVANCED).
 * Đồng thời kết hợp:
 * - Keyword khớp trong Tiêu đề HOẶC Tác giả HOẶC Thể loại
 * - Lọc riêng theo Tác giả (nếu được truyền)
 * - Lọc riêng theo Thể loại (nếu được truyền)
 * - Lọc trạng thái còn sách trong kho (onlyAvailable == true -> quantity > 0)
 */
@Component
public class AdvancedCombinedSearchStrategy2 implements BookSearchStrategy2 {

    @Override
    public String getStrategyKey() {
        return "ADVANCED";
    }

    @Override
    public List<Book> executeSearch(List<Book> books, BookSearchFilter2 filter) {
        if (books == null || books.isEmpty()) {
            return Collections.emptyList();
        }

        if (filter == null) {
            return books;
        }

        String kw = filter.getKeyword() != null ? filter.getKeyword().trim().toLowerCase(Locale.ROOT) : "";
        String author = filter.getAuthor() != null ? filter.getAuthor().trim().toLowerCase(Locale.ROOT) : "";
        String category = filter.getCategory() != null ? filter.getCategory().trim().toLowerCase(Locale.ROOT) : "";
        boolean checkAvailability = Boolean.TRUE.equals(filter.getOnlyAvailable());

        return books.stream()
                .filter(b -> {
                    if (b == null) return false;

                    // 1. Kiểm tra tồn kho
                    if (checkAvailability && (b.getQuantity() == null || b.getQuantity() <= 0)) {
                        return false;
                    }

                    // 2. Kiểm tra tác giả cụ thể nếu có
                    if (!author.isEmpty()) {
                        if (b.getAuthor() == null || !b.getAuthor().toLowerCase(Locale.ROOT).contains(author)) {
                            return false;
                        }
                    }

                    // 3. Kiểm tra thể loại cụ thể nếu có
                    if (!category.isEmpty()) {
                        if (b.getCategory() == null || !b.getCategory().toLowerCase(Locale.ROOT).contains(category)) {
                            return false;
                        }
                    }

                    // 4. Kiểm tra từ khóa chung (khớp title HOẶC author HOẶC category)
                    if (!kw.isEmpty()) {
                        boolean titleMatch = b.getTitle() != null && b.getTitle().toLowerCase(Locale.ROOT).contains(kw);
                        boolean authorMatch = b.getAuthor() != null && b.getAuthor().toLowerCase(Locale.ROOT).contains(kw);
                        boolean categoryMatch = b.getCategory() != null && b.getCategory().toLowerCase(Locale.ROOT).contains(kw);
                        return titleMatch || authorMatch || categoryMatch;
                    }

                    return true;
                })
                .sorted(Comparator.comparing(b -> b.getTitle() != null ? b.getTitle() : "", String.CASE_INSENSITIVE_ORDER))
                .toList();
    }
}

