package com.qiu.cardflow.graph.rpc;

import com.qiu.cardflow.common.interfaces.exception.BusinessException;
import com.qiu.cardflow.graph.dto.CardNodeDTO;
import com.qiu.cardflow.graph.dto.GraphDTO;
import com.qiu.cardflow.graph.interfaces.IGraphRpc;
import com.qiu.cardflow.graph.service.IGraphService;
import com.qiu.cardflow.rpc.starter.RPCContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@DubboService
@Slf4j
@RequiredArgsConstructor
@Validated
public class GraphRpcImpl implements IGraphRpc {
    private final IGraphService IGraphService;

    @Override
    public boolean addCard(CardNodeDTO cardDTO) {
        return IGraphService.addCard(cardDTO);
    }

    @Override
    public boolean deleteCard(String cardId) {
        return IGraphService.removeCard(cardId);
    }

    @Override
    public boolean updateCard(CardNodeDTO cardDTO) {
        return IGraphService.updateCard(cardDTO);
    }

    @Override
    public GraphDTO getTagsGraph() throws BusinessException {
        return IGraphService.getTagsGraph(RPCContext.getUserId());
    }

    @Override
    public List<String> getCardsByTags(List<String> tagNames) throws BusinessException {
        return IGraphService.getCardsByTags(tagNames);
    }
}
