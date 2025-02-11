package com.qiu.cardflow.controller;

import com.qiu.cardflow.common.api.BaseResponse;
import com.qiu.cardflow.common.api.UserContext;
import com.qiu.cardflow.model.dto.CardGraphDTO;
import com.qiu.cardflow.model.entity.Card;
import com.qiu.cardflow.service.CardGraphService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/graph/card")
@Slf4j
public class CardGraphController {

    @Autowired
    private CardGraphService cardGraphService;

    @PostMapping("/create")
    public BaseResponse<Card> createCard(@RequestBody CardGraphDTO cardDTO) {
        Long userId = UserContext.getUserId();

        Card card = cardGraphService.createCardWithRelations(cardDTO, userId);
        return BaseResponse.success(card);
    }
} 