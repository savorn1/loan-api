package com.example.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@RequiredArgsConstructor
public class S3FileStorageService implements FileStorageService {

    private final S3Client s3Client;
    private final String bucket;
    private final String publicBaseUrl;

    @Override
    public StoredFile upload(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new StorageException("Cannot upload an empty file");
        }

        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String objectKey = "%s/%s%s".formatted(
                folder,
                UUID.randomUUID(),
                StringUtils.hasText(extension) ? "." + extension : "");

        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(objectKey)
                            .contentType(file.getContentType())
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException e) {
            throw new StorageException("Failed to read file for upload", e);
        }

        String url = "%s/%s/%s".formatted(stripTrailingSlash(publicBaseUrl), bucket, objectKey);
        return new StoredFile(objectKey, url);
    }

    @Override
    public void delete(String objectKey) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .build());
    }

    private static String stripTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
