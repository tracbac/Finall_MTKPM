package com.library.digital_library.service.service2.strategy;

import com.library.digital_library.model.Book;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Concrete Strategy 1: Tìm kiếm sách theo Tiêu đề (TITLE).
 * Không phân biệt hoa/thường, xử lý null an toàn và sắp xếp thứ tự bảng chữ cái theo tiêu đề.
 */
@Component
public class TitleSearchStrategy2 implements BookSearchStrategy2 {

    @Override
    public String getStrategyKey() {
        return "TITLE";
    }

    @Override
    public List<Book> executeSearch(List<Book> books, BookSearchFilter2 filter) {
        if (books == null || books.isEmpty()) {
            return Collections.emptyList();
        }

        String searchTitle = (filter != null && filter.getKeyword() != null)
                ? filter.getKeyword().trim().toLowerCase(Locale.ROOT)
                : "";

        return books.stream()
                .filter(b -> b != null && b.getTitle() != null
                        && b.getTitle().toLowerCase(Locale.ROOT).contains(searchTitle))
                .sorted(Comparator.comparing(b -> b.getTitle() != null ? b.getTitle() : "", String.CASE_INSENSITIVE_ORDER))
                .toList();
    }
}

