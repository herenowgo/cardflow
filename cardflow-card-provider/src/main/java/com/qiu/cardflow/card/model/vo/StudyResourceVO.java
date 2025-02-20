package com.qiu.cardflow.card.model.vo;

import com.qiu.cardflow.card.dto.ResourceType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 学习资源返回VO
 */
@Data
public class StudyResourceVO {
    private String id;
    private String name; // 资源名称
    private ResourceType resourceType; // 资源类型
    private String path; // 资源存储路径
    private String parentPath; // 父目录路径
    private Long size; // 文件大小(bytes)
    private String coverUrl; // 封面图片URL
    private String description; // 描述信息
    private String content; // 文本内容（用于文章类型）
    private String note; // 笔记内容
    private String resourceUrl; // 在线资源URL
    @Schema(description = "结构化标签")
    private List<String> structuredTags;
}