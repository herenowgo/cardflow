package com.qiu.cardflow.document.model.dto;

import com.qiu.cardflow.document.model.enums.ResourceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 学习资源请求DTO
 */
@Data
@Schema(description = "学习资源请求")
public class StudyResourceRequest {
    @Schema(description = "资源名称", example = "Java编程思想笔记")
    @NotBlank(message = "资源名称不能为空")
    private String name;

    @Schema(description = "资源类型", example = "ARTICLE")
    @NotNull(message = "资源类型不能为空")
    private ResourceType resourceType;

    @Schema(description = "父目录路径（以'/'结尾）", example = "/学习笔记/", defaultValue = "/")
    private String parentPath = "/";

    @Schema(description = "封面图片URL")
    private String coverUrl;

    @Schema(description = "描述信息")
    private String description;

    @Schema(description = "文本内容（用于文章类型）")
    private String content;

    @Schema(description = "笔记内容")
    private String note;

    @Schema(description = "在线资源URL")
    private String resourceUrl;
}