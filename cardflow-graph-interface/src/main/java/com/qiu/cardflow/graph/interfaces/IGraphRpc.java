package com.qiu.cardflow.graph.interfaces;

import com.qiu.cardflow.graph.dto.CardDTO;
import com.qiu.cardflow.graph.dto.GraphDTO;

/**
 * 知识图谱的RPC服务接口
 */
public interface IGraphRpc {

    /**
     * 新增抽认卡
     * @return
     */
    boolean addCard(CardDTO cardDTO);

    /**
     * 删除抽认卡
     * @param cardId
     * @return
     */
    boolean deleteCard(String cardId);

    /**
     * 更新抽认卡
     * @return
     */
    boolean updateCard(CardDTO cardDTO);

    /**
     * 获取用户的知识图谱
     * @param userId
     * @return
     */
    GraphDTO getTagsGraph(Long userId);
}
