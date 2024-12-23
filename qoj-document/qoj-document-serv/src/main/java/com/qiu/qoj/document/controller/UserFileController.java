package com.qiu.qoj.document.controller;

import com.qiu.qoj.common.api.BaseResponse;
import com.qiu.qoj.document.model.dto.file.FilePreviewDTO;
import com.qiu.qoj.document.model.entity.UserFile;
import com.qiu.qoj.document.service.UserFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class UserFileController {

    private final UserFileService userFileService;

    @PostMapping("/upload")
    public BaseResponse<String> uploadFile(
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false, defaultValue = "/") String parentPath) throws Exception {

        String path = userFileService.uploadFile(file, parentPath);
        return BaseResponse.success(path);

    }

    @PostMapping("/folder")
    public BaseResponse<Void> createFolder(
            @RequestParam String name,
            @RequestParam(required = false, defaultValue = "/") String parentPath) {
        userFileService.createFolder(name, parentPath);
        return BaseResponse.success(null);
    }

    @DeleteMapping
    public BaseResponse<Void> delete(@RequestParam String path) {
        userFileService.delete(path);
        return BaseResponse.success(null);
    }

    @GetMapping("/url")
    public BaseResponse<String> getFileUrl(@RequestParam String path) throws Exception {

        String url = userFileService.getFileUrl(path);
        return BaseResponse.success(url);

    }

    @PutMapping("/rename")
    public BaseResponse<Void> rename(
            @RequestParam String path,
            @RequestParam String newName) {
        userFileService.rename(path, newName);
        return BaseResponse.success(null);
    }

    @PostMapping("/move")
    public BaseResponse<Void> move(
            @RequestParam String sourcePath,
            @RequestParam String targetPath) {
        userFileService.move(sourcePath, targetPath);
        return BaseResponse.success(null);
    }

    @GetMapping("/storage/stats")
    public BaseResponse<Map<String, Long>> getStorageStats() {
        return BaseResponse.success(userFileService.getStorageStats());
    }

    @GetMapping("/list")
    public BaseResponse<List<UserFile>> listFiles(
            @RequestParam(required = false, defaultValue = "/") String path) {
        return BaseResponse.success(userFileService.listFiles(path));
    }

    @GetMapping("/preview")
    public BaseResponse<FilePreviewDTO> previewFile(@RequestParam String path) throws Exception {

        return BaseResponse.success(userFileService.getPreview(path));

    }
} 