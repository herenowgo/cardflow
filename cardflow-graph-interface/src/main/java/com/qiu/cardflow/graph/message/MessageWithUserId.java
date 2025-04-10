package com.qiu.cardflow.graph.message;

import java.util.List;

import com.qiu.cardflow.graph.dto.CardNodeDTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MessageWithUserId {
    Long userId;
    List<CardNodeDTO> data;
}
