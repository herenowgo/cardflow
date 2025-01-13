package com.qiu.qoj.document.model.vo;

import com.qiu.qoj.document.model.enums.ResourceType;
import lombok.Data;

import java.util.Date;

/**
 * 学习资源返回VO
 */
@Data
public class StudyResourceVO {
    private String id;
    private Long creatorId; // 创建者ID
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
    private Date createTime; // 创建时间
    private Date updateTime; // 更新时间
}