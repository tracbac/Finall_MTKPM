package com.library.digital_library.unittest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.library.digital_library.model.Book;
import com.library.digital_library.model.model3.EBookFile3;
import com.library.digital_library.repository.BookRepository;
import com.library.digital_library.repository.repository3.EBookFile3Repository;
import com.library.digital_library.service.EBookFileService;
import com.library.digital_library.service.SupabaseEBookStorage;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class EBookFileServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private EBookFile3Repository eBookFileRepository;

    @Mock
    private SupabaseEBookStorage storage;

    @InjectMocks
    private EBookFileService eBookFileService;

    private Book book;

    @BeforeEach
    void setUp() {
        book = new Book();
        book.setId(8L);
        book.setTitle("Digital Library");
    }

    @Test
    void uploadsPdfAndStoresOnlyPrivateObjectMetadata() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "digital-library.pdf", "application/pdf", "%PDF-1.7 body".getBytes());
        when(bookRepository.findById(8L)).thenReturn(Optional.of(book));
        when(eBookFileRepository.findByBookId(8L)).thenReturn(Optional.empty());
        when(eBookFileRepository.save(any(EBookFile3.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = eBookFileService.upload(8L, file);

        assertEquals(8L, response.bookId());
        assertEquals("digital-library.pdf", response.fileName());
        assertEquals((long) file.getSize(), response.fileSize());
        verify(storage).upload("ebook-8.pdf", file);
        verify(eBookFileRepository).save(any(EBookFile3.class));
    }

    @Test
    void rejectsNonPdfContentBeforeSendingItToStorage() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "not-a-pdf.pdf", "application/pdf", "not a PDF".getBytes());
        when(bookRepository.findById(8L)).thenReturn(Optional.of(book));
        doThrow(new IllegalArgumentException("Tệp tải lên không phải PDF hợp lệ."))
                .when(storage).upload("ebook-8.pdf", file);

        assertThrows(IllegalArgumentException.class, () -> eBookFileService.upload(8L, file));

        verify(storage).upload("ebook-8.pdf", file);
        verify(eBookFileRepository, never()).save(any(EBookFile3.class));
    }

    @Test
    void rejectsFilesWithoutPdfExtension() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "ebook.txt", "application/pdf", "%PDF-1.7 body".getBytes());
        when(bookRepository.findById(8L)).thenReturn(Optional.of(book));

        assertThrows(IllegalArgumentException.class, () -> eBookFileService.upload(8L, file));

        verify(storage, never()).upload(any(), any());
    }
}
