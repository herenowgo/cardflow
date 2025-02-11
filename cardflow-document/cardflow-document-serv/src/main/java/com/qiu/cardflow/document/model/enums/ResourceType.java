package com.qiu.cardflow.document.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 学习资源类型枚举
 */
@Getter
@AllArgsConstructor
public enum ResourceType {
    PDF("PDF", "PDF文件"),
    ARTICLE("ARTICLE", "文章"),
    NOTE("NOTE", "笔记"),
    URL("URL", "在线资源");

    private final String code;
    private final String description;
}