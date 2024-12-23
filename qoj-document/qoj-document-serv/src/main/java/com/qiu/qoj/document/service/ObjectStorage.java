package com.qiu.qoj.document.service;

import com.qiu.qoj.document.model.dto.file.FileDTO;

public interface ObjectStorage {
    /**
     * 上传文件
     *
     * @return 文件的访问路径
     */
    String uploadFile(FileDTO fileDTO) throws Exception;

    /**
     * 删除文件
     */
    void deleteFile(String path) throws Exception;

    /**
     * 获取文件的临时访问URL
     *
     * @param path          文件路径
     * @param expireMinutes 过期时间(分钟)
     */
    String getPresignedUrl(String path, int expireMinutes) throws Exception;

    /**
     * 检查文件是否存在
     */
    boolean doesObjectExist(String path) throws Exception;

    /**
     * 获取文本文件内容
     * @param path 文件路径
     * @param maxSize 最大读取字节数
     * @return 文件内容
     */
    String getTextContent(String path, long maxSize) throws Exception;
}