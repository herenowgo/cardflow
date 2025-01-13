package com.qiu.qoj.document.model.dto;

import com.qiu.qoj.document.model.enums.ResourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 学习资源请求DTO
 */
@Data
public class StudyResourceRequest {
    @NotBlank(message = "资源名称不能为空")
    private String name; // 资源名称

    @NotNull(message = "资源类型不能为空")
    private ResourceType resourceType; // 资源类型

    private String parentPath; // 父目录路径
    private String coverUrl; // 封面图片URL
    private String description; // 描述信息
    private String content; // 文本内容（用于文章类型）
    private String note; // 笔记内容
    private String resourceUrl; // 在线资源URL
}