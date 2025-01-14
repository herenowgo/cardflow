package com.qiu.qoj.document.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import com.qiu.qoj.document.model.enums.ResourceType;

/**
 * 文件列表返回VO
 */
@Data
@Builder
@Schema(description = "文件列表项")
public class FileListVO {
    @Schema(description = "文件/文件夹ID")
    private String id;

    @Schema(description = "文件/文件夹名称")
    private String name;

    @Schema(description = "是否是文件夹")
    private Boolean isFolder;

    @Schema(description = "资源类型", example = "PDF")
    private ResourceType resourceType;
}