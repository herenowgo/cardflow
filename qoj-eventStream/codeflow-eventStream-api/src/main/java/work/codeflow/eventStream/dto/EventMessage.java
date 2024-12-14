package work.codeflow.eventStream.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class EventMessage {
    private String requestId;
    private String userId;
    private EventType eventType;
    private Integer sequence;
    private Object data;
} 