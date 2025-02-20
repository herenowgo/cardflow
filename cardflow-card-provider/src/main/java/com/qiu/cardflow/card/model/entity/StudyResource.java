package com.qiu.cardflow.card.model.entity;

import com.qiu.cardflow.card.dto.ResourceType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

/**
 * 学习资源实体
 */
@Data
@Document(collection = "study_resources")
@Schema(description = "学习资源实体")
@Builder
public class StudyResource {
    @Schema(description = "资源ID")
    @Id
    private String id;

    @Schema(description = "所属用户ID")
    private Long userId;

    @Schema(description = "资源名称")
    private String name;

    @Schema(description = "资源类型")
    private ResourceType resourceType;

    @Schema(description = "逻辑父目录路径")
    private String parentPath;

    @Schema(description = "结构化标签")
    private List<String> structuredTags;

    @Schema(description = "文件大小(bytes)")
    private Long size;

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

    @Schema(description = "对象存储中的文件名")
    private String objectStorageFileName;

    @Schema(description = "是否是文件夹")
    private Boolean isFolder;

    @Schema(description = "是否已删除")
    private Boolean isDeleted;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;

    @Schema(description = "删除时间")
    private Date deleteTime;

    /**
     * 获取资源的完整路径
     *
     * @return 完整路径
     */
    public String getPath() {
        if (parentPath == null) {
            parentPath = "/";
        }
        String fullPath = parentPath.endsWith("/") ? parentPath + name : parentPath + "/" + name;
        return isFolder ? fullPath + "/" : fullPath;
    }
}
