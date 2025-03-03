package com.qiu.cardflow.graph.service;

import com.qiu.cardflow.graph.dto.CardNodeDTO;
import com.qiu.cardflow.graph.dto.GraphDTO;

import java.util.List;

public interface IGraphService {
    /**
     * 添加卡片节点及其关系
     */
    boolean addCard(CardNodeDTO cardDTO);

    /**
     * 批量添加卡片节点及其关系
     */
    boolean addCards(List<CardNodeDTO> cardDTOList, Long userId);

    /**
     * 删除卡片节点及其关系
     */
    boolean removeCard(String cardId);

    /**
     * 更新卡片节点及其关系
     */
    boolean updateCard(CardNodeDTO cardDTO);

    /**
     * 获取用户的标签图谱
     * 1. 所有的知识点标签，包括每个知识点的属于这个用户的HAS_TAG关系有多少个，把这个数量作为NoteDTO的value也就是这儿知识点的权重
     * 2. 每个知识点的属于这个用户的HAS_TAG关系：从用户节点出发到卡片节点，再到知识点节点，获取这个关系的数量，作为这个知识点节点的权重value
     * 3. 图中的边的话就是与同一个卡片节点有关联的知识点节点，这两个通过同一个卡片关联的知识点节点连起来就是边。也就是将在同一个卡片上的知识点的共现关系作为图的边。每条边两端的知识点节点共现了几次，这条边的权重weight就是多少。
     */
    GraphDTO getTagsGraph(Long userId);

    /**
     * 根据知识点标签列表获取当前用户所有含有这些标签的卡片ID
     * @param tagNames 标签名列表
     * @return 卡片ID列表
     */
    List<String> getCardsByTags(List<String> tagNames);
}