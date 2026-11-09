package com.library.digital_library.controller;

import com.library.digital_library.dto.EBookFileResponse3;
import com.library.digital_library.service.EBookFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/ebooks")
@RequiredArgsConstructor
public class AdminEBookFileController {

    private final EBookFileService eBookFileService;

    @PostMapping(path = "/{bookId}/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EBookFileResponse3> upload(
            @PathVariable Long bookId,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(eBookFileService.upload(bookId, file));
    }
}
