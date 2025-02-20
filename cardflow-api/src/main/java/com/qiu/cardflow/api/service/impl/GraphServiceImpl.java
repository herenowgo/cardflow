package com.qiu.cardflow.api.service.impl;

import com.qiu.cardflow.api.service.IGraphService;
import com.qiu.cardflow.graph.dto.CardDTO;
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
    public boolean addCard(CardDTO cardDTO) {
        try {
            return graphRpc.addCard(cardDTO);
        } catch (Exception e) {
            log.error("添加卡片到知识图谱失败", e);
            return false;
        }
    }

    @Override
    public boolean deleteCard(String cardId) {
        try {
            return graphRpc.deleteCard(cardId);
        } catch (Exception e) {
            log.error("从知识图谱删除卡片失败", e);
            return false;
        }
    }

    @Override
    public boolean updateCard(CardDTO cardDTO) {
        try {
            return graphRpc.updateCard(cardDTO);
        } catch (Exception e) {
            log.error("更新知识图谱中的卡片失败", e);
            return false;
        }
    }

    @Override
    public GraphDTO getTagsGraph() {
        return graphRpc.getTagsGraph();
    }
}
