package com.tuiop.markethub.common.storage.object;

import com.tuiop.markethub.common.storage.object.exceptions.StorageException;
import com.tuiop.markethub.common.storage.object.minio.MinioProperties;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class MinioObjectStorageService implements ObjectStorageService {

    private final MinioClient minioClient;
    private final MinioProperties properties;

    @Override
    public void upload(
            String objectKey,
            InputStream inputStream,
            long size,
            String contentType
    ) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(properties.bucket())
                            .object(objectKey)
                            .stream(inputStream, size, -1)
                            .contentType(contentType)
                            .build()
            );
        } catch (Exception e) {
            throw new StorageException("Failed to upload object to MinIO", e);
        }
    }

    @Override
    public InputStream download(String objectKey) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(properties.bucket())
                            .object(objectKey)
                            .build()
            );
        } catch (Exception e) {
            throw new StorageException("Failed to download object from MinIO", e);
        }
    }

    @Override
    public void delete(String objectKey) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(properties.bucket())
                            .object(objectKey)
                            .build()
            );
        } catch (Exception e) {
            throw new StorageException("Failed to delete object from MinIO", e);
        }
    }
}