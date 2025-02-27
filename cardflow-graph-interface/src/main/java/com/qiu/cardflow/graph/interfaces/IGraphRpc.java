package com.qiu.cardflow.graph.interfaces;

import java.util.List;

import com.qiu.cardflow.common.interfaces.exception.BusinessException;
import com.qiu.cardflow.common.interfaces.exception.RPC;
import com.qiu.cardflow.graph.dto.CardNodeDTO;
import com.qiu.cardflow.graph.dto.GraphDTO;

/**
 * 知识图谱的RPC服务接口
 */
public interface IGraphRpc extends RPC {

    /**
     * 新增抽认卡
     *
     * @return
     */
    boolean addCard(CardNodeDTO cardDTO) throws BusinessException;

    /**
     * 删除抽认卡
     *
     * @param cardId
     * @return
     */
    boolean deleteCard(String cardId) throws BusinessException;

    /**
     * 更新抽认卡
     *
     * @return
     */
    boolean updateCard(CardNodeDTO cardDTO) throws BusinessException;

    /**
     * 获取用户的知识图谱
     *
     * @return
     */
    GraphDTO getTagsGraph() throws BusinessException;

    /**
     * 根据知识点标签列表获取所有含有这些标签的卡片ID
     * @param tagNames 标签名列表
     * @return 卡片ID列表
     */
    List<String> getCardsByTags(List<String> tagNames) throws BusinessException;
}
