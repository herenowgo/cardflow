package com.qiu.cardflow.document.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.io.FileTypeUtil;
import com.qiu.cardflow.common.api.UserContext;
import com.qiu.cardflow.common.exception.ApiException;
import com.qiu.cardflow.common.exception.Asserts;
import com.qiu.cardflow.document.constant.DocumentConstant;
import com.qiu.cardflow.document.model.dto.StudyResourceRequest;
import com.qiu.cardflow.document.model.dto.UpdateStudyResourceRequest;
import com.qiu.cardflow.document.model.dto.file.FileDTO;
import com.qiu.cardflow.document.model.dto.file.FilePreviewDTO;
import com.qiu.cardflow.document.model.entity.StudyResource;
import com.qiu.cardflow.document.model.enums.ResourceType;
import com.qiu.cardflow.document.model.vo.FileListVO;
import com.qiu.cardflow.document.model.vo.StudyResourceVO;
import com.qiu.cardflow.document.repository.StudyResourceRepository;
import com.qiu.cardflow.document.service.ObjectStorage;
import com.qiu.cardflow.document.service.StudyResourceService;
import com.qiu.cardflow.document.util.FileValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudyResourceServiceImpl implements StudyResourceService {

    private final ObjectStorage objectStorage;
    private final StudyResourceRepository studyResourceRepository;
    private StudyResource studyResource;
    private FileDTO fileDTO;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String uploadFile(MultipartFile file, String parentPath) throws Exception {
        // 1. 验证文件和路径
        FileValidationUtil.validateFile(file);
        FileValidationUtil.validatePath(parentPath);

        // 2. 检查存储空间配额
        // checkStorageQuota(file.getSize());

        // 3. 检查文件夹下的文件数量限制
        // checkFolderFileLimit(parentPath);

        // 1. 构建文件路径
        String fileName = file.getOriginalFilename();
        String storagePath = buildFilePath(fileName);

        // 2. 上传到对象存储
        FileDTO fileDTO = new FileDTO();
        fileDTO.setFile(file);
        fileDTO.setPath(storagePath);
        fileDTO.setOverwrite(true);
        storagePath = objectStorage.uploadFile(fileDTO);

        // 3. 保存逻辑文件对象到MongoDB
        StudyResource studyResource = StudyResource.builder()
                .userId(UserContext.getUserId())
                .objectStorageFileName(storagePath)
                .name(fileName)
                .resourceType(ResourceType.PDF)
                .size(file.getSize())
                .parentPath(parentPath)
                .isFolder(false)
                .isDeleted(false)
                .isPublic(false) // 默认不公开
                .createTime(new Date())
                .updateTime(new Date())
                .build();

        studyResourceRepository.save(studyResource);

        return storagePath;
    }

    @Override
    public void createFolder(String name, String parentPath) {
        // 检查同名文件夹是否存在
        checkNameExists(name, parentPath);

        Date now = new Date();
        StudyResource folder = StudyResource.builder()
                .userId(UserContext.getUserId())
                .name(name)
                .parentPath(parentPath)
                .isFolder(true)
                .isDeleted(false)
                .isPublic(false) // 默认不公开
                .createTime(now)
                .updateTime(now)
                .build();
        studyResourceRepository.save(folder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String id) {
        // 1. 查找资源
        StudyResource resource = studyResourceRepository.findById(id)
                .orElseThrow(() -> new ApiException("资源不存在"));

        // 2. 检查访问权限
        Asserts.failIf(!UserContext.getUserId().equals(resource.getUserId()) && !UserContext.isAdmin(),
                "无权限删除该资源");

        // 3. 如果是文件夹，递归删除所有子文件和子文件夹
        if (resource.getIsFolder()) {
            deleteFolder(resource.getPath());
        } else {
            // 4. 如果是文件，删除对象存储中的文件
            try {
                if (resource.getObjectStorageFileName() != null) {
                    objectStorage.deleteFile(resource.getObjectStorageFileName());
                }
            } catch (Exception e) {
                log.error("Delete file from storage failed: {}", resource.getPath(), e);
                throw new RuntimeException("Delete file failed", e);
            }
        }

        // 5. 标记为已删除
        resource.setIsDeleted(true);
        resource.setDeleteTime(new Date());
        studyResourceRepository.save(resource);
    }

    @Override
    public Map<String, Long> getStorageStats() {
        Long userId = UserContext.getUserId();
        Map<String, Long> stats = new HashMap<>();

        // 计算已使用空间
        Long usedSpace = studyResourceRepository.calculateUserStorageUsed(userId);
        stats.put("usedSpace", usedSpace);

        // 总空间限制
        stats.put("totalSpace", DocumentConstant.MAX_FILE_SIZE * 100); // 示例：限制为5GB

        return stats;
    }

    @Override
    public List<FileListVO> listFiles(String path) {
        List<StudyResource> resources = studyResourceRepository.findBasicFileInfoByUserIdAndParentPath(
                UserContext.getUserId(), path);

        return resources.stream()
                .map(resource -> FileListVO.builder()
                        .id(resource.getId())
                        .name(resource.getName())
                        .isFolder(resource.getIsFolder())
                        .resourceType(resource.getResourceType())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public FilePreviewDTO getPreview(String id) throws Exception {
        // 1. 获取资源信息
        StudyResource resource = studyResourceRepository.findById(id)
                .orElseThrow(() -> new ApiException("资源不存在"));

        // 2. 检查访问权限 - 加入公开资源校验逻辑
        boolean hasAccess = UserContext.getUserId().equals(resource.getUserId()) ||
                UserContext.isAdmin() ||
                Boolean.TRUE.equals(resource.getIsPublic());
        Asserts.failIf(!hasAccess, "无权限访问该资源");

        // 3. 检查是否为文件夹
        Asserts.failIf(resource.getIsFolder(), "文件夹不支持预览");

        // 4. 获取文件类型
        // String type = FileTypeUtil.getType(resource.get());
        // boolean canPreview = isPreviewable(type);

        // // 5. 如果不支持预览,只返回基本信息
        // if (!canPreview) {
        // return FilePreviewDTO.builder()
        // .type(type)
        // .canPreview(false)
        // .build();
        // }

        // 6. 根据文件类型处理预览
        String previewUrl = null;
        String content = null;

        // if (isImageType(type)) {
        // // 图片类型 - 返回临时访问URL
        // previewUrl =
        // objectStorage.getPresignedUrl(resource.getObjectStorageFileName(), 30);
        // } else if (isTextType(type)) {
        // // 文本类型 - 读取文件内容
        // content = objectStorage.getTextContent(resource.getObjectStorageFileName(),
        // DocumentConstant.MAX_TEXT_PREVIEW_SIZE);
        // } else if (isPdfType(type)) {
        // PDF类型 - 返回临时访问URL
        previewUrl = objectStorage.getPresignedUrl(resource.getObjectStorageFileName(), 360);
        // }

        return FilePreviewDTO.builder()
                .type("pdf")
                .url(previewUrl)
                .content(content)
                .canPreview(true)
                .build();
    }

    /**
     * 判断文件类型是否支持预览
     */
    private boolean isPreviewable(String type) {
        return isImageType(type) || isTextType(type) || isPdfType(type);
    }

    /**
     * 判断是否为图片类型
     */
    private boolean isImageType(String type) {
        return type.matches("jpg|jpeg|png|gif|bmp");
    }

    /**
     * 判断是否为文本类型
     */
    private boolean isTextType(String type) {
        return type.matches("txt|json|xml|md|java|py|js|html|css");
    }

    /**
     * 判断是否为PDF类型
     */
    private boolean isPdfType(String type) {
        return "pdf".equals(type);
    }

    /**
     * 获取文本文件内容
     */
    private String getTextContent(String path) throws Exception {
        return objectStorage.getTextContent(path, DocumentConstant.MAX_TEXT_PREVIEW_SIZE);
    }

    /**
     * 递归删除文件夹
     */
    private void deleteFolder(String folderPath) {
        List<StudyResource> children = studyResourceRepository.findByUserIdAndParentPathAndIsDeletedFalse(
                UserContext.getUserId(), folderPath);

        for (StudyResource child : children) {
            delete(child.getId());
        }
    }

    /**
     * 检查指定路径下是否存在同名文件/文件夹
     */
    private void checkNameExists(String name, String parentPath) {
        boolean exists = studyResourceRepository.existsByUserIdAndParentPathAndNameAndIsDeletedFalse(
                UserContext.getUserId(), parentPath, name);
        Asserts.failIf(exists, "File/folder with same name already exists");
    }

    /**
     * 获取文件/文件夹信息并检查权限
     */
    private StudyResource getUserFile(String path) {
        // 解析路径获取父目录和文件名
        String parentPath = path.substring(0, path.lastIndexOf('/') + 1);
        String name = path.substring(path.lastIndexOf('/') + 1);
        if (name.isEmpty() && parentPath.length() > 1) {
            // 处理以/结尾的文件夹路径
            parentPath = parentPath.substring(0, parentPath.length() - 1);
            name = parentPath.substring(parentPath.lastIndexOf('/') + 1) + "/";
            parentPath = parentPath.substring(0, parentPath.lastIndexOf('/') + 1);
        }

        StudyResource file = studyResourceRepository.findByParentPathAndNameAndIsDeletedFalse(parentPath, name);
        Asserts.failIf(file == null, "File/folder not found");

        // 检查权限
        Asserts.failIf(!UserContext.getUserId().equals(file.getUserId()) && !UserContext.isAdmin(),
                "No permission to access this file/folder");

        return file;
    }

    /**
     * 构建文件路径
     */
    private String buildFilePath(String fileName) {
        return DocumentConstant.USER_PREFIX +
                UserContext.getUserId() + "/" +
                fileName;
    }

    /**
     * 确保路径以/结尾并拼接文件名
     */
    private String buildFullPath(String parentPath, String name) {
        if (!parentPath.endsWith("/")) {
            parentPath = parentPath + "/";
        }
        return parentPath + name;
    }

    @Override
    public long getUserQuota() {
        // 根据用户角色返回不同的配额
        return UserContext.isAdmin() ? DocumentConstant.VIP_USER_QUOTA : DocumentConstant.DEFAULT_USER_QUOTA;
    }

    /**
     * 检查文件夹下的文件数量是否超出限制
     */
    private void checkFolderFileLimit(String parentPath) {
        long count = studyResourceRepository.countByUserIdAndParentPathAndIsDeletedFalse(
                UserContext.getUserId(), parentPath);

        Asserts.failIf(count >= DocumentConstant.MAX_FILES_PER_FOLDER,
                "Folder file count limit exceeded: " + DocumentConstant.MAX_FILES_PER_FOLDER);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateResource(Long userId, UpdateStudyResourceRequest request) {
        // 1. 检查资源是否存在
        StudyResource resource = studyResourceRepository.findById(request.getId())
                .orElseThrow(() -> new ApiException("资源不存在"));

        // 2. 验证资源所有权或管理员权限
        boolean hasAccess = resource.getUserId().equals(userId) || UserContext.isAdmin();
        Asserts.failIf(!hasAccess, "无权限修改该资源");

        // 3. 处理公开属性 - 只有管理员可以设置
        if (request.getIsPublic() != null && !request.getIsPublic().equals(resource.getIsPublic())) {
            Asserts.failIf(!UserContext.isAdmin(), "只有管理员可以设置资源是否公开");
            resource.setIsPublic(request.getIsPublic());
        }

        // 4. 更新允许修改的字段 - 排除isPublic字段，已单独处理
        BeanUtil.copyProperties(request, resource, CopyOptions.create()
                .setIgnoreNullValue(true)
                .setIgnoreProperties("id", "isPublic")); // 忽略id和isPublic字段

        // 5. 更新时间
        resource.setUpdateTime(new Date());

        // 6. 保存更新
        studyResourceRepository.save(resource);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StudyResourceVO createResource(Long userId, StudyResourceRequest request) {
        // 1. 验证资源类型
        Asserts.failIf(ResourceType.PDF.equals(request.getResourceType()),
                "PDF类型资源请使用文件上传接口");

        // 2. 检查父目录路径
        FileValidationUtil.validatePath(request.getParentPath());

        // 3. 检查同名文件
        checkNameExists(request.getName(), request.getParentPath());

        // 处理公开属性 - 只有管理员可以设置
        Boolean isPublic = request.getIsPublic();
        if (Boolean.TRUE.equals(isPublic)) {
            Asserts.failIf(!UserContext.isAdmin(), "只有管理员可以创建公开资源");
        }

        // 4. 创建资源对象
        Date now = new Date();
        StudyResource resource = StudyResource.builder()
                .userId(userId)
                .name(request.getName())
                .resourceType(request.getResourceType())
                .parentPath(request.getParentPath())
                .coverUrl(request.getCoverUrl())
                .description(request.getDescription())
                .content(request.getContent())
                .note(request.getNote())
                .resourceUrl(request.getResourceUrl())
                .isFolder(false)
                .isDeleted(false)
                .isPublic(isPublic) // 设置公开属性
                .createTime(now)
                .updateTime(now)
                .build();

        // 5. 保存资源
        resource = studyResourceRepository.save(resource);

        // 6. 转换为VO并返回
        StudyResourceVO vo = new StudyResourceVO();
        BeanUtils.copyProperties(resource, vo);
        return vo;
    }

    @Override
    public StudyResourceVO getResourceById(String id) {
        // 1. 查找资源
        StudyResource resource = studyResourceRepository.findById(id)
                .orElseThrow(() -> new ApiException("资源不存在"));

        // 2. 检查资源是否已删除
        Asserts.failIf(resource.getIsDeleted(), "资源已删除");

        // 3. 检查访问权限 - 加入公开资源校验逻辑
        boolean hasAccess = UserContext.getUserId().equals(resource.getUserId()) ||
                UserContext.isAdmin() ||
                Boolean.TRUE.equals(resource.getIsPublic());
        Asserts.failIf(!hasAccess, "无权限访问该资源");

        // 4. 转换为VO并返回
        StudyResourceVO vo = new StudyResourceVO();
        BeanUtils.copyProperties(resource, vo);
        return vo;
    }

    @Override
    public String uploadCover(MultipartFile file) throws Exception {
        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ApiException("只支持上传图片文件");
        }

        // 验证图片格式
        String extension = FileTypeUtil.getType(file.getOriginalFilename());
        if (!Arrays.asList("jpg", "jpeg", "png").contains(extension.toLowerCase())) {
            throw new ApiException("只支持jpg、jpeg、png格式的图片");
        }

        // 验证文件大小（限制为2MB）
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new ApiException("图片大小不能超过2MB");
        }

        // 构建文件路径
        String filePath = DocumentConstant.USER_PREFIX + UserContext.getUserId() + "/" + DocumentConstant.COVER_PREFIX
                + UUID.randomUUID().toString() + "." + extension;

        // 上传到对象存储
        FileDTO fileDTO = FileDTO.builder()
                .file(file)
                .path(filePath)
                .overwrite(false)
                .build();

        return objectStorage.uploadFile(fileDTO);
    }

    @Override
    public List<StudyResourceVO> getPublicResources() {
        // 获取所有公开且未删除的非文件夹资源
        List<StudyResource> resources = studyResourceRepository.findByIsPublicTrueAndIsDeletedFalseAndIsFolderFalse();

        // 转换为VO并返回
        return resources.stream()
                .map(resource -> {
                    StudyResourceVO vo = new StudyResourceVO();
                    BeanUtils.copyProperties(resource, vo);
                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    public StudyResourceVO getPublicResourceById(String id) {
        // 1. 查找资源
        StudyResource resource = studyResourceRepository.findById(id)
                .orElseThrow(() -> new ApiException("资源不存在"));

        // 2. 检查资源是否已删除
        Asserts.failIf(resource.getIsDeleted(), "资源已删除");

        // 3. 检查资源是否公开
        Asserts.failIf(!Boolean.TRUE.equals(resource.getIsPublic()), "该资源不是公开资源");

        // 4. 转换为VO并返回
        StudyResourceVO vo = new StudyResourceVO();
        BeanUtils.copyProperties(resource, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StudyResourceVO favoritePublicResource(Long userId, String resourceId, String parentPath) throws Exception {
        // 1. 获取要收藏的资源
        StudyResource originResource = studyResourceRepository.findById(resourceId)
                .orElseThrow(() -> new ApiException("资源不存在"));

        // 2. 验证是否为公开资源
        Asserts.failIf(!Boolean.TRUE.equals(originResource.getIsPublic()), "只能收藏公开资源");
        Asserts.failIf(originResource.getIsDeleted(), "资源已删除");
        Asserts.failIf(originResource.getIsFolder(), "不能收藏文件夹");
        
        // 3. 验证目标路径
        FileValidationUtil.validatePath(parentPath);
        
        // 4. 检查同名文件是否存在
        checkNameExists(originResource.getName(), parentPath);
        
        // 5. 创建资源副本
        Date now = new Date();
        StudyResource newResource = StudyResource.builder()
                .userId(userId)
                .name(originResource.getName())
                .resourceType(originResource.getResourceType())
                .parentPath(parentPath)
                .coverUrl(originResource.getCoverUrl())
                .description(originResource.getDescription())
                .content(originResource.getContent())
                .note("") // 收藏的资源不复制原有笔记
                .resourceUrl(originResource.getResourceUrl())
                .objectStorageFileName(originResource.getObjectStorageFileName()) // 共享原始文件
                .size(originResource.getSize())
                .structuredTags(originResource.getStructuredTags())
                .isFolder(false)
                .isDeleted(false)
                .isPublic(false) // 收藏的资源默认不公开
                .createTime(now)
                .updateTime(now)
                .build();
                
        // 6. 保存资源副本
        newResource = studyResourceRepository.save(newResource);
        
        // 7. 转换为VO并返回
        StudyResourceVO vo = new StudyResourceVO();
        BeanUtils.copyProperties(newResource, vo);
        return vo;
    }
}