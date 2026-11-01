package com.buywise.modules.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssistantChatRequest {

    private Long conversationId;

    @NotBlank(message = "Message content is required")
    private String message;
}
