package com.qiu.cardflow.controller;

import com.qiu.cardflow.model.dto.KnowledgeGraphDTO;
import com.qiu.cardflow.service.KnowledgeGraphService;
import com.qiu.cardflow.util.BaseResponse;
import com.qiu.cardflow.util.ErrorCode;
import com.qiu.cardflow.util.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/knowledge-graph")
@Slf4j
public class KnowledgeGraphController {

    @Autowired
    private KnowledgeGraphService knowledgeGraphService;

    @GetMapping("/user")
    public BaseResponse<KnowledgeGraphDTO> getUserKnowledgeGraph() {
        Long userId = UserContext.getUserId();
        try {
            KnowledgeGraphDTO graph = knowledgeGraphService.getUserKnowledgeGraph(userId);
            return BaseResponse.success(graph);
        } catch (Exception e) {
            log.error("获取用户知识图谱失败", e);
            return BaseResponse.error(ErrorCode.SYSTEM_ERROR, "获取知识图谱失败：" + e.getMessage());
        }
    }
} 