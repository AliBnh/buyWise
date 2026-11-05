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

/**
 * REST controller exposing conversational AI purchase intelligence grounded in verified consumer data.
 * 
 * Demonstrates:
 * - Anti-Hallucination Retrieval Augmented Generation (RAG): Answers are grounded strictly in authentic user purchases,
 *   warranties, and comparison matrices, preventing model confabulation.
 * - Stateful Conversation Threading: Persists message exchanges while maintaining stateless HTTP mechanics.
 * - Tool-Assisted Grounding: Dispatches deterministic tool queries to resolve exact pricing and expiration dates.
 */
@RestController
@RequestMapping("/api/v1/ai/assistant")
@RequiredArgsConstructor
@Tag(name = "AI Assistant", description = "Context-aware AI Assistant grounded in authentic user purchase and comparison data")
public class AiAssistantController {

    private final AiAssistantService assistantService;

    /**
     * Dispatches a user query to the AI assistant, executing deterministic tool retrieval before LLM synthesis.
     * 
     * @param request Validated prompt payload with optional conversationId and contextual entity references.
     * @return HTTP 200 OK with synthesized answer, cited source references, and suggested follow-ups.
     */
    @PostMapping("/chat")
    @Operation(summary = "Ask question to the BuyWise Assistant grounded in user data")
    public ResponseEntity<AssistantChatResponse> chat(@Valid @RequestBody AssistantChatRequest request) {
        return ResponseEntity.ok(assistantService.processChat(request));
    }

    /**
     * Lists prior conversation threads initiated by the authenticated user.
     * 
     * @return HTTP 200 OK with conversation thread summaries.
     */
    @GetMapping("/conversations")
    @Operation(summary = "List assistant conversations for current user")
    public ResponseEntity<List<AiConversation>> getConversations() {
        return ResponseEntity.ok(assistantService.getUserConversations());
    }

    /**
     * Retrieves historical message transcripts for an existing conversation thread.
     * 
     * @param id Target conversation ID.
     * @return HTTP 200 OK with ordered list of user and assistant messages.
     */
    @GetMapping("/conversations/{id}/messages")
    @Operation(summary = "List messages in an assistant conversation")
    public ResponseEntity<List<AiMessage>> getMessages(@PathVariable Long id) {
        return ResponseEntity.ok(assistantService.getConversationMessages(id));
    }
}
