package com.library.digital_library.service;

import org.springframework.web.multipart.MultipartFile;

public interface BookCoverStorage {
    String upload(MultipartFile file);
}
