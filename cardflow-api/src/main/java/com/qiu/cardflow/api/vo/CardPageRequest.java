package com.qiu.cardflow.api.vo;

import java.util.List;

import com.qiu.cardflow.api.common.PageRequest;

import lombok.Data;

@Data
public class CardPageRequest extends PageRequest {

    private Boolean overt = false; // 是否公开
    private String question; // 模糊查找问题/正面内容的所有卡片
    private String answer; // 模糊查找答案/背面内容的所有卡片
    private List<String> tags; // 标签列表，查找同时包含这些标签的所有卡片
    private String group; // 所属分组

}
