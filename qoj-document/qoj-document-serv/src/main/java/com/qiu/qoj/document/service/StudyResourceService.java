package com.qiu.qoj.document.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.qiu.qoj.document.model.dto.UpdateStudyResourceRequest;
import com.qiu.qoj.document.model.dto.file.FilePreviewDTO;
import com.qiu.qoj.document.model.entity.StudyResource;
import com.qiu.qoj.document.model.vo.StudyResourceVO;

public interface StudyResourceService {
    /**
     * 上传文件
     */
    String uploadFile(MultipartFile file, String parentPath) throws Exception;

    /**
     * 创建文件夹
     */
    void createFolder(String name, String parentPath);

    /**
     * 删除文件/文件夹
     */
    void delete(String path);

    /**
     * 获取文件访问URL
     */
    String getFileUrl(String path) throws Exception;

    /**
     * 重命名文件/文件夹
     */
    void rename(String path, String newName);

    /**
     * 移动文件/文件夹
     */
    void move(String sourcePath, String targetPath);

    /**
     * 获取用户存储空间使用情况
     */
    Map<String, Long> getStorageStats();

    /**
     * 列出指定路径下的文件和文件夹
     */
    List<StudyResource> listFiles(String path);

    /**
     * 获取文件预览信息
     *
     * @param path 文件路径
     * @return 预览信息
     */
    FilePreviewDTO getPreview(String path) throws Exception;

    /**
     * 检查用户存储空间是否足够
     *
     * @param fileSize 要上传的文件大小
     */
    void checkStorageQuota(long fileSize);

    /**
     * 获取用户存储空间配额
     */
    long getUserQuota();

    /**
     * 获取指定时间段内创建的文件
     */
    List<StudyResource> getFilesByTimeRange(Date startTime, Date endTime);

    /**
     * 获取最近修改的文件
     */
    List<StudyResource> getRecentFiles(int limit);

    /**
     * 获取最近删除的文件(用于回收站功能)
     */
    List<StudyResource> getRecentlyDeletedFiles(int days);

    /**
     * 更新学习资源
     *
     * @param userId  用户ID
     * @param request 更新请求
     * @return 更新后的资源信息
     */
    StudyResourceVO updateResource(Long userId, UpdateStudyResourceRequest request);
}