package com.qiu.qoj.document.controller;


import com.qiu.qoj.common.api.BaseResponse;
import com.qiu.qoj.common.api.UserContext;
import com.qiu.qoj.common.exception.ApiException;
import com.qiu.qoj.common.exception.Asserts;
import com.qiu.qoj.document.constant.DocumentConstant;
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
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/storage")
@RequiredArgsConstructor
public class ObjectStorageController {


    private final ObjectStorage objectStorage;

    // todo 每个用户都有自己的空间容量限制

    @PostMapping("/upload")
    public BaseResponse<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("objectName") String objectName) {
        checkFileBeforeUpload(file.getSize(), objectName);
        objectName = wrapObjectNameWithUserInfo(objectName);

        String url = objectStorage.uploadFile(file, objectName);
        return BaseResponse.success(url);
    }


    @PostMapping("/upload/batch")
    public BaseResponse<Map<String, String>> uploadFiles(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("objectNames") List<String> objectNames) {
        Asserts.failIf(files.size() != objectNames.size(), "文件数量和对象名称数量不一致");
        for (int i = 0; i < files.size(); i++) {
            checkFileBeforeUpload(files.get(i).getSize(), objectNames.get(i));
            objectNames.set(i, wrapObjectNameWithUserInfo(objectNames.get(i)));
        }

        Map<String, String> urls = objectStorage.uploadFiles(files, objectNames, Map.of(DocumentConstant.USER_ID, UserContext.getUserId().toString()));
        return BaseResponse.success(urls);
    }

    @PostMapping("/upload/text")
    public BaseResponse<String> uploadString(
            @RequestParam("content") String content,
            @RequestParam("objectName") String objectName) {
        objectName = wrapObjectNameWithUserInfo(objectName);
        checkFileBeforeUpload(content.length() * 2L, objectName);
        String url = objectStorage.uploadString(content, objectName);
        return BaseResponse.success(url);
    }

    @GetMapping("/download/{objectName}")
    public ResponseEntity<byte[]> downloadFile(
            @PathVariable String objectName,
            @RequestParam(required = false) boolean inline) {
        String rawObjectName = objectName;
        objectName = wrapObjectNameWithUserInfo(objectName);
        checkOwnership(objectName);

        try (InputStream inputStream = objectStorage.downloadFile(objectName)) {
            String encodedFileName = URLEncoder.encode(rawObjectName, StandardCharsets.UTF_8);
            String contentDisposition = inline ? "inline" : "attachment";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            String.format("%s; filename=\"%s\"", contentDisposition, encodedFileName))
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(IOUtils.toByteArray(inputStream));
        } catch (Exception e) {
            log.error("Failed to download file", e);
            throw new ApiException("Failed to download file", e);
        }
    }

    private void checkOwnership(String objectName) {
        Map<String, String> fileMetadata = objectStorage.getFileMetadata(objectName);
        Asserts.failIf(!UserContext.isAdmin()
                        &&
                        fileMetadata.containsKey(DocumentConstant.USER_ID)
                        &&
                        !UserContext.getUserId().toString().equals(fileMetadata.get(DocumentConstant.USER_ID)),
                "You are not the owner of this file");
    }

    @DeleteMapping("/{objectName}")
    public BaseResponse<Void> deleteFile(@PathVariable String objectName) {
        objectName = wrapObjectNameWithUserInfo(objectName);
        checkOwnership(objectName);

        objectStorage.deleteFile(objectName);
        return BaseResponse.success(null);
    }

    @DeleteMapping("/batch")
    public BaseResponse<List<String>> deleteFiles(@RequestBody List<String> objectNames) {

        objectNames.stream()
                .map(this::wrapObjectNameWithUserInfo)
                .forEach(this::checkOwnership);
        List<String> failedDeletes = objectStorage.deleteFiles(objectNames);
        return BaseResponse.success(failedDeletes);
    }

    @GetMapping("/url/{objectName}")
    public BaseResponse<String> getFileUrl(@PathVariable String objectName) {
        objectName = wrapObjectNameWithUserInfo(objectName);
        checkOwnership(objectName);
        String url = objectStorage.getFileUrl(objectName);
        return BaseResponse.success(url);
    }

    /**
     * 获取当前用户的所有文件
     *
     * @return
     */
    @GetMapping("/user/files")
    public BaseResponse<List<String>> listFileNames() {
        List<Item> items = objectStorage.listFiles(wrapObjectNameWithUserInfo(""));
        List<String> rawFileNames = items.stream()
                .filter(item -> !item.isDir())
                .map(item -> unwrapObjectNameWithUserInfo(item.objectName()))
                .collect(Collectors.toList());
        return BaseResponse.success(rawFileNames);
    }


    @GetMapping("/size/{objectName}")
    public BaseResponse<Long> getFileSize(@PathVariable String objectName) {
        objectName = wrapObjectNameWithUserInfo(objectName);
        checkOwnership(objectName);
        long size = objectStorage.getFileSize(objectName);
        return BaseResponse.success(size);
    }

    @GetMapping("/lastModified/{objectName}")
    public BaseResponse<Date> getLastModified(@PathVariable String objectName) {
        objectName = wrapObjectNameWithUserInfo(objectName);
        checkOwnership(objectName);
        Date lastModified = objectStorage.getLastModified(objectName);
        return BaseResponse.success(lastModified);
    }

    @GetMapping("/tags/{objectName}")
    public BaseResponse<Map<String, String>> getFileTags(@PathVariable String objectName) {
        objectName = wrapObjectNameWithUserInfo(objectName);
        checkOwnership(objectName);
        Map<String, String> tags = objectStorage.getFileTags(objectName);
        return BaseResponse.success(tags);
    }

    @PutMapping("/tags/{objectName}")
    public BaseResponse<Void> setFileTags(
            @PathVariable String objectName,
            @RequestBody Map<String, String> tags) {
        objectName = wrapObjectNameWithUserInfo(objectName);
        checkOwnership(objectName);
        objectStorage.setFileTags(objectName, tags);
        return BaseResponse.success(null);
    }

    private String wrapObjectNameWithUserInfo(String objectName) {
        return DocumentConstant.USER_PREFIX + UserContext.getUserId().toString() + "/" + objectName;
    }

    private String unwrapObjectNameWithUserInfo(String objectName) {
        return objectName.substring(DocumentConstant.USER_PREFIX.length() + UserContext.getUserId().toString().length() + 1);
    }

    private static void checkFileBeforeUpload(Long bytes, String objectName) {
        Asserts.failIf(bytes > DocumentConstant.MAX_FILE_SIZE, "文件大小不能超过" + DocumentConstant.MAX_FILE_SIZE / 1024 + "MB");
        // 文件必须包含后缀名
        Asserts.failIf(!objectName.contains(".") || objectName.lastIndexOf(".") == objectName.length() - 1, "文件名必须包含后缀名");
        // 检查文件后缀
        Asserts.failIf(!DocumentConstant.ALLOW_FILE_TYPE.contains(objectName.substring(objectName.lastIndexOf(".") + 1)), "不允许上传该类型的文件");
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
} 