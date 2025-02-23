package com.qiu.cardflow.api.service;

import com.qiu.cardflow.graph.dto.CardNodeDTO;
import com.qiu.cardflow.graph.dto.GraphDTO;

public interface IGraphService {
    /**
     * 添加卡片到知识图谱
     */
    boolean addCard(CardNodeDTO cardDTO);

    /**
     * 从知识图谱中删除卡片
     */
    boolean deleteCard(String cardId);

    /**
     * 更新知识图谱中的卡片
     */
    boolean updateCard(CardNodeDTO cardDTO);

    /**
     * 获取用户的知识标签图谱
     */
    GraphDTO getTagsGraph();
}
