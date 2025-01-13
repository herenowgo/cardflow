package com.qiu.qoj.document.service.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.qiu.qoj.common.api.UserContext;
import com.qiu.qoj.common.exception.ApiException;
import com.qiu.qoj.common.exception.Asserts;
import com.qiu.qoj.document.constant.DocumentConstant;
import com.qiu.qoj.document.model.dto.StudyResourceRequest;
import com.qiu.qoj.document.model.dto.UpdateStudyResourceRequest;
import com.qiu.qoj.document.model.dto.file.FileDTO;
import com.qiu.qoj.document.model.dto.file.FilePreviewDTO;
import com.qiu.qoj.document.model.entity.StudyResource;
import com.qiu.qoj.document.model.enums.ResourceType;
import com.qiu.qoj.document.model.vo.FileListVO;
import com.qiu.qoj.document.model.vo.StudyResourceVO;
import com.qiu.qoj.document.repository.StudyResourceRepository;
import com.qiu.qoj.document.service.ObjectStorage;
import com.qiu.qoj.document.service.StudyResourceService;
import com.qiu.qoj.document.util.FileValidationUtil;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.io.FileTypeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudyResourceServiceImpl implements StudyResourceService {

    private final ObjectStorage objectStorage;
    private final StudyResourceRepository studyResourceRepository;
    private StudyResource studyResource;

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
        String path = buildFilePath(fileName);

        // 2. 上传到对象存储
        FileDTO fileDTO = new FileDTO();
        fileDTO.setFile(file);
        fileDTO.setPath(path);
        fileDTO.setOverwrite(false);
        String storagePath = objectStorage.uploadFile(fileDTO);

        // 3. 保存逻辑文件对象到MongoDB
        StudyResource studyResource = StudyResource.builder()
                .userId(UserContext.getUserId())
                .objectStorageFileName(storagePath)
                .name(fileName)
                .path(parentPath + fileName)
                .resourceType(ResourceType.PDF)
                .size(file.getSize())
                .parentPath(parentPath)
                .isFolder(false)
                .isDeleted(false)
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
                .path(buildFullPath(parentPath, name + "/"))
                .parentPath(parentPath)
                .isFolder(true)
                .isDeleted(false)
                .createTime(now)
                .updateTime(now)
                .build();
        studyResourceRepository.save(folder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String path) {
        // 1. 获取文件/文件夹信息
        StudyResource file = getUserFile(path);

        // 2. 如果是文件夹，递归删除所有子文件和子文件夹
        if (file.getIsFolder()) {
            deleteFolder(path);
        } else {
            // 3. 如果是文件，删除对象存储中的文件
            try {
                objectStorage.deleteFile(file.getObjectStorageFileName());
            } catch (Exception e) {
                log.error("Delete file from storage failed: {}", path, e);
                throw new RuntimeException("Delete file failed", e);
            }
        }

        // 4. 标记为已删除
        file.setIsDeleted(true);
        file.setDeleteTime(new Date());
        studyResourceRepository.save(file);
    }

    @Override
    public String getFileUrl(String path) throws Exception {
        // 1. 获取文件
        StudyResource file = getUserFile(path);
        Asserts.failIf(file.getIsFolder(), "Folders do not have URLs");

        // 2. 生成临时访问URL(默认30分钟)
        return objectStorage.getPresignedUrl(file.getObjectStorageFileName(), 30);
    }

    @Override
    public void rename(String path, String newName) {
        // 1. 获取文件/文件夹信息
        StudyResource file = getUserFile(path);

        // 2. 检查新名称是否已存在
        checkNameExists(newName, file.getParentPath());

        // 3. 更新文件名和路径
        String newPath = buildFullPath(file.getParentPath(), newName);
        if (file.getIsFolder()) {
            newPath += "/";
        }
        file.setName(newName);
        file.setPath(newPath);
        file.setUpdateTime(new Date());
        studyResourceRepository.save(file);
    }

    @Override
    public void move(String sourcePath, String targetPath) {
        // 1. 获取源文件/文件夹信息
        StudyResource sourceFile = getUserFile(sourcePath);

        // 2. 检查目标路径是否存在同名文件
        checkNameExists(sourceFile.getName(), targetPath);

        // 3. 更新路径
        String newPath = buildFullPath(targetPath, sourceFile.getName());
        if (sourceFile.getIsFolder()) {
            newPath += "/";
        }
        sourceFile.setPath(newPath);
        sourceFile.setParentPath(targetPath);
        sourceFile.setUpdateTime(new Date());
        studyResourceRepository.save(sourceFile);
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
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public FilePreviewDTO getPreview(String path) throws Exception {
        // 1. 获取文件信息
        StudyResource file = getUserFile(path);
        Asserts.failIf(file.getIsFolder(), "Folders cannot be previewed");

        String type = FileTypeUtil.getType(file.getName());
        boolean canPreview = isPreviewable(type);

        // 2. 如果不支持预览,只返回基本信息
        if (!canPreview) {
            return FilePreviewDTO.builder()
                    .type(type)
                    .canPreview(false)
                    .build();
        }

        // 3. 根据文件类型处理预览
        String previewUrl = null;
        String content = null;

        if (isImageType(type)) {
            // 图片类型 - 返回临时访问URL
            previewUrl = objectStorage.getPresignedUrl(path, 99999);
        } else if (isTextType(type)) {
            // 文本类型 - 读取文件内容
            content = getTextContent(path);
        } else if (isPdfType(type)) {
            // PDF类型 - 返回临时访问URL
            previewUrl = objectStorage.getPresignedUrl(path, 30);
        }

        return FilePreviewDTO.builder()
                .type(type)
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
            delete(child.getPath());
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
        StudyResource file = studyResourceRepository.findByPathAndIsDeletedFalse(path);
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
    public void checkStorageQuota(long fileSize) {
        Long userId = UserContext.getUserId();
        long usedSpace = studyResourceRepository.calculateUserStorageUsed(userId);
        long quota = getUserQuota();

        Asserts.failIf(usedSpace + fileSize > quota,
                "Storage quota exceeded. Available: " + (quota - usedSpace) + " bytes");
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
    public List<StudyResource> getFilesByTimeRange(Date startTime, Date endTime) {
        return studyResourceRepository.findByUserIdAndCreateTimeBetweenAndIsDeletedFalse(
                UserContext.getUserId(), startTime, endTime);
    }

    @Override
    public List<StudyResource> getRecentFiles(int limit) {
        return studyResourceRepository.findByUserIdAndIsDeletedFalseOrderByUpdateTimeDesc(
                UserContext.getUserId(), PageRequest.of(0, limit));
    }

    @Override
    public List<StudyResource> getRecentlyDeletedFiles(int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -days);
        return studyResourceRepository.findByUserIdAndIsDeletedTrueAndDeleteTimeGreaterThan(
                UserContext.getUserId(), calendar.getTime());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StudyResourceVO updateResource(Long userId, UpdateStudyResourceRequest request) {
        // 1. 检查资源是否存在
        StudyResource resource = studyResourceRepository.findById(request.getId())
                .orElseThrow(() -> new ApiException("资源不存在"));

        // 2. 验证资源所有权
        if (!resource.getUserId().equals(userId)) {
            throw new ApiException("无权限修改该资源");
        }

        // 3. 更新允许修改的字段
        BeanUtil.copyProperties(request, resource, CopyOptions.create()
                .setIgnoreNullValue(true)
                .setIgnoreProperties("id")); // 忽略id字段,防止覆盖

        // 4. 更新时间
        resource.setUpdateTime(new Date());

        // 5. 保存更新
        resource = studyResourceRepository.save(resource);

        // 6. 转换为VO并返回
        StudyResourceVO vo = new StudyResourceVO();
        BeanUtils.copyProperties(resource, vo);
        return vo;
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

        // 4. 创建资源对象
        Date now = new Date();
        StudyResource resource = StudyResource.builder()
                .userId(userId)
                .name(request.getName())
                .resourceType(request.getResourceType())
                .path(buildFullPath(request.getParentPath(), request.getName()))
                .parentPath(request.getParentPath())
                .coverUrl(request.getCoverUrl())
                .description(request.getDescription())
                .content(request.getContent())
                .note(request.getNote())
                .resourceUrl(request.getResourceUrl())
                .isFolder(false)
                .isDeleted(false)
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

        // 3. 检查访问权限
        Asserts.failIf(!UserContext.getUserId().equals(resource.getUserId()) && !UserContext.isAdmin(),
                "无权限访问该资源");

        // 4. 转换为VO并返回
        StudyResourceVO vo = new StudyResourceVO();
        BeanUtils.copyProperties(resource, vo);
        return vo;
    }
}