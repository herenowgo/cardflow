package com.qiu.cardflow.document.model.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 更新学习资源请求DTO
 */
@Data
public class UpdateStudyResourceRequest {
    @NotBlank(message = "资源ID不能为空")
    private String id; // 资源ID

    private String parentPath; // 父目录路径
    private String name; // 资源名称
    private String description; // 描述信息
    private String content; // 文本内容（用于文章类型）
    private String note; // 笔记内容
    private String coverUrl; // 封面图片URL
    private String resourceUrl; // 在线资源URL
    @Schema(description = "结构化标签")
    private List<String> structuredTags;

    @Schema(description = "是否公开（仅管理员可设置）")
    private Boolean isPublic;
}