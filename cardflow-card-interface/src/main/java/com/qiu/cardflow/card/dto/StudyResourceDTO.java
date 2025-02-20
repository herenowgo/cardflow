package com.qiu.cardflow.card.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 学习资源实体
 */
@Data
@Builder
public class StudyResourceDTO implements Serializable {

    private String id;

    private Long userId;

    private String name;

    private ResourceType resourceType;

    private String parentPath;

    private List<String> structuredTags;

    private Long size;

    private String coverUrl;

    private String description;

    private String content;

    private String note;

    private String resourceUrl;

    private String objectStorageFileName;

    private Boolean isFolder;

    private Boolean isDeleted;

    private Date createTime;

    private Date updateTime;

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
