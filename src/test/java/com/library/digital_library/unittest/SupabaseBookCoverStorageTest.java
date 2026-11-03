package com.library.digital_library.unittest;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import com.library.digital_library.service.BookCoverStorageException;
import com.library.digital_library.service.SupabaseBookCoverStorage;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

class SupabaseBookCoverStorageTest {

    @Test
    void includesSupabaseErrorDetailsWhenUploadIsRejected() {
        String baseUrl = "https://library-test.supabase.co";
        AtomicReference<RestTemplate> usedTemplate = new AtomicReference<>();
        SupabaseBookCoverStorage storage = new SupabaseBookCoverStorage(
                new RestTemplateBuilder().additionalCustomizers(usedTemplate::set),
                baseUrl,
                "service-role-key",
                "book_prices");
        MockRestServiceServer server = MockRestServiceServer.bindTo(usedTemplate.get()).build();
        server.expect(requestTo(org.hamcrest.Matchers.startsWith(
                        baseUrl + "/storage/v1/object/book_prices/")))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"statusCode\":\"400\",\"error\":\"InvalidMimeType\",\"message\":\"mime type not allowed\"}"));

        BookCoverStorageException exception = org.junit.jupiter.api.Assertions.assertThrows(
                BookCoverStorageException.class,
                () -> storage.upload(new MockMultipartFile(
                        "imageFile", "cover.png", "image/png", new byte[] {1, 2, 3})));

        assertTrue(exception.getMessage().contains("HTTP 400"));
        assertTrue(exception.getMessage().contains("mime type not allowed"));
        server.verify();
    }
}
