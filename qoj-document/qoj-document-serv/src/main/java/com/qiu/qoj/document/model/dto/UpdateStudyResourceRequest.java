package com.qiu.qoj.document.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 更新学习资源请求DTO
 */
@Data
public class UpdateStudyResourceRequest {
    @NotBlank(message = "资源ID不能为空")
    private String id; // 资源ID

    private String name; // 资源名称
    private String description; // 描述信息
    private String content; // 文本内容（用于文章类型）
    private String note; // 笔记内容
    private String coverUrl; // 封面图片URL
    private String resourceUrl; // 在线资源URL
}