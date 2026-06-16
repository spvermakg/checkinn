package com.konrad.checkinn.core.service;


import com.konrad.checkinn.core.exception.StorageOperationException;
import io.minio.*;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class MinioStorageService implements StorageService {

    private final MinioClient minioClient;
    private final String bucketName;

    private static final Logger log = LoggerFactory.getLogger(MinioStorageService.class);

    public MinioStorageService(MinioClient minioClient, @Value("${minio.bucket-name}") String bucketName) {
        this.minioClient = minioClient;
        this.bucketName = bucketName;
    }

    @PostConstruct
    private void buildBucket() {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
        } catch (Exception e) {
            throw new StorageOperationException("Failed to initialize storage bucket :"+ e.getMessage());
        }
    }


    @Override
    public String uploadFile(MultipartFile file) {
        String generatedFileKey = generateFileKey(file.getOriginalFilename());
        try (InputStream inputStream = file.getInputStream()) {

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(generatedFileKey)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
        } catch (Exception e) {
            throw new StorageOperationException(StorageOperationException.FILE_UPLOAD_FAILURE);
        }

        return generatedFileKey;
    }

    private String generateFileKey(String fileName) {
        String extension = "";
        if (fileName != null && fileName.contains(".")) {
            extension = fileName.substring(fileName.lastIndexOf('.'));
        }
        return UUID.randomUUID() + extension;
    }


    @Override
    public void deleteFile(String key) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs
                            .builder()
                            .bucket(bucketName)
                            .object(key)
                            .build()
            );
        } catch (Exception e) {
            throw new StorageOperationException(StorageOperationException.FILE_DELETION_FAILURE);
        }
    }

    @Override
    public String getFileUrl(String key) {
        String fileUrl;
        try{
            fileUrl = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(key)
                            .expiry(2, TimeUnit.HOURS)
                            .build()
            );
        } catch (Exception e) {
            throw new StorageOperationException(StorageOperationException.FILE_URL_FETCH_FAILURE);
        }
        return fileUrl;
    }
}
