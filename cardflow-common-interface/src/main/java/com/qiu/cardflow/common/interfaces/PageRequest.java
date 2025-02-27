package com.qiu.cardflow.common.interfaces;

import java.io.Serializable;

import lombok.Data;

/**
 * 分页请求
 */
@Data
public class PageRequest implements Serializable {

    /**
     * 当前页号
     */
    private Long current = 1l;

    /**
     * 页面大小
     */
    private Long pageSize = 10l;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序顺序（默认升序）
     */
    private String sortOrder = "ascend";
}
