package com.qiu.qoj.document.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 学习资源类型枚举
 */
@Getter
@AllArgsConstructor
public enum ResourceType {
    IMAGE("IMAGE", "图片"),
    PDF("PDF", "PDF文件"),
    ARTICLE("ARTICLE", "文章"),
    NOTE("NOTE", "笔记"),
    URL("URL", "在线资源"),
    OTHER("OTHER", "其他");

    private final String code;
    private final String description;
}