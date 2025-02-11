package com.qiu.cardflow.service;

import com.qiu.cardflow.common.exception.ApiException;
import com.qiu.cardflow.model.dto.CardGraphDTO;
import com.qiu.cardflow.model.entity.Card;
import com.qiu.cardflow.model.entity.KnowledgeTag;
import com.qiu.cardflow.model.entity.Material;
import com.qiu.cardflow.model.entity.StructuredTag;
import com.qiu.cardflow.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@Transactional
public class CardGraphService {

    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private KnowledgeTagRepository knowledgeTagRepository;
    @Autowired
    private MaterialRepository materialRepository;
    @Autowired
    private StructuredTagRepository structuredTagRepository;
    @Autowired
    private UserRepository userRepository;

    public Card createCardWithRelations(CardGraphDTO cardDTO, Long userId) {
        // 1. 创建卡片
        Card card = new Card(cardDTO.getId(), cardDTO.getQuestion());

        // 2. 关联学习资料
        Material material = getMaterial(cardDTO.getMaterialId());
        card.setBasedOnMaterial(material);

        // 3. 处理知识点标签
        List<KnowledgeTag> knowledgeTags = processKnowledgeTags(cardDTO.getKnowledgeTagNames(), material, userId);
        card.getKnowledgeTags().addAll(knowledgeTags);

        // 4. 处理结构化标签
        StructuredTag structuredTag = processStructuredTag(cardDTO.getStructuredTagPath(), card, material);

        // 5. 保存卡片及其关系
        card = cardRepository.save(card);

        // 6. todo 分析并建立知识点之间的关联
//        analyzeAndCreateKnowledgeRelations(knowledgeTags);

        return card;
    }

    private Material getMaterial(String materialId) {
        return materialRepository.findById(materialId)
                .orElseThrow(() -> new ApiException("学习资料不存在"));
    }

    private List<KnowledgeTag> processKnowledgeTags(List<String> tagNames, Material material, Long userId) {
        List<KnowledgeTag> tags = new ArrayList<>();

        for (String name : tagNames) {
            // 1. 查找或创建知识点
            KnowledgeTag tag = knowledgeTagRepository.findByName(name)
                    .stream().findFirst()
                    .orElseGet(() -> {
                        KnowledgeTag newTag = new KnowledgeTag(name);
                        return knowledgeTagRepository.save(newTag);
                    });

            // 2. 建立与学习资料的关联
            if (!material.getCoveredKnowledge().contains(tag)) {
                material.getCoveredKnowledge().add(tag);
                materialRepository.save(material);
            }

            tags.add(tag);
        }

        return tags;
    }

    private StructuredTag processStructuredTag(String tagPath, Card card, Material material) {
        // 1. 查找或创建结构化标签
        StructuredTag structuredTag = structuredTagRepository.findById(tagPath)
                .orElseGet(() -> {
                    StructuredTag newTag = new StructuredTag(tagPath);
                    return structuredTagRepository.save(newTag);
                });

        // 2. 建立与卡片的关联
        structuredTag.getOrganizedCards().add(card);

        // 3. 建立与学习资料的关联
        if (!material.getStructuredTags().contains(structuredTag)) {
            material.getStructuredTags().add(structuredTag);
            materialRepository.save(material);
        }

        return structuredTagRepository.save(structuredTag);
    }

} 