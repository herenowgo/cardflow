package com.qiu.qoj.document.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.qiu.qoj.common.api.BaseResponse;
import com.qiu.qoj.common.api.UserContext;
import com.qiu.qoj.document.model.dto.StudyResourceRequest;
import com.qiu.qoj.document.model.dto.UpdateStudyResourceRequest;
import com.qiu.qoj.document.model.dto.file.FilePreviewDTO;
import com.qiu.qoj.document.model.entity.StudyResource;
import com.qiu.qoj.document.model.vo.FileListVO;
import com.qiu.qoj.document.model.vo.StudyResourceVO;
import com.qiu.qoj.document.service.StudyResourceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/resource")
@RequiredArgsConstructor
@Tag(name = "StudyResourceController", description = "提供文件上传、下载、预览等功能")
public class StudyResourceController {

        private final StudyResourceService studyResourceService;

        @Operation(summary = "上传文件", description = "上传文件到指定目录，支持大小限制和文件类型校验")
        @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public BaseResponse<String> uploadFile(
                        @Parameter(description = "文件", required = true) @RequestPart("file") MultipartFile file)
                        throws Exception {
                String path = studyResourceService.uploadFile(file, "/");
                return BaseResponse.success(path);
        }

        @Operation(summary = "创建文件夹", description = "在指定目录下创建新文件夹，同名文件夹不允许创建")
        @PostMapping("/folder")
        public BaseResponse<Void> createFolder(
                        @Parameter(description = "文件夹名称", required = true) @RequestParam String name,
                        @Parameter(description = "父目录路径", schema = @Schema(defaultValue = "/")) @RequestParam(required = false, defaultValue = "/") String parentPath) {
                studyResourceService.createFolder(name, parentPath);
                return BaseResponse.success(null);
        }

        @Operation(summary = "删除文件/文件夹", description = "删除指定路径的文件或文件夹，文件夹会递归删除其下所有内容")
        @DeleteMapping
        public BaseResponse<Void> delete(
                        @Parameter(description = "文件/文件夹路径", required = true) @RequestParam String path) {
                studyResourceService.delete(path);
                return BaseResponse.success(null);
        }

        @Operation(summary = "获取文件访问URL", description = "获取文件的临时访问URL，默认有效期30分钟")
        @GetMapping("/url")
        public BaseResponse<String> getFileUrl(
                        @Parameter(description = "文件路径", required = true) @RequestParam String path) throws Exception {
                String url = studyResourceService.getFileUrl(path);
                return BaseResponse.success(url);
        }

        @Operation(summary = "重命名文件/文件夹", description = "重命名指定路径的文件或文件夹，新名称不能与同目录下的其他文件重名")
        @PutMapping("/rename")
        public BaseResponse<Void> rename(
                        @Parameter(description = "文件/文件夹路径", required = true) @RequestParam String path,
                        @Parameter(description = "新名称", required = true) @RequestParam String newName) {
                studyResourceService.rename(path, newName);
                return BaseResponse.success(null);
        }

        @Operation(summary = "移动文件/文件夹", description = "移动文件/文件夹到新的目录，目标路径必须存在且为文件夹")
        @PostMapping("/move")
        public BaseResponse<Void> move(
                        @Parameter(description = "源文件/文件夹路径", required = true) @RequestParam String sourcePath,
                        @Parameter(description = "目标目录路径", required = true) @RequestParam String targetPath) {
                studyResourceService.move(sourcePath, targetPath);
                return BaseResponse.success(null);
        }

        @Operation(summary = "获取存储空间统计", description = "获取当前用户的存储空间使用情况，包括已用空间和总空间")
        @GetMapping("/storage/stats")
        public BaseResponse<Map<String, Long>> getStorageStats() {
                return BaseResponse.success(studyResourceService.getStorageStats());
        }

        @Operation(summary = "获取文件列表", description = "获取指定目录下的文件和文件夹列表，只返回名称和类型信息")
        @GetMapping("/list")
        public BaseResponse<List<FileListVO>> listFiles(
                        @Parameter(description = "目录路径", schema = @Schema(defaultValue = "/")) @RequestParam(required = false, defaultValue = "/") String path) {
                return BaseResponse.success(studyResourceService.listFiles(path));
        }

        @Operation(summary = "预览文件", description = "获取文件的预览信息，支持文本、图片、PDF等格式，返回对应的预览URL或内容")
        @GetMapping("/preview")
        public BaseResponse<FilePreviewDTO> previewFile(
                        @Parameter(description = "文件路径", required = true) @RequestParam String path) throws Exception {
                return BaseResponse.success(studyResourceService.getPreview(path));
        }

        @Operation(summary = "更新学习资源", description = "更新学习资源的基本信息")
        @PutMapping("/update")
        public BaseResponse<StudyResourceVO> updateResource(
                        @Parameter(description = "更新资源请求", required = true) @RequestBody @Valid UpdateStudyResourceRequest request) {
                return BaseResponse.success(
                                studyResourceService.updateResource(UserContext.getUserId(), request));
        }

        @Operation(summary = "创建非文档类型学习资源", description = "创建不需要上传文件的学习资源，如文章、笔记、在线资源等")
        @PostMapping
        public BaseResponse<StudyResourceVO> createResource(
                        @Parameter(description = "学习资源请求", required = true) @RequestBody @Valid StudyResourceRequest request) {
                StudyResourceVO resource = studyResourceService.createResource(UserContext.getUserId(), request);
                return BaseResponse.success(resource);
        }
}