package com.library.digital_library.service.service2.factory;

import com.library.digital_library.model.model2.CatalogItemRequest2;
import com.library.digital_library.model.model2.LibraryItem2;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lớp điều phối Factory Method Pattern (Factory Provider).
 * Sử dụng Constructor Injection để tự động thu thập mọi Creator có trong Spring Context,
 * đảm bảo tuân thủ nghiêm ngặt Open-Closed Principle (OCP) và Dependency Inversion Principle (DIP).
 */
@Component
public class LibraryItemFactoryProvider2 {

    private final Map<String, LibraryItemCreator2> creatorRegistry = new ConcurrentHashMap<>();

    public LibraryItemFactoryProvider2(List<LibraryItemCreator2> creators) {
        if (creators != null) {
            for (LibraryItemCreator2 creator : creators) {
                creatorRegistry.put(creator.getSupportedType().toUpperCase(Locale.ROOT), creator);
            }
        }
    }

    /**
     * Lấy Creator tương ứng với loại tài liệu.
     * Mặc định là EBOOK nếu không truyền hoặc để trống.
     */
    public LibraryItemCreator2 getCreator(String itemType) {
        if (itemType == null || itemType.isBlank()) {
            itemType = "EBOOK";
        }
        String normalizedType = itemType.trim().toUpperCase(Locale.ROOT);
        LibraryItemCreator2 creator = creatorRegistry.get(normalizedType);
        if (creator == null) {
            throw new IllegalArgumentException("Không hỗ trợ loại tài liệu: '" + itemType + "'. Các loại hợp lệ: " + creatorRegistry.keySet());
        }
        return creator;
    }

    /**
     * Phương thức tiện ích để khởi tạo và kiểm tra tính hợp lệ của tài liệu trực tiếp từ Request.
     */
    public LibraryItem2 createItem(CatalogItemRequest2 request) {
        if (request == null) {
            throw new IllegalArgumentException("Yêu cầu CatalogItemRequest2 không được là null.");
        }
        LibraryItemCreator2 creator = getCreator(request.getItemType());
        return creator.buildValidatedItem(request);
    }

    /**
     * Lấy danh sách các loại tài liệu hiện đang được hỗ trợ trong hệ thống.
     */
    public Map<String, LibraryItemCreator2> getRegisteredCreators() {
        return Collections.unmodifiableMap(creatorRegistry);
    }
}

