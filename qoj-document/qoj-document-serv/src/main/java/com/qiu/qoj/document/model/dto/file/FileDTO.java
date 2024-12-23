package com.qiu.qoj.document.model.dto.file;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class FileDTO {
    private MultipartFile file;
    private String path;           // 上传的目标路径（包含文件名）
    private Boolean overwrite;     // 是否覆盖同名文件
} 