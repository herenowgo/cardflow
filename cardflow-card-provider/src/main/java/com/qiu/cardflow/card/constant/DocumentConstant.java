package com.qiu.cardflow.card.constant;

import java.util.Map;
import java.util.Set;

public interface DocumentConstant {
    String USER_PREFIX = "user/";
    String COVER_PREFIX = "covers/";
    String USER_ID = "userId";

    // 文件大小限制
    long MAX_FILE_SIZE = 100 * 1024 * 1024; // 单个文件最大50MB
    long MAX_TEXT_FILE_SIZE = 10 * 1024 * 1024; // 文本文件最大10MB
    long MAX_IMAGE_FILE_SIZE = 5 * 1024 * 1024; // 图片最大5MB

    // 用户存储空间限制
    long DEFAULT_USER_QUOTA = 100 * 1024 * 1024L; // 普通用户默认1GB
    long VIP_USER_QUOTA = 500 * 1024 * 1024L; // VIP用户5GB

    // 允许的文件类型及其对应的大小限制
    Map<String, Long> FILE_TYPE_SIZE_LIMIT = Map.of(
            "pdf", MAX_FILE_SIZE
            // "doc", MAX_FILE_SIZE,
            // "docx", MAX_FILE_SIZE,
            // "txt", MAX_TEXT_FILE_SIZE,
            // "jpg", MAX_IMAGE_FILE_SIZE,
            // "jpeg", MAX_IMAGE_FILE_SIZE,
            // "png", MAX_IMAGE_FILE_SIZE,
            // "gif", MAX_IMAGE_FILE_SIZE
    );

    // 允许的文件类型
    Set<String> ALLOW_FILE_TYPE = FILE_TYPE_SIZE_LIMIT.keySet();

    // 文件名最大长度
    int MAX_FILENAME_LENGTH = 40;

    // 路径最大深度
    int MAX_PATH_DEPTH = 3;

    // 单个文件夹下最大文件数
    int MAX_FILES_PER_FOLDER = 1000;

    // 文本预览大小限制
    long MAX_TEXT_PREVIEW_SIZE = 5 * 1024 * 1024L; // 最大1MB
}
