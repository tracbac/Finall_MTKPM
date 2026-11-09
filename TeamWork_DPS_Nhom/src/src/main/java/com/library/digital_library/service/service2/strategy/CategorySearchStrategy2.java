package com.library.digital_library.service.service2.strategy;

import com.library.digital_library.model.Book;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Concrete Strategy 3: Lọc danh mục sách theo Thể loại (CATEGORY).
 */
@Component
public class CategorySearchStrategy2 implements BookSearchStrategy2 {

    @Override
    public String getStrategyKey() {
        return "CATEGORY";
    }

    @Override
    public List<Book> executeSearch(List<Book> books, BookSearchFilter2 filter) {
        if (books == null || books.isEmpty()) {
            return Collections.emptyList();
        }

        String targetCategory = "";
        if (filter != null) {
            if (filter.getCategory() != null && !filter.getCategory().isBlank()) {
                targetCategory = filter.getCategory().trim().toLowerCase(Locale.ROOT);
            } else if (filter.getKeyword() != null) {
                targetCategory = filter.getKeyword().trim().toLowerCase(Locale.ROOT);
            }
        }

        final String finalCategory = targetCategory;
        return books.stream()
                .filter(b -> b != null && b.getCategory() != null
                        && b.getCategory().toLowerCase(Locale.ROOT).contains(finalCategory))
                .sorted(Comparator.comparing(b -> b.getTitle() != null ? b.getTitle() : "", String.CASE_INSENSITIVE_ORDER))
                .toList();
    }
}

