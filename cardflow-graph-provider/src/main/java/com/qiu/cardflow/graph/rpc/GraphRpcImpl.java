package com.qiu.cardflow.graph.rpc;

import com.qiu.cardflow.graph.dto.CardDTO;
import com.qiu.cardflow.graph.dto.GraphDTO;
import com.qiu.cardflow.graph.interfaces.IGraphRpc;
import com.qiu.cardflow.graph.service.IGraphService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
@Slf4j
@RequiredArgsConstructor
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
    public GraphDTO getTagsGraph(Long userId) {
        return IGraphService.getTagsGraph(userId);
    }
}
