package com.qiu.cardflow.api.service.impl;

import java.util.List;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import com.qiu.cardflow.api.context.UserContext;
import com.qiu.cardflow.api.service.IGraphService;
import com.qiu.cardflow.card.dto.card.CardDTO;
import com.qiu.cardflow.card.interfaces.ICardRPC;
import com.qiu.cardflow.graph.dto.CardNodeDTO;
import com.qiu.cardflow.graph.dto.GraphDTO;
import com.qiu.cardflow.graph.interfaces.IGraphRpc;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class GraphServiceImpl implements IGraphService {

    @DubboReference(validation = "true")
    private IGraphRpc graphRpc;

    @DubboReference(validation = "true")
    private ICardRPC cardRpc;

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
    public GraphDTO getTagsGraph(Boolean overt) {
        if (overt == null || !overt) {
            return graphRpc.getTagsGraph();

        }
        Long userId = UserContext.getUserId();
        UserContext.setUserId(-1l);
        GraphDTO res = graphRpc.getTagsGraph();
        UserContext.setUserId(userId);
        return res;
    }

    @Override
    public List<CardDTO> getCardsByTags(List<String> tags) {
        List<String> cardIdList = graphRpc.getCardsByTags(tags);
        ;
        return cardRpc.getCardsByIds(cardIdList);
    }
}
