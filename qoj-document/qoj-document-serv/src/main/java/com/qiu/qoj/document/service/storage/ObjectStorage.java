package com.qiu.qoj.document.service.storage;

import io.minio.messages.Item;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface ObjectStorage {
    String uploadFile(MultipartFile file, String objectName);

    String uploadFile(MultipartFile file, String objectName, Map<String, String> userMetadata);

    String uploadString(String content, String objectName);

    String uploadString(String content, String objectName, Map<String, String> userMetadata);

    InputStream downloadFile(String objectName);

    void deleteFile(String objectName);

    List<String> deleteFiles(List<String> objectNames);

    String getFileUrl(String objectName);

    List<Item> listFiles(String prefix);

    // 获取文件元数据
    Map<String, String> getFileMetadata(String objectName);

    // 复制文件
    void copyFile(String sourceObjectName, String targetObjectName);

    // 移动文件（复制后删除源文件）
    void moveFile(String sourceObjectName, String targetObjectName);

    // 检查文件是否存在
    boolean doesFileExist(String objectName);

    // 获取文件大小
    long getFileSize(String objectName);

    // 批量上传文件
    Map<String, String> uploadFiles(List<MultipartFile> files, List<String> objectNames);

    Map<String, String> uploadFiles(List<MultipartFile> files, List<String> objectNames, Map<String, String> userMetadata);

    // 获取文件的最后修改时间
    Date getLastModified(String objectName);

    // 设置文件的元数据
    void setFileMetadata(String objectName, Map<String, String> metadata);

    // 获取文件的标签
    Map<String, String> getFileTags(String objectName);

    // 设置文件的标签
    void setFileTags(String objectName, Map<String, String> tags);
}