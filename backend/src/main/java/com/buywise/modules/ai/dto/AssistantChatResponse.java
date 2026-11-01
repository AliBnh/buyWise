package com.buywise.modules.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssistantChatResponse {

    private Long conversationId;
    private String response;
    private List<String> toolCallsExecuted;
    private List<CitedReferenceDto> citedReferences;
    private Instant timestamp;
}
