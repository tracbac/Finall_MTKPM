package com.library.digital_library.service.service2.factory;

import com.library.digital_library.model.model2.CatalogItemRequest2;
import com.library.digital_library.model.model2.LibraryItem2;

import java.math.BigDecimal;

/**
 * Creator trừu tượng trong Factory Method Pattern.
 * Đóng vai trò vừa là khung khởi tạo (Template Method), vừa định nghĩa Factory Method trừu tượng
 * để các lớp con quyết định cụ thể loại đối tượng LibraryItem2 cần tạo.
 */
public abstract class LibraryItemCreator2 {

    /**
     * Định danh loại tài liệu mà Creator này hỗ trợ (EBOOK, AUDIOBOOK, PHYSICAL).
     */
    public abstract String getSupportedType();

    /**
     * Factory Method trừu tượng - Lớp con cụ thể sẽ quyết định tạo EBookItem2, AudioBookItem2, hay PhysicalBookItem2.
     */
    protected abstract LibraryItem2 createItem(CatalogItemRequest2 request);

    /**
     * Template Method: Tiền xử lý, kiểm tra tính hợp lệ dữ liệu và điều phối việc tạo đối tượng.
     */
    public final LibraryItem2 buildValidatedItem(CatalogItemRequest2 request) {
        if (request == null) {
            throw new IllegalArgumentException("Thông tin yêu cầu tạo tài liệu không được để trống (null).");
        }

        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Tiêu đề sách (title) là bắt buộc và không được để trống.");
        }

        if (request.getAuthor() == null || request.getAuthor().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên tác giả (author) là bắt buộc và không được để trống.");
        }

        if (request.getQuantity() != null && request.getQuantity() < 0) {
            throw new IllegalArgumentException("Số lượng sách (quantity) phải >= 0.");
        }

        if (request.getCoverPrice() != null && request.getCoverPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Giá bìa (coverPrice) phải >= 0.");
        }

        if (request.getRentalPrice() != null && request.getRentalPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Giá thuê (rentalPrice) phải >= 0.");
        }

        // Thiết lập giá trị mặc định hợp lý nếu chưa có
        if (request.getQuantity() == null) {
            request.setQuantity(1);
        }
        if (request.getCoverPrice() == null) {
            request.setCoverPrice(BigDecimal.ZERO);
        }
        if (request.getRentalPrice() == null) {
            request.setRentalPrice(BigDecimal.ZERO);
        }

        // Gọi Factory Method do Concrete Creator cài đặt
        LibraryItem2 item = createItem(request);

        // Bảo đảm các thuộc tính cơ sở kế thừa được gán đầy đủ
        item.setTitle(request.getTitle().trim());
        item.setAuthor(request.getAuthor().trim());
        item.setCategory(request.getCategory() != null ? request.getCategory().trim() : "Chung");
        item.setIsbn(request.getIsbn() != null ? request.getIsbn().trim() : null);
        item.setQuantity(request.getQuantity());
        item.setImageUrl(request.getImageUrl());
        item.setDescription(request.getDescription());
        item.setCoverPrice(request.getCoverPrice());
        item.setRentalPrice(request.getRentalPrice());

        return item;
    }
}

