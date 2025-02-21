package com.qiu.cardflow.card.dto.anki;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
public class AnkiNoteAddRequest implements Serializable {
    private String id;

    private String question;

    private String answer;

    private String deckName;

    private String modelName;

    private List<String> tags;
}
