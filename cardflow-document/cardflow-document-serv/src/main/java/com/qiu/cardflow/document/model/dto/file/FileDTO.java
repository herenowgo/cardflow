package com.qiu.cardflow.document.model.dto.file;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileDTO {
    private MultipartFile file;
    private String path; // 上传的目标路径（包含文件名）
    private Boolean overwrite; // 是否覆盖同名文件
}