package com.qiu.qoj.document.service.storage;

import com.qiu.qoj.common.exception.ApiException;
import com.qiu.qoj.document.config.ObjectStorageConfig;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import io.minio.messages.Tags;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3ObjectStorage implements ObjectStorage {
    private final MinioClient minioClient;
    private final ObjectStorageConfig config;

    @Override
    public String uploadFile(MultipartFile file, String objectName) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(config.getBucket())
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
            return getFileUrl(objectName);
        } catch (Exception e) {
            log.error("Failed to upload file", e);
            throw new ApiException("Failed to upload file", e);
        }
    }

    @Override
    public String uploadFile(MultipartFile file, String objectName, Map<String, String> userMetadata) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(config.getBucket())
                    .object(objectName)
                    .userMetadata(userMetadata)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
            return getFileUrl(objectName);
        } catch (Exception e) {
            log.error("Failed to upload file", e);
            throw new ApiException("Failed to upload file", e);
        }
    }

    @Override
    public String uploadString(String content, String objectName) {
        try {
            byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(config.getBucket())
                    .object(objectName)
                    .stream(bais, bytes.length, -1)
                    .contentType("text/plain")
                    .build());
            return getFileUrl(objectName);
        } catch (Exception e) {
            log.error("Failed to upload string content", e);
            throw new ApiException("Failed to upload string content", e);
        }
    }

    @Override
    public String uploadString(String content, String objectName, Map<String, String> userMetadata) {
        try {
            byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(config.getBucket())
                    .object(objectName)
                    .userMetadata(userMetadata)
                    .stream(bais, bytes.length, -1)
                    .contentType("text/plain")
                    .build());
            return getFileUrl(objectName);
        } catch (Exception e) {
            log.error("Failed to upload string content", e);
            throw new ApiException("Failed to upload string content", e);
        }
    }

    @Override
    public InputStream downloadFile(String objectName) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(config.getBucket())
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            log.error("Failed to download file", e);
            throw new ApiException("Failed to download file", e);
        }
    }

    @Override
    public void deleteFile(String objectName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(config.getBucket())
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            log.error("Failed to delete file", e);
            throw new ApiException("Failed to delete file", e);
        }
    }

    @Override
    public List<String> deleteFiles(List<String> objectNames) {
        List<String> failedDeletes = new ArrayList<>();
        try {
            List<DeleteObject> objects = objectNames.stream()
                    .map(name -> new DeleteObject(name))
                    .toList();

            Iterable<Result<DeleteError>> results = minioClient.removeObjects(RemoveObjectsArgs.builder()
                    .bucket(config.getBucket())
                    .objects(objects)
                    .build());

            for (Result<DeleteError> result : results) {
                DeleteError error = result.get();
                failedDeletes.add(error.objectName());
            }
        } catch (Exception e) {
            log.error("Failed to delete files", e);
            throw new ApiException("Failed to delete files", e);
        }
        return failedDeletes;
    }

    @Override
    public String getFileUrl(String objectName) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .bucket(config.getBucket())
                    .object(objectName)
                    .method(Method.GET)
                    .expiry(7, TimeUnit.DAYS)
                    .build());
        } catch (Exception e) {
            log.error("Failed to get file URL", e);
            throw new ApiException("Failed to get file URL", e);
        }
    }

    @Override
    public List<Item> listFiles(String prefix) {
        List<Item> items = new ArrayList<>();
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(config.getBucket())
                    .prefix(prefix)
                    .recursive(true)
                    .build());

            for (Result<Item> result : results) {
                items.add(result.get());
            }
        } catch (Exception e) {
            log.error("Failed to list files", e);
            throw new ApiException("Failed to list files", e);
        }
        return items;
    }


    @Override
    public Map<String, String> getFileMetadata(String objectName) {
        try {
            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(config.getBucket())
                            .object(objectName)
                            .build());
            return stat.userMetadata();
        } catch (Exception e) {
            log.error("Failed to get file metadata", e);
            throw new ApiException("Failed to get file metadata", e);
        }
    }

    @Override
    public void copyFile(String sourceObjectName, String targetObjectName) {
        try {
            minioClient.copyObject(CopyObjectArgs.builder()
                    .bucket(config.getBucket())
                    .object(targetObjectName)
                    .source(CopySource.builder()
                            .bucket(config.getBucket())
                            .object(sourceObjectName)
                            .build())
                    .build());
        } catch (Exception e) {
            log.error("Failed to copy file", e);
            throw new ApiException("Failed to copy file", e);
        }
    }

    @Override
    public void moveFile(String sourceObjectName, String targetObjectName) {
        try {
            copyFile(sourceObjectName, targetObjectName);
            deleteFile(sourceObjectName);
        } catch (Exception e) {
            log.error("Failed to move file", e);
            throw new ApiException("Failed to move file", e);
        }
    }

    @Override
    public boolean doesFileExist(String objectName) {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(config.getBucket())
                    .object(objectName)
                    .build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public long getFileSize(String objectName) {
        try {
            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(config.getBucket())
                            .object(objectName)
                            .build());
            return stat.size();
        } catch (Exception e) {
            log.error("Failed to get file size", e);
            throw new ApiException("Failed to get file size", e);
        }
    }

    @Override
    public Map<String, String> uploadFiles(List<MultipartFile> files, List<String> objectNames) {
        if (files.size() != objectNames.size()) {
            throw new IllegalArgumentException("Files and object names lists must have the same size");
        }

        Map<String, String> results = new HashMap<>();
        for (int i = 0; i < files.size(); i++) {
            String url = uploadFile(files.get(i), objectNames.get(i));
            results.put(objectNames.get(i), url);
        }
        return results;
    }

    @Override
    public Map<String, String> uploadFiles(List<MultipartFile> files, List<String> objectNames, Map<String, String> userMetadata) {
        if (files.size() != objectNames.size()) {
            throw new IllegalArgumentException("Files and object names lists must have the same size");
        }

        Map<String, String> results = new HashMap<>();
        for (int i = 0; i < files.size(); i++) {
            String url = uploadFile(files.get(i), objectNames.get(i), userMetadata);
            results.put(objectNames.get(i), url);
        }
        return results;
    }

    @Override
    public Date getLastModified(String objectName) {
        try {
            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(config.getBucket())
                            .object(objectName)
                            .build());
            return Date.from(stat.lastModified().toInstant());
        } catch (Exception e) {
            log.error("Failed to get last modified time", e);
            throw new ApiException("Failed to get last modified time", e);
        }
    }

    @Override
    public void setFileMetadata(String objectName, Map<String, String> metadata) {
        try {
            CopyObjectArgs args = CopyObjectArgs.builder()
                    .bucket(config.getBucket())
                    .object(objectName)
                    .source(CopySource.builder()
                            .bucket(config.getBucket())
                            .object(objectName)
                            .build())
                    .userMetadata(metadata)
                    .metadataDirective(Directive.REPLACE)
                    .build();
            minioClient.copyObject(args);
        } catch (Exception e) {
            log.error("Failed to set file metadata", e);
            throw new ApiException("Failed to set file metadata", e);
        }
    }

    @Override
    public Map<String, String> getFileTags(String objectName) {
        try {
            Tags tags = minioClient.getObjectTags(GetObjectTagsArgs.builder()
                    .bucket(config.getBucket())
                    .object(objectName)
                    .build());
            return tags.get();
        } catch (Exception e) {
            log.error("Failed to get file tags", e);
            throw new ApiException("Failed to get file tags", e);
        }
    }

    @Override
    public void setFileTags(String objectName, Map<String, String> tags) {
        try {
            minioClient.setObjectTags(SetObjectTagsArgs.builder()
                    .bucket(config.getBucket())
                    .object(objectName)
                    .tags(tags)
                    .build());
        } catch (Exception e) {
            log.error("Failed to set file tags", e);
            throw new ApiException("Failed to set file tags", e);
        }
    }
}