package com.qiu.qoj.controller;

import com.qiu.qoj.model.dto.KnowledgeGraphDTO;
import com.qiu.qoj.service.KnowledgeGraphService;
import com.qiu.qoj.util.BaseResponse;
import com.qiu.qoj.util.ErrorCode;
import com.qiu.qoj.util.UserContext;
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