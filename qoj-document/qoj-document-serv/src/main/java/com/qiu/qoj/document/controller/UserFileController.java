package com.qiu.qoj.document.controller;

import com.qiu.qoj.common.api.BaseResponse;
import com.qiu.qoj.document.model.dto.file.FilePreviewDTO;
import com.qiu.qoj.document.model.entity.UserFile;
import com.qiu.qoj.document.service.UserFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
@Tag(name = "UserFileController", description = "提供文件上传、下载、预览等功能")
public class UserFileController {

    private final UserFileService userFileService;

    @Operation(summary = "上传文件",
            description = "上传文件到指定目录，支持大小限制和文件类型校验")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BaseResponse<String> uploadFile(
            @Parameter(description = "文件", required = true)
            @RequestPart("file") MultipartFile file) throws Exception {
        String path = userFileService.uploadFile(file, "/");
        return BaseResponse.success(path);
    }

    @Operation(summary = "创建文件夹",
            description = "在指定目录下创建新文件夹，同名文件夹不允许创建")
    @PostMapping("/folder")
    public BaseResponse<Void> createFolder(
            @Parameter(description = "文件夹名称", required = true)
            @RequestParam String name,
            @Parameter(description = "父目录路径", schema = @Schema(defaultValue = "/"))
            @RequestParam(required = false, defaultValue = "/") String parentPath) {
        userFileService.createFolder(name, parentPath);
        return BaseResponse.success(null);
    }

    @Operation(summary = "删除文件/文件夹",
            description = "删除指定路径的文件或文件夹，文件夹会递归删除其下所有内容")
    @DeleteMapping
    public BaseResponse<Void> delete(
            @Parameter(description = "文件/文件夹路径", required = true)
            @RequestParam String path) {
        userFileService.delete(path);
        return BaseResponse.success(null);
    }

    @Operation(summary = "获取文件访问URL",
            description = "获取文件的临时访问URL，默认有效期30分钟")
    @GetMapping("/url")
    public BaseResponse<String> getFileUrl(
            @Parameter(description = "文件路径", required = true)
            @RequestParam String path) throws Exception {
        String url = userFileService.getFileUrl(path);
        return BaseResponse.success(url);
    }

    @Operation(summary = "重命名文件/文件夹",
            description = "重命名指定路径的文件或文件夹，新名称不能与同目录下的其他文件重名")
    @PutMapping("/rename")
    public BaseResponse<Void> rename(
            @Parameter(description = "文件/文件夹路径", required = true)
            @RequestParam String path,
            @Parameter(description = "新名称", required = true)
            @RequestParam String newName) {
        userFileService.rename(path, newName);
        return BaseResponse.success(null);
    }

    @Operation(summary = "移动文件/文件夹",
            description = "移动文件/文件夹到新的目录，目标路径必须存在且为文件夹")
    @PostMapping("/move")
    public BaseResponse<Void> move(
            @Parameter(description = "源文件/文件夹路径", required = true)
            @RequestParam String sourcePath,
            @Parameter(description = "目标目录路径", required = true)
            @RequestParam String targetPath) {
        userFileService.move(sourcePath, targetPath);
        return BaseResponse.success(null);
    }

    @Operation(summary = "获取存储空间统计",
            description = "获取当前用户的存储空间使用情况，包括已用空间和总空间")
    @GetMapping("/storage/stats")
    public BaseResponse<Map<String, Long>> getStorageStats() {
        return BaseResponse.success(userFileService.getStorageStats());
    }

    @Operation(summary = "获取文件列表",
            description = "获取指定目录下的文件和文件夹列表，不包括已删除的文件")
    @GetMapping("/list")
    public BaseResponse<List<UserFile>> listFiles(
            @Parameter(description = "目录路径", schema = @Schema(defaultValue = "/"))
            @RequestParam(required = false, defaultValue = "/") String path) {
        return BaseResponse.success(userFileService.listFiles(path));
    }

    @Operation(summary = "预览文件",
            description = "获取文件的预览信息，支持文本、图片、PDF等格式，返回对应的预览URL或内容")
    @GetMapping("/preview")
    public BaseResponse<FilePreviewDTO> previewFile(
            @Parameter(description = "文件路径", required = true)
            @RequestParam String path) throws Exception {
        return BaseResponse.success(userFileService.getPreview(path));
    }
}