package com.qiu.qoj.document.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.qiu.qoj.document.model.dto.StudyResourceRequest;
import com.qiu.qoj.document.model.dto.UpdateStudyResourceRequest;
import com.qiu.qoj.document.model.dto.file.FilePreviewDTO;
import com.qiu.qoj.document.model.entity.StudyResource;
import com.qiu.qoj.document.model.vo.StudyResourceVO;
import com.qiu.qoj.document.model.vo.FileListVO;

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
     * 
     * @param id 资源ID
     */
    void delete(String id);

    /**
     * 获取文件访问URL
     */
    String getFileUrl(String path) throws Exception;

    /**
     * 获取用户存储空间使用情况
     */
    Map<String, Long> getStorageStats();

    /**
     * 列出指定路径下的文件和文件夹
     * 
     * @param path 目录路径
     * @return 文件和文件夹列表
     */
    List<FileListVO> listFiles(String path);

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
     * 根据ID获取资源详细信息
     *
     * @param id 资源ID
     * @return 资源详细信息
     */
    StudyResourceVO getResourceById(String id);

    /**
     * 创建非文档类型学习资源
     *
     * @param userId  用户ID
     * @param request 创建资源请求
     * @return 创建后的资源信息
     */
    StudyResourceVO createResource(Long userId, StudyResourceRequest request);

    /**
     * 更新学习资源
     *
     * @param userId  用户ID
     * @param request 更新请求
     * @return 更新后的资源信息
     */
    StudyResourceVO updateResource(Long userId, UpdateStudyResourceRequest request);

    /**
     * 上传资源封面图片
     * 
     * @param file 封面图片文件
     * @return 图片的永久访问URL
     * @throws Exception 如果上传失败或文件格式不支持
     */
    String uploadCover(MultipartFile file) throws Exception;
}