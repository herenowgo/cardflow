package com.qiu.qoj.document.model.dto.card;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AnkiNoteAddRequest {
    private String question;

    private String answer;

    String deckName;

    String modelName;

    List<String> tags;
}
