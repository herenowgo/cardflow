package com.qiu.qoj.document.model.dto.card;

import lombok.Builder;
import lombok.Data;

import java.util.List;


/**
 * 与 anki 进行同步
 * 1. 新卡片的同步
 * 系统中没有cardID的卡片
 * anki中有但是系统中没有的AnkiInfo的cardID——需要系统中用户所有的AnkiInfo的cardID
 * 2. 更新过的卡片的同步
 * 判断系统中的卡片是否更新了：modifiedTime > AnkiInfo的syncTime
 * 判断anki中的卡片是否更新了：anki笔记的mod > AnkiInfo的syncTime
 * 都判断之后，如果只有一边更新了，就同步给另一端
 * 否则都显示出来，让用户去选择
 * 3. 后端接口设计
 * 所有卡片的同步时间、AnkiInfo的cardID、和modifiedTime
 * 以及没有cardID的卡片的数据
 */
@Data
@Builder
public class AnkiSyncResponse {

    // 已经与Anki同步过的卡片
    List<AnkiSyncedCard> ankiSyncedCards;

    // 所有卡片的AnkiInfo的cardID
    List<Long> cardIds;

    // 没有cardID的卡片
    List<AnkiNoteAddRequest> ankiNoteAddRequests;


    @Data
    @Builder
    public static class AnkiSyncedCard {
        String id;
        Long cardId;
        Long syncTime;
        Long modifiedTime;
    }
}
