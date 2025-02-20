package com.qiu.cardflow.card.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

/**
 * 学习资源类型枚举
 */
@Getter
@AllArgsConstructor
public enum ResourceType implements Serializable {
    PDF("PDF", "PDF文件"),
    ARTICLE("ARTICLE", "文章"),
    NOTE("NOTE", "笔记"),
    URL("URL", "在线资源");

    private final String code;
    private final String description;
}