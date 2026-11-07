package com.library.digital_library.service.service2.factory;

import com.library.digital_library.model.model2.CatalogItemRequest2;
import com.library.digital_library.model.model2.EBookItem2;
import com.library.digital_library.model.model2.LibraryItem2;
import org.springframework.stereotype.Component;

/**
 * Concrete Creator cho Sách điện tử (E-Book).
 */
@Component
public class EBookCreator2 extends LibraryItemCreator2 {

    @Override
    public String getSupportedType() {
        return "EBOOK";
    }

    @Override
    protected LibraryItem2 createItem(CatalogItemRequest2 request) {
        EBookItem2 eBook = new EBookItem2();

        String format = (request.getFileFormat() != null && !request.getFileFormat().isBlank())
                ? request.getFileFormat().trim().toUpperCase()
                : "PDF";
        eBook.setFileFormat(format);

        Double size = (request.getFileSizeMb() != null && request.getFileSizeMb() > 0)
                ? request.getFileSizeMb()
                : 5.0;
        eBook.setFileSizeMb(size);

        String downloadUrl = (request.getDownloadUrl() != null && !request.getDownloadUrl().isBlank())
                ? request.getDownloadUrl().trim()
                : "/api/books/v2/download/sample-" + (request.getIsbn() != null ? request.getIsbn() : "ebook") + "." + format.toLowerCase();
        eBook.setDownloadUrl(downloadUrl);

        return eBook;
    }
}

