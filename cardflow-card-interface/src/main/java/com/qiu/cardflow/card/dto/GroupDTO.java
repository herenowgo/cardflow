package com.qiu.cardflow.card.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 用来分类管理用户所有卡片的组名，和Card类中的group字段对应。每个用户对应一个group文档，所有的组名都保存在name字段里了
 */
@Data
public class GroupDTO implements Serializable {
    private String id;

    private Long userId;

    private List<String> name;
}
