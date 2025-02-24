package com.qiu.cardflow.api.service.impl;

import com.qiu.cardflow.api.service.IGraphService;
import com.qiu.cardflow.graph.dto.CardNodeDTO;
import com.qiu.cardflow.graph.dto.GraphDTO;
import com.qiu.cardflow.graph.interfaces.IGraphRpc;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GraphServiceImpl implements IGraphService {

    @DubboReference(validation = "true")
    private IGraphRpc graphRpc;

    @Override
    public boolean addCard(CardNodeDTO cardDTO) {
        return graphRpc.addCard(cardDTO);
    }

    @Override
    public boolean deleteCard(String cardId) {
        return graphRpc.deleteCard(cardId);
    }

    @Override
    public boolean updateCard(CardNodeDTO cardDTO) {

        return graphRpc.updateCard(cardDTO);

    }

    @Override
    public GraphDTO getTagsGraph() {
        return graphRpc.getTagsGraph();
    }
}
