package com.example.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

/**
 * Auto-wires an {@link S3Client}/{@link FileStorageService} for any service that adds
 * common-storage as a dependency and sets S3_ENDPOINT + S3_BUCKET (the same env vars
 * as .env.example). AWS_ACCESS_KEY_ID/AWS_SECRET_ACCESS_KEY/AWS_REGION are picked up
 * by the AWS SDK's default provider chain, no extra config needed.
 *
 * S3_ENDPOINT and S3_PUBLIC_ENDPOINT are deliberately separate: S3_ENDPOINT is what
 * *this service* uses to reach MinIO (in Docker, the in-network hostname, e.g.
 * http://minio:9000 — unreachable from a browser), while S3_PUBLIC_ENDPOINT is what
 * gets baked into the URLs handed back to clients (a browser-reachable host, e.g.
 * http://localhost:9000 in dev or a real public domain in production). Defaults to
 * S3_ENDPOINT when unset, which is correct for non-Docker local runs where both
 * resolve the same way.
 */
@Configuration
@ConditionalOnProperty(name = "S3_ENDPOINT")
public class StorageAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public S3Client s3Client(
            @Value("${S3_ENDPOINT}") String endpoint,
            @Value("${AWS_REGION:us-east-1}") String region) {
        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public FileStorageService fileStorageService(
            S3Client s3Client,
            @Value("${S3_BUCKET}") String bucket,
            @Value("${S3_PUBLIC_ENDPOINT:${S3_ENDPOINT}}") String publicEndpoint) {
        return new S3FileStorageService(s3Client, bucket, publicEndpoint);
    }
}
