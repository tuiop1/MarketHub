package com.tuiop.markethub.common.storage.object.minio;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioBucketInitializer {
    @Bean
    ApplicationRunner createBucketIfMissing(
            MinioClient minioClient,
            MinioProperties properties
    ) {
        return args -> {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(properties.bucket())
                            .build()
            );

            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(properties.bucket())
                                .build()
                );
            }
        };
    }
}
