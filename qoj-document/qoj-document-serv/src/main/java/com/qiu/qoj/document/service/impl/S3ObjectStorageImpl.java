package com.qiu.qoj.document.service.impl;

import com.qiu.qoj.common.exception.Asserts;
import com.qiu.qoj.document.config.ObjectStorageConfig;
import com.qiu.qoj.document.constant.DocumentConstant;
import com.qiu.qoj.document.model.dto.file.FileDTO;
import com.qiu.qoj.document.service.ObjectStorage;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3ObjectStorageImpl implements ObjectStorage {

    private final MinioClient minioClient;
    private final ObjectStorageConfig config;

    @Override
    public String uploadFile(FileDTO fileDTO) throws Exception {
        MultipartFile file = fileDTO.getFile();
        String path = fileDTO.getPath();
        Boolean overwrite = fileDTO.getOverwrite();

        // 文件校验
        validateFile(file);

        // 检查文件是否已存在(如果不允许覆盖)
        if (!overwrite && doesObjectExist(path)) {
            throw new RuntimeException("File already exists: " + path);
        }

        // 执行上传
        try (InputStream inputStream = file.getInputStream()) {
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(config.getBucket())
                    .object(path)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build();

            minioClient.putObject(args);
            return path;
        }
    }

    @Override
    public void deleteFile(String path) throws Exception {
        RemoveObjectArgs args = RemoveObjectArgs.builder()
                .bucket(config.getBucket())
                .object(path)
                .build();

        minioClient.removeObject(args);
    }

    @Override
    public String getPresignedUrl(String path, int expireMinutes) throws Exception {
        GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs.builder()
                .method(Method.GET)
                .bucket(config.getBucket())
                .object(path)
                .expiry(expireMinutes, TimeUnit.MINUTES)
                .build();

        return minioClient.getPresignedObjectUrl(args);
    }

    @Override
    public boolean doesObjectExist(String path) throws Exception {
        try {
            StatObjectArgs args = StatObjectArgs.builder()
                    .bucket(config.getBucket())
                    .object(path)
                    .build();

            minioClient.statObject(args);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getTextContent(String path, long maxSize) throws Exception {
        GetObjectArgs args = GetObjectArgs.builder()
                .bucket(config.getBucket())
                .object(path)
                .build();

        try (GetObjectResponse response = minioClient.getObject(args)) {
            // 限制读取大小
            byte[] bytes = IOUtils.toByteArray(response, maxSize);
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }

    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
        // 检查文件大小
        Asserts.failIf(file.getSize() > DocumentConstant.MAX_FILE_SIZE,
                "File size exceeds limit: " + DocumentConstant.MAX_FILE_SIZE + " bytes");

        // 检查文件类型
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        Asserts.failIf(!DocumentConstant.ALLOW_FILE_TYPE.contains(extension.toLowerCase()),
                "File type not allowed: " + extension);
    }

    /**
     * 初始化存储桶(如果不存在)
     */
    public void initBucket() throws Exception {
        boolean bucketExists = minioClient.bucketExists(
                BucketExistsArgs.builder()
                        .bucket(config.getBucket())
                        .build());

        if (!bucketExists) {
            minioClient.makeBucket(
                    MakeBucketArgs.builder()
                            .bucket(config.getBucket())
                            .build());
            log.info("Created bucket: {}", config.getBucket());
        }
    }
}