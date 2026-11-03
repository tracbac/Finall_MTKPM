package com.library.digital_library.model.model2;

/**
 * Interface Segregation Principle (ISP) - Giao diện chuyên biệt cho sách giấy vật lý trên giá/kệ thư viện.
 * Giúp quản lý vị trí kệ sách và trạng thái sẵn sàng cho mượn tại chỗ.
 */
public interface PhysicalShelvable2 {
    String getShelfLocation();
    boolean isAvailableForPhysicalLoan();
}

