package com.qiu.qoj.document.model.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@Data
@Document(collection = "files")
public class UserFile {
    @Id
    private String id;

    private Long userId;                // 所属用户ID
    private String name;                // 文件名
    private String path;                // 存储路径
    private String type;                // 文件类型
    private Long size;                  // 文件大小(bytes)
    private String coverPath;           // 封面图片路径
    private String parentPath;          // 父目录路径
    private Boolean isFolder;           // 是否是文件夹
    private Boolean isDeleted;          // 是否已删除
    private Date createTime;            // 创建时间
    private Date updateTime;            // 更新时间
    private Date deleteTime;            // 删除时间
} 