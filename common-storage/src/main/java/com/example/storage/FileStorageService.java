package com.example.storage;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    /**
     * Uploads {@code file} under {@code folder} in the configured bucket using a
     * generated object key, so callers never need to worry about name collisions.
     */
    StoredFile upload(MultipartFile file, String folder);

    void delete(String objectKey);
}
