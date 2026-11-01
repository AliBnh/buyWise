package com.buywise.modules.ai.controller;

import com.buywise.modules.ai.dto.AssistantChatRequest;
import com.buywise.modules.ai.dto.AssistantChatResponse;
import com.buywise.modules.ai.model.AiConversation;
import com.buywise.modules.ai.model.AiMessage;
import com.buywise.modules.ai.service.AiAssistantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ai/assistant")
@RequiredArgsConstructor
@Tag(name = "AI Assistant", description = "Context-aware AI Assistant grounded in authentic user purchase and comparison data")
public class AiAssistantController {

    private final AiAssistantService assistantService;

    @PostMapping("/chat")
    @Operation(summary = "Ask question to the BuyWise Assistant grounded in user data")
    public ResponseEntity<AssistantChatResponse> chat(@Valid @RequestBody AssistantChatRequest request) {
        return ResponseEntity.ok(assistantService.processChat(request));
    }

    @GetMapping("/conversations")
    @Operation(summary = "List assistant conversations for current user")
    public ResponseEntity<List<AiConversation>> getConversations() {
        return ResponseEntity.ok(assistantService.getUserConversations());
    }

    @GetMapping("/conversations/{id}/messages")
    @Operation(summary = "List messages in an assistant conversation")
    public ResponseEntity<List<AiMessage>> getMessages(@PathVariable Long id) {
        return ResponseEntity.ok(assistantService.getConversationMessages(id));
    }
}
