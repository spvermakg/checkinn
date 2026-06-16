package com.konrad.checkinn.core.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String uploadFile(MultipartFile file);
    void deleteFile(String key);
    String getFileUrl(String key);
}
