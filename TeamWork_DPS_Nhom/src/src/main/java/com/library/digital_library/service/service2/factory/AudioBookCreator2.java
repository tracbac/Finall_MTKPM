package com.library.digital_library.service.service2.factory;

import com.library.digital_library.model.model2.AudioBookItem2;
import com.library.digital_library.model.model2.CatalogItemRequest2;
import com.library.digital_library.model.model2.LibraryItem2;
import org.springframework.stereotype.Component;

/**
 * Concrete Creator cho Sách nói (Audio Book).
 */
@Component
public class AudioBookCreator2 extends LibraryItemCreator2 {

    @Override
    public String getSupportedType() {
        return "AUDIOBOOK";
    }

    @Override
    protected LibraryItem2 createItem(CatalogItemRequest2 request) {
        AudioBookItem2 audioBook = new AudioBookItem2();

        Integer duration = (request.getDurationMinutes() != null && request.getDurationMinutes() > 0)
                ? request.getDurationMinutes()
                : 90;
        audioBook.setDurationMinutes(duration);

        String narrator = (request.getNarrator() != null && !request.getNarrator().isBlank())
                ? request.getNarrator().trim()
                : "Phát thanh viên Thư viện số";
        audioBook.setNarrator(narrator);

        String streamUrl = (request.getStreamUrl() != null && !request.getStreamUrl().isBlank())
                ? request.getStreamUrl().trim()
                : "/api/books/v2/stream/sample-" + (request.getIsbn() != null ? request.getIsbn() : "audio") + ".mp3";
        audioBook.setStreamUrl(streamUrl);

        return audioBook;
    }
}

