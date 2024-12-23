package com.qiu.qoj.document.util;

import com.qiu.qoj.common.exception.Asserts;
import com.qiu.qoj.document.constant.DocumentConstant;
import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;

public class FileValidationUtil {

    /**
     * 验证文件名
     */
    public static void validateFileName(String fileName) {
        // 检查文件名长度
        Asserts.failIf(fileName.length() > DocumentConstant.MAX_FILENAME_LENGTH,
                "File name too long, max length is " + DocumentConstant.MAX_FILENAME_LENGTH);

        // 检查文件名是否包含非法字符
        Asserts.failIf(!fileName.matches("^[a-zA-Z0-9._-]+$"),
                "File name contains invalid characters");
    }

    /**
     * 验证文件路径
     */
    public static void validatePath(String path) {
        // 检查路径深度
        String[] parts = path.split("/");
        Asserts.failIf(parts.length > DocumentConstant.MAX_PATH_DEPTH,
                "Path depth exceeds limit: " + DocumentConstant.MAX_PATH_DEPTH);

        // 检查路径是否包含非法字符
        Asserts.failIf(!path.matches("^[a-zA-Z0-9/_-]+$"),
                "Path contains invalid characters");
    }

    /**
     * 验证文件
     */
    public static void validateFile(MultipartFile file) {
        // 检查文件名
        String fileName = file.getOriginalFilename();
        validateFileName(fileName);

        // 检查文件类型
        String extension = FilenameUtils.getExtension(fileName).toLowerCase();
        Asserts.failIf(!DocumentConstant.ALLOW_FILE_TYPE.contains(extension),
                "File type not allowed: " + extension);

        // 检查文件大小
        long maxSize = DocumentConstant.FILE_TYPE_SIZE_LIMIT.get(extension);
        Asserts.failIf(file.getSize() > maxSize,
                "File size exceeds limit for type " + extension + ": " + maxSize + " bytes");
    }
} 