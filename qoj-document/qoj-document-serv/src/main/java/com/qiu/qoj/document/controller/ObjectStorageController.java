package com.qiu.qoj.document.controller;


import com.qiu.qoj.common.api.BaseResponse;
import com.qiu.qoj.document.service.storage.ObjectStorage;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/storage")
@RequiredArgsConstructor
public class ObjectStorageController {

    private final ObjectStorage objectStorage;

    @PostMapping("/upload")
    public BaseResponse<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("objectName") String objectName) {
        String url = objectStorage.uploadFile(file, objectName);
        return BaseResponse.success(url);
    }

    @PostMapping("/upload/batch")
    public BaseResponse<Map<String, String>> uploadFiles(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("objectNames") List<String> objectNames) {
        Map<String, String> urls = objectStorage.uploadFiles(files, objectNames);
        return BaseResponse.success(urls);
    }

    @PostMapping("/upload/text")
    public BaseResponse<String> uploadString(
            @RequestParam("content") String content,
            @RequestParam("objectName") String objectName) {
        String url = objectStorage.uploadString(content, objectName);
        return BaseResponse.success(url);
    }

    @GetMapping("/download/{objectName}")
    public ResponseEntity<byte[]> downloadFile(
            @PathVariable String objectName,
            @RequestParam(required = false) boolean inline) {
        try (InputStream inputStream = objectStorage.downloadFile(objectName)) {
            String encodedFileName = URLEncoder.encode(objectName, StandardCharsets.UTF_8);
            String contentDisposition = inline ? "inline" : "attachment";
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            String.format("%s; filename=\"%s\"", contentDisposition, encodedFileName))
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(IOUtils.toByteArray(inputStream));
        } catch (Exception e) {
            log.error("Failed to download file", e);
            throw new RuntimeException("Failed to download file", e);
        }
    }

    @DeleteMapping("/{objectName}")
    public BaseResponse<Void> deleteFile(@PathVariable String objectName) {
        objectStorage.deleteFile(objectName);
        return BaseResponse.success(null);
    }

    @DeleteMapping("/batch")
    public BaseResponse<List<String>> deleteFiles(@RequestBody List<String> objectNames) {
        List<String> failedDeletes = objectStorage.deleteFiles(objectNames);
        return BaseResponse.success(failedDeletes);
    }

    @GetMapping("/url/{objectName}")
    public BaseResponse<String> getFileUrl(@PathVariable String objectName) {
        String url = objectStorage.getFileUrl(objectName);
        return BaseResponse.success(url);
    }

    @GetMapping("/list")
    public BaseResponse<List<Item>> listFiles(
            @RequestParam(required = false, defaultValue = "") String prefix) {
        List<Item> items = objectStorage.listFiles(prefix);
        return BaseResponse.success(items);
    }
//
//    @GetMapping("/metadata/{objectName}")
//    public BaseResponse<Map<String, String>> getFileMetadata(@PathVariable String objectName) {
//        Map<String, String> metadata = objectStorage.getFileMetadata(objectName);
//        return BaseResponse.success(metadata);
//    }
//
//    @PutMapping("/metadata/{objectName}")
//    public BaseResponse<Void> setFileMetadata(
//            @PathVariable String objectName,
//            @RequestBody Map<String, String> metadata) {
//        objectStorage.setFileMetadata(objectName, metadata);
//        return BaseResponse.success(null);
//    }
//
//    @PostMapping("/copy")
//    public BaseResponse<Void> copyFile(
//            @RequestParam String sourceObjectName,
//            @RequestParam String targetObjectName) {
//        objectStorage.copyFile(sourceObjectName, targetObjectName);
//        return BaseResponse.success(null);
//    }
//
//    @PostMapping("/move")
//    public BaseResponse<Void> moveFile(
//            @RequestParam String sourceObjectName,
//            @RequestParam String targetObjectName) {
//        objectStorage.moveFile(sourceObjectName, targetObjectName);
//        return BaseResponse.success(null);
//    }
//
//    @GetMapping("/exists/{objectName}")
//    public BaseResponse<Boolean> doesFileExist(@PathVariable String objectName) {
//        boolean exists = objectStorage.doesFileExist(objectName);
//        return BaseResponse.success(exists);
//    }

    @GetMapping("/size/{objectName}")
    public BaseResponse<Long> getFileSize(@PathVariable String objectName) {
        long size = objectStorage.getFileSize(objectName);
        return BaseResponse.success(size);
    }

    @GetMapping("/lastModified/{objectName}")
    public BaseResponse<Date> getLastModified(@PathVariable String objectName) {
        Date lastModified = objectStorage.getLastModified(objectName);
        return BaseResponse.success(lastModified);
    }

    @GetMapping("/tags/{objectName}")
    public BaseResponse<Map<String, String>> getFileTags(@PathVariable String objectName) {
        Map<String, String> tags = objectStorage.getFileTags(objectName);
        return BaseResponse.success(tags);
    }

    @PutMapping("/tags/{objectName}")
    public BaseResponse<Void> setFileTags(
            @PathVariable String objectName,
            @RequestBody Map<String, String> tags) {
        objectStorage.setFileTags(objectName, tags);
        return BaseResponse.success(null);
    }
} 