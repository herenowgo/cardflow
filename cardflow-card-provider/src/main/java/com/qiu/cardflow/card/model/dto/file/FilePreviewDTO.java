package com.qiu.cardflow.card.model.dto.file;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FilePreviewDTO {
    private String type; // 文件类型
    private String url; // 预览URL
    private String content; // 文本内容(如果是文本文件)
    private Boolean canPreview; // 是否支持预览
}