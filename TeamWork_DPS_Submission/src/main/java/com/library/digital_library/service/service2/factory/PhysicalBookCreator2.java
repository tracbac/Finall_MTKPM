package com.library.digital_library.service.service2.factory;

import com.library.digital_library.model.model2.CatalogItemRequest2;
import com.library.digital_library.model.model2.LibraryItem2;
import com.library.digital_library.model.model2.PhysicalBookItem2;
import org.springframework.stereotype.Component;

/**
 * Concrete Creator cho Sách giấy vật lý (Physical Book).
 */
@Component
public class PhysicalBookCreator2 extends LibraryItemCreator2 {

    @Override
    public String getSupportedType() {
        return "PHYSICAL";
    }

    @Override
    protected LibraryItem2 createItem(CatalogItemRequest2 request) {
        PhysicalBookItem2 physicalBook = new PhysicalBookItem2();

        String location = (request.getShelfLocation() != null && !request.getShelfLocation().isBlank())
                ? request.getShelfLocation().trim()
                : "Khu vực kho chính - Kệ P-01";
        physicalBook.setShelfLocation(location);

        return physicalBook;
    }
}

