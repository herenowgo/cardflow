package com.qiu.cardflow.ai.structured;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Cards implements Serializable {
    List<Card> cards;
}
