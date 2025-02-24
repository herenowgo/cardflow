package com.qiu.cardflow.graph.service.impl;

import com.qiu.cardflow.common.interfaces.exception.BusinessException;
import com.qiu.cardflow.graph.constants.NodeType;
import com.qiu.cardflow.graph.dto.CardNodeDTO;
import com.qiu.cardflow.graph.dto.EdgeDTO;
import com.qiu.cardflow.graph.dto.GraphDTO;
import com.qiu.cardflow.graph.dto.NodeDTO;
import com.qiu.cardflow.graph.model.entity.CardNode;
import com.qiu.cardflow.graph.model.entity.TagNode;
import com.qiu.cardflow.graph.model.entity.UserNode;
import com.qiu.cardflow.graph.repository.CardNodeRepository;
import com.qiu.cardflow.graph.repository.TagNodeRepository;
import com.qiu.cardflow.graph.repository.UserNodeRepository;
import com.qiu.cardflow.graph.service.IGraphService;
import com.qiu.cardflow.rpc.starter.RPCContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Validated
public class IGraphServiceImpl implements IGraphService {
    private final CardNodeRepository cardNodeRepository;
    private final TagNodeRepository tagNodeRepository;
    private final UserNodeRepository userNodeRepository;

    @Override
    public boolean addCard(CardNodeDTO cardDTO) {

        UserNode userNode = new UserNode(RPCContext.getUserId());
        List<TagNode> tagNodeList = cardDTO.getTags().stream()
                .map(TagNode::new)
                .toList();

        CardNode cardNode = CardNode.builder()
                .userNode(userNode)
                .cardId(cardDTO.getCardId())
                .tagNodeList(tagNodeList)
                .build();

        // 4. 保存卡片节点及其关系
        cardNodeRepository.save(cardNode);
        return true;

    }

    @Override
    public boolean removeCard(String cardId) {
        try {
            // 使用新的删除方法，同时删除节点和关系
            cardNodeRepository.deleteCardWithRelationships(cardId, RPCContext.getUserId());
            return true;
        } catch (Exception e) {
            log.error("删除卡片及其关系失败，cardId: {}", cardId, e);
            throw new BusinessException("删除卡片失败");
        }
    }

    @Override
    public boolean updateCard(CardNodeDTO cardDTO) {
        try {
            // 1. 更新卡片的标签关系
            cardNodeRepository.updateCardTags(cardDTO.getCardId(), RPCContext.getUserId(), cardDTO.getTags());
            return true;
        } catch (Exception e) {
            log.error("更新卡片失败，cardId: {}", cardDTO.getCardId(), e);
            throw new BusinessException("更新卡片失败");
        }
    }

    @Override
    public GraphDTO getTagsGraph(Long userId) {
        GraphDTO graphDTO = new GraphDTO();
        List<NodeDTO> nodes = new ArrayList<>();
        List<EdgeDTO> edges = new ArrayList<>();

        // 1. 获取所有标签节点及其权重
        List<CardNodeRepository.TagWeightResult> tagWeights = cardNodeRepository.findUserTagsWithWeight(userId);

        // 使用Map保存标签名到节点ID的映射，方便后续创建边
        Map<String, Long> tagNameToId = new HashMap<>();
        long nodeId = 0;

        // 创建节点
        for (CardNodeRepository.TagWeightResult tagWeight : tagWeights) {
            NodeDTO node = new NodeDTO();
            node.setId(nodeId);
            node.setName(tagWeight.getTagName());
            node.setValue(String.valueOf(tagWeight.getWeight())); // 设置节点权重
            node.setType(NodeType.TAG);

            nodes.add(node);
            tagNameToId.put(tagWeight.getTagName(), nodeId);
            nodeId++;
        }

        // 2. 获取标签之间的共现关系
        List<CardNodeRepository.TagCoOccurrenceResult> coOccurrences = cardNodeRepository.findTagCoOccurrences(userId);

        // 创建边
        for (CardNodeRepository.TagCoOccurrenceResult coOccurrence : coOccurrences) {
            EdgeDTO edge = new EdgeDTO();
            edge.setSource(tagNameToId.get(coOccurrence.getSourceTag()));
            edge.setTarget(tagNameToId.get(coOccurrence.getTargetTag()));
            edge.setWeight(coOccurrence.getWeight().intValue());
            // edge.setName("相关"); // 边的类型名称

            edges.add(edge);
        }

        graphDTO.setNodes(nodes);
        graphDTO.setEdges(edges);
        return graphDTO;
    }
}