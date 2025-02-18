package com.qiu.cardflow.graph.interfaces;

import com.qiu.cardflow.common.interfaces.exception.BusinessException;
import com.qiu.cardflow.graph.dto.CardDTO;
import com.qiu.cardflow.graph.dto.GraphDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 知识图谱的RPC服务接口
 */
public interface IGraphRpc {

    /**
     * 新增抽认卡
     *
     * @return
     */
    boolean addCard(CardDTO cardDTO) throws BusinessException;

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
    boolean updateCard(CardDTO cardDTO) throws BusinessException;

    /**
     * 获取用户的知识图谱
     *
     * @param userId
     * @return
     */
    GraphDTO getTagsGraph(@Valid @NotNull(message = "用户id不能为空!") @Min(value = 1, message = "用户id不能小于1") Long userId) throws BusinessException;
}
