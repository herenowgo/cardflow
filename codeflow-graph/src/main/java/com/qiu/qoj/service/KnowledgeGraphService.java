package com.qiu.qoj.service;


import com.qiu.qoj.model.dto.KnowledgeGraphDTO;
import com.qiu.qoj.model.entity.KnowledgeTag;
import com.qiu.qoj.model.entity.KnowledgeTagRelation;
import com.qiu.qoj.repository.KnowledgeTagRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class KnowledgeGraphService {

    @Autowired
    private KnowledgeTagRepository knowledgeTagRepository;

    /**
     * 获取用户的知识图谱
     */
    public KnowledgeGraphDTO getUserKnowledgeGraph(Long userId) {
        List<Map<String, Object>> graphData = knowledgeTagRepository.getUserKnowledgeGraph(userId);

        Set<KnowledgeGraphDTO.KnowledgeNodeDTO> nodes = new HashSet<>();
        Set<KnowledgeGraphDTO.KnowledgeRelationDTO> relations = new HashSet<>();

        for (Map<String, Object> data : graphData) {
            // 处理知识点节点
            KnowledgeTag tag = (KnowledgeTag) data.get("k");
            nodes.add(convertToNodeDTO(tag));

            // 处理关联关系
            List<KnowledgeTagRelation> tagRelations = (List<KnowledgeTagRelation>) data.get("relations");
            List<KnowledgeTag> relatedTags = (List<KnowledgeTag>) data.get("relatedTags");
            processRelations(tagRelations, relatedTags, relations);

            // 处理父子关系
            List<Relationship> parentRelations = (List<Relationship>) data.get("parentRelations");
            List<KnowledgeTag> parents = (List<KnowledgeTag>) data.get("parents");
            processParentRelations(tag, parents, relations);
        }

        return KnowledgeGraphDTO.builder()
                .nodes(new ArrayList<>(nodes))
                .relations(new ArrayList<>(relations))
                .build();
    }

    private KnowledgeNodeDTO convertToNodeDTO(KnowledgeTag tag) {
        return KnowledgeNodeDTO.builder()
                .id(tag.getId())
                .name(tag.getName())
                .type(determineNodeType(tag))
                .build();
    }

    private String determineNodeType(KnowledgeTag tag) {
        if (tag.getParent() == null) {
            return "ROOT";
        } else if (tag.getRelatedTags().isEmpty()) {
            return "LEAF";
        } else {
            return "BRANCH";
        }
    }

    private void processRelations(List<KnowledgeTagRelation> relations,
                                  List<KnowledgeTag> relatedTags,
                                  Set<KnowledgeRelationDTO> resultRelations) {
        for (int i = 0; i < relations.size(); i++) {
            KnowledgeTagRelation relation = relations.get(i);
            KnowledgeTag relatedTag = relatedTags.get(i);

            resultRelations.add(KnowledgeRelationDTO.builder()
                    .sourceId(relation.getTarget().getId())
                    .targetId(relatedTag.getId())
                    .type(relation.getType().name())
                    .weight(relation.getWeight())
                    .cooccurrenceCount(relation.getCooccurrenceCount())
                    .build());
        }
    }

    private void processParentRelations(KnowledgeTag tag,
                                        List<KnowledgeTag> parents,
                                        Set<KnowledgeRelationDTO> relations) {
        for (KnowledgeTag parent : parents) {
            relations.add(KnowledgeRelationDTO.builder()
                    .sourceId(tag.getId())
                    .targetId(parent.getId())
                    .type("IS_CHILD_OF")
                    .weight(1.0)
                    .build());
        }
    }
} 