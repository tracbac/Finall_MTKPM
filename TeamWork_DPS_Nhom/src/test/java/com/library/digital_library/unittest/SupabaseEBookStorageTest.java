package com.library.digital_library.unittest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.library.digital_library.service.SupabaseEBookStorage;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

class SupabaseEBookStorageTest {

    private static final String STORAGE_BASE_URL = "https://library-test.supabase.co";

    @Test
    void streamsPrivatePdfUsingTheServerServiceKey() {
        AtomicReference<RestTemplate> usedTemplate = new AtomicReference<>();
        RestTemplateBuilder builder = new RestTemplateBuilder()
                .additionalCustomizers(usedTemplate::set);
        SupabaseEBookStorage storage = new SupabaseEBookStorage(
                builder, STORAGE_BASE_URL, "service-role-key", "ebooks");
        MockRestServiceServer server = MockRestServiceServer.bindTo(usedTemplate.get()).build();
        byte[] pdf = "%PDF-1.7".getBytes();
        server.expect(requestTo(STORAGE_BASE_URL + "/storage/v1/object/ebooks/ebook-8.pdf"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer service-role-key"))
                .andRespond(withSuccess(pdf, MediaType.APPLICATION_PDF));

        byte[] result = storage.download("ebook-8.pdf");

        assertArrayEquals(pdf, result);
        server.verify();
    }

    @Test
    void validatesPdfSignatureBeforeUpload() {
        AtomicReference<RestTemplate> usedTemplate = new AtomicReference<>();
        SupabaseEBookStorage storage = new SupabaseEBookStorage(
                new RestTemplateBuilder().additionalCustomizers(usedTemplate::set),
                STORAGE_BASE_URL,
                "service-role-key",
                "ebooks");
        MockRestServiceServer server = MockRestServiceServer.bindTo(usedTemplate.get()).build();
        MockMultipartFile file = new MockMultipartFile(
                "file", "wrong.pdf", "application/pdf", "plain text".getBytes());

        assertThrows(IllegalArgumentException.class, () -> storage.upload("ebook-8.pdf", file));

        server.verify();
    }

    @Test
    void uploadsPdfToPrivateBucketUsingServiceKey() {
        AtomicReference<RestTemplate> usedTemplate = new AtomicReference<>();
        SupabaseEBookStorage storage = new SupabaseEBookStorage(
                new RestTemplateBuilder().additionalCustomizers(usedTemplate::set),
                STORAGE_BASE_URL,
                "service-role-key",
                "ebooks");
        MockRestServiceServer server = MockRestServiceServer.bindTo(usedTemplate.get()).build();
        server.expect(requestTo(STORAGE_BASE_URL + "/storage/v1/object/ebooks/ebook-8.pdf"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer service-role-key"))
                .andExpect(header("x-upsert", "true"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));
        MockMultipartFile file = new MockMultipartFile(
                "file", "digital-library.pdf", "application/pdf", "%PDF-1.7 body".getBytes());

        storage.upload("ebook-8.pdf", file);

        server.verify();
    }

    @Test
    void deletesPrivateObjectByStoragePath() {
        AtomicReference<RestTemplate> usedTemplate = new AtomicReference<>();
        SupabaseEBookStorage storage = new SupabaseEBookStorage(
                new RestTemplateBuilder().additionalCustomizers(usedTemplate::set),
                STORAGE_BASE_URL,
                "service-role-key",
                "ebooks");
        MockRestServiceServer server = MockRestServiceServer.bindTo(usedTemplate.get()).build();
        server.expect(requestTo(STORAGE_BASE_URL + "/storage/v1/object/ebooks"))
                .andExpect(method(HttpMethod.DELETE))
                .andExpect(header("Authorization", "Bearer service-role-key"))
                .andExpect(content().json("{\"prefixes\":[\"ebook-8.pdf\"]}"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        storage.delete("ebook-8.pdf");

        server.verify();
    }
}
