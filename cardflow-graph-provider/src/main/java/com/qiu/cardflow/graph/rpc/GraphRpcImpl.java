package com.qiu.cardflow.graph.rpc;

import com.qiu.cardflow.common.interfaces.exception.BusinessException;
import com.qiu.cardflow.graph.dto.CardDTO;
import com.qiu.cardflow.graph.dto.GraphDTO;
import com.qiu.cardflow.graph.interfaces.IGraphRpc;
import com.qiu.cardflow.graph.service.IGraphService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.validation.annotation.Validated;

@DubboService
@Slf4j
@RequiredArgsConstructor
@Validated
public class GraphRpcImpl implements IGraphRpc {
    private final IGraphService IGraphService;

    @Override
    public boolean addCard(CardDTO cardDTO) {
        return IGraphService.addCard(cardDTO);
    }

    @Override
    public boolean deleteCard(String cardId) {
        return IGraphService.removeCard(cardId);
    }

    @Override
    public boolean updateCard(CardDTO cardDTO) {
        return IGraphService.updateCard(cardDTO);
    }

    @Override
    public GraphDTO getTagsGraph(@Valid @NotNull(message = "用户id不能为空!") @Min(value = 1, message = "用户id不能小于1") Long userId) throws BusinessException {
        return IGraphService.getTagsGraph(userId);
    }
}
