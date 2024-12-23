package com.qiu.qoj.document.service.impl;

import com.qiu.qoj.common.api.UserContext;
import com.qiu.qoj.common.exception.Asserts;
import com.qiu.qoj.document.constant.DocumentConstant;
import com.qiu.qoj.document.model.dto.file.FileDTO;
import com.qiu.qoj.document.model.dto.file.FilePreviewDTO;
import com.qiu.qoj.document.model.entity.UserFile;
import com.qiu.qoj.document.repository.UserFileRepository;
import com.qiu.qoj.document.service.ObjectStorage;
import com.qiu.qoj.document.service.UserFileService;
import com.qiu.qoj.document.util.FileValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserFileServiceImpl implements UserFileService {

    private final ObjectStorage objectStorage;
    private final UserFileRepository userFileRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String uploadFile(MultipartFile file, String parentPath) throws Exception {
        // 1. 验证文件和路径
        FileValidationUtil.validateFile(file);
        FileValidationUtil.validatePath(parentPath);

        // 2. 检查存储空间配额
        checkStorageQuota(file.getSize());

        // 3. 检查文件夹下的文件数量限制
        checkFolderFileLimit(parentPath);

        // 1. 构建文件路径
        String fileName = file.getOriginalFilename();
        String path = buildFilePath(parentPath, fileName);

        // 2. 上传到对象存储
        FileDTO fileDTO = new FileDTO();
        fileDTO.setFile(file);
        fileDTO.setPath(path);
        fileDTO.setOverwrite(false);
        String storagePath = objectStorage.uploadFile(fileDTO);

        // 3. 保存文件元数据到MongoDB
        UserFile userFile = new UserFile();
        userFile.setUserId(UserContext.getUserId());
        userFile.setName(fileName);
        userFile.setPath(storagePath);
        userFile.setType(FilenameUtils.getExtension(fileName));
        userFile.setSize(file.getSize());
        userFile.setParentPath(parentPath);
        userFile.setIsFolder(false);
        userFile.setIsDeleted(false);
        Date now = new Date();
        userFile.setCreateTime(now);
        userFile.setUpdateTime(now);
        userFileRepository.save(userFile);

        return storagePath;
    }

    @Override
    public void createFolder(String name, String parentPath) {
        // 检查同名文件夹是否存在
        checkNameExists(name, parentPath);

        UserFile folder = new UserFile();
        folder.setUserId(UserContext.getUserId());
        folder.setName(name);
        folder.setPath(buildFilePath(parentPath, name));
        folder.setParentPath(parentPath);
        folder.setIsFolder(true);
        folder.setIsDeleted(false);
        Date now = new Date();
        folder.setCreateTime(now);
        folder.setUpdateTime(now);
        userFileRepository.save(folder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String path) {
        // 1. 获取文件/文件夹信息
        UserFile file = getUserFile(path);

        // 2. 如果是文件夹，递归删除所有子文件和子文件夹
        if (file.getIsFolder()) {
            deleteFolder(path);
        } else {
            // 3. 如果是文件，删除对象存储中的文件
            try {
                objectStorage.deleteFile(path);
            } catch (Exception e) {
                log.error("Delete file from storage failed: {}", path, e);
                throw new RuntimeException("Delete file failed", e);
            }
        }

        // 4. 标记为已删除
        file.setIsDeleted(true);
        file.setDeleteTime(new Date());
        userFileRepository.save(file);
    }

    @Override
    public String getFileUrl(String path) throws Exception {
        // 1. 检查文件是否存在
        UserFile file = getUserFile(path);
        Asserts.failIf(file.getIsFolder(), "Folders do not have URLs");

        // 2. 生成临时访问URL(默认30分钟)
        return objectStorage.getPresignedUrl(path, 30);
    }

    @Override
    public void rename(String path, String newName) {
        // 1. 获取文件/文件夹信息
        UserFile file = getUserFile(path);

        // 2. 检查新名称是否已存在
        checkNameExists(newName, file.getParentPath());

        // 3. 更新文件名和路径
        String newPath = buildFilePath(file.getParentPath(), newName);
        file.setName(newName);
        file.setPath(newPath);
        file.setUpdateTime(new Date());
        userFileRepository.save(file);
    }

    @Override
    public void move(String sourcePath, String targetPath) {
        // 1. 获取源文件/文件夹信息
        UserFile sourceFile = getUserFile(sourcePath);

        // 2. 检查目标路径是否存在同名文件
        checkNameExists(sourceFile.getName(), targetPath);

        // 3. 更新路径
        String newPath = buildFilePath(targetPath, sourceFile.getName());
        sourceFile.setPath(newPath);
        sourceFile.setParentPath(targetPath);
        sourceFile.setUpdateTime(new Date());
        userFileRepository.save(sourceFile);
    }

    @Override
    public Map<String, Long> getStorageStats() {
        Long userId = UserContext.getUserId();
        Map<String, Long> stats = new HashMap<>();

        // 计算已使用空间
        Long usedSpace = userFileRepository.calculateUserStorageUsed(userId);
        stats.put("usedSpace", usedSpace);

        // 总空间限制
        stats.put("totalSpace", DocumentConstant.MAX_FILE_SIZE * 100); // 示例：限制为5GB

        return stats;
    }

    @Override
    public List<UserFile> listFiles(String path) {
        return userFileRepository.findByUserIdAndParentPathAndIsDeletedFalse(
                UserContext.getUserId(), path);
    }

    @Override
    public FilePreviewDTO getPreview(String path) throws Exception {
        // 1. 获取文件信息
        UserFile file = getUserFile(path);
        Asserts.failIf(file.getIsFolder(), "Folders cannot be previewed");

        String type = file.getType().toLowerCase();
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
            previewUrl = objectStorage.getPresignedUrl(path, 30);
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
        List<UserFile> children = userFileRepository.findByUserIdAndParentPathAndIsDeletedFalse(
                UserContext.getUserId(), folderPath);

        for (UserFile child : children) {
            delete(child.getPath());
        }
    }

    /**
     * 检查指定路径下是否存在同名文件/文件夹
     */
    private void checkNameExists(String name, String parentPath) {
        boolean exists = userFileRepository.existsByUserIdAndParentPathAndNameAndIsDeletedFalse(
                UserContext.getUserId(), parentPath, name);
        Asserts.failIf(exists, "File/folder with same name already exists");
    }

    /**
     * 获取文件/文件夹信息并检查权限
     */
    private UserFile getUserFile(String path) {
        UserFile file = userFileRepository.findByPathAndIsDeletedFalse(path);
        Asserts.failIf(file == null, "File/folder not found");

        // 检查权限
        Asserts.failIf(!UserContext.getUserId().equals(file.getUserId()) && !UserContext.isAdmin(),
                "No permission to access this file/folder");

        return file;
    }

    /**
     * 构建文件路径
     */
    private String buildFilePath(String parentPath, String fileName) {
        return DocumentConstant.USER_PREFIX +
                UserContext.getUserId() + "/" +
                (parentPath.startsWith("/") ? parentPath.substring(1) : parentPath) + "/" +
                fileName;
    }

    @Override
    public void checkStorageQuota(long fileSize) {
        Long userId = UserContext.getUserId();
        long usedSpace = userFileRepository.calculateUserStorageUsed(userId);
        long quota = getUserQuota();

        Asserts.failIf(usedSpace + fileSize > quota,
                "Storage quota exceeded. Available: " + (quota - usedSpace) + " bytes");
    }

    @Override
    public long getUserQuota() {
        // 根据用户角色返回不同的配额
        return UserContext.isAdmin() ?
                DocumentConstant.VIP_USER_QUOTA :
                DocumentConstant.DEFAULT_USER_QUOTA;
    }

    /**
     * 检查文件夹下的文件数量是否超出限制
     */
    private void checkFolderFileLimit(String parentPath) {
        long count = userFileRepository.countByUserIdAndParentPathAndIsDeletedFalse(
                UserContext.getUserId(), parentPath);

        Asserts.failIf(count >= DocumentConstant.MAX_FILES_PER_FOLDER,
                "Folder file count limit exceeded: " + DocumentConstant.MAX_FILES_PER_FOLDER);
    }

    @Override
    public List<UserFile> getFilesByTimeRange(Date startTime, Date endTime) {
        return userFileRepository.findByUserIdAndCreateTimeBetweenAndIsDeletedFalse(
                UserContext.getUserId(), startTime, endTime);
    }

    @Override
    public List<UserFile> getRecentFiles(int limit) {
        return userFileRepository.findByUserIdAndIsDeletedFalseOrderByUpdateTimeDesc(
                UserContext.getUserId(), PageRequest.of(0, limit));
    }

    @Override
    public List<UserFile> getRecentlyDeletedFiles(int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -days);
        return userFileRepository.findByUserIdAndIsDeletedTrueAndDeleteTimeGreaterThan(
                UserContext.getUserId(), calendar.getTime());
    }
} 