package com.library.digital_library.service.service2.strategy;

import com.library.digital_library.model.Book;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Concrete Strategy 2: Tìm kiếm sách theo Tác giả (AUTHOR).
 * Không phân biệt hoa/thường, hỗ trợ trích xuất tên tác giả từ filter.
 */
@Component
public class AuthorSearchStrategy2 implements BookSearchStrategy2 {

    @Override
    public String getStrategyKey() {
        return "AUTHOR";
    }

    @Override
    public List<Book> executeSearch(List<Book> books, BookSearchFilter2 filter) {
        if (books == null || books.isEmpty()) {
            return Collections.emptyList();
        }

        String targetAuthor = "";
        if (filter != null) {
            if (filter.getAuthor() != null && !filter.getAuthor().isBlank()) {
                targetAuthor = filter.getAuthor().trim().toLowerCase(Locale.ROOT);
            } else if (filter.getKeyword() != null) {
                targetAuthor = filter.getKeyword().trim().toLowerCase(Locale.ROOT);
            }
        }

        final String finalAuthor = targetAuthor;
        return books.stream()
                .filter(b -> b != null && b.getAuthor() != null
                        && b.getAuthor().toLowerCase(Locale.ROOT).contains(finalAuthor))
                .sorted(Comparator.comparing(b -> b.getAuthor() != null ? b.getAuthor() : "", String.CASE_INSENSITIVE_ORDER))
                .toList();
    }
}

