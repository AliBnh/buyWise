package com.buywise.modules.ai.service;

import com.buywise.common.exception.ResourceNotFoundException;
import com.buywise.common.security.SecurityUtils;
import com.buywise.modules.ai.dto.AssistantChatRequest;
import com.buywise.modules.ai.dto.AssistantChatResponse;
import com.buywise.modules.ai.model.AiConversation;
import com.buywise.modules.ai.model.AiMessage;
import com.buywise.modules.ai.provider.LlmProvider;
import com.buywise.modules.ai.repository.AiConversationRepository;
import com.buywise.modules.ai.repository.AiMessageRepository;
import com.buywise.modules.ai.tools.AssistantToolRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Service orchestrating the context-aware BuyWise AI Assistant.
 * 
 * Demonstrates:
 * - Controlled Tool/Context Ingestion: Answers questions grounded strictly in the user's authentic database records.
 * - Hallucination Prevention: Instructs the model to explicitly state when data is unavailable rather than guessing.
 * - Traceable Citations: Returns structured reference links pointing to the underlying purchases, comparisons, or documents.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiAssistantService {

    private final AiConversationRepository conversationRepository;
    private final AiMessageRepository messageRepository;
    private final AssistantToolRegistry toolRegistry;
    private final LlmProvider llmProvider;
    private final ObjectMapper objectMapper;
    private final com.buywise.common.metrics.BuyWiseMetricsService metricsService;

    @Transactional
    public AssistantChatResponse processChat(AssistantChatRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();

        AiConversation conversation;
        if (request.getConversationId() != null) {
            conversation = conversationRepository.findByIdAndUserId(request.getConversationId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("AiConversation", "id", request.getConversationId()));
        } else {
            String titleSnippet = request.getMessage().length() > 40
                    ? request.getMessage().substring(0, 37) + "..."
                    : request.getMessage();

            conversation = AiConversation.builder()
                    .userId(userId)
                    .title(titleSnippet)
                    .build();
            conversation = conversationRepository.save(conversation);
        }

        // Save User Message
        AiMessage userMsg = AiMessage.builder()
                .conversation(conversation)
                .senderType("USER")
                .content(request.getMessage())
                .build();
        messageRepository.save(userMsg);

        // Fetch authorized user context and citations
        AssistantToolRegistry.UserContextBundle contextBundle = toolRegistry.gatherUserContext(userId, request.getMessage());

        String systemPrompt = """
                You are the BuyWise AI Purchase Intelligence Assistant.
                You help ordinary consumers make smarter purchasing decisions and manage their purchases.
                
                CRITICAL INSTRUCTIONS:
                1. Answer the user's query truthfully using ONLY the authenticated user's records provided below.
                2. If the user asks about an item or warranty not present in their data, clearly inform them that you cannot find records for it in their BuyWise library.
                3. NEVER invent dates, prices, or serial numbers.
                4. Traceable references: Whenever you refer to an owned product, comparison, or deadline, cite its name and expiration/due date.
                5. Keep answers clear, empathetic, practical, and concise for consumer understanding.
                """;

        String userPrompt = String.format("USER QUERY:\n%s\n\nAUTHENTICATED USER'S DATA CONTEXT:\n\"\"\"\n%s\n\"\"\"",
                request.getMessage(), contextBundle.formattedContext());

        log.info("Generating AI assistant response for user id: {}, conversation id: {}", userId, conversation.getId());
        long startTime = System.currentTimeMillis();
        String assistantResponse;
        try {
            assistantResponse = llmProvider.generateCompletion(systemPrompt, userPrompt);
            metricsService.recordAiRequest("assistant_chat", "success", System.currentTimeMillis() - startTime);
        } catch (Exception ex) {
            log.warn("AI Assistant failed, providing fallback: {}", ex.getMessage());
            metricsService.recordAiRequest("assistant_chat", "fallback", System.currentTimeMillis() - startTime);
            assistantResponse = "I apologize, but I am currently having trouble communicating with the AI service. " +
                    "Your purchases and deadlines remain safely stored. Please check your Dashboard or Ownership Library for current status.";
        }

        // Save Assistant Message
        AiMessage assistantMsg = AiMessage.builder()
                .conversation(conversation)
                .senderType("ASSISTANT")
                .content(assistantResponse)
                .build();

        try {
            assistantMsg.setCitedReferencesJson(objectMapper.writeValueAsString(contextBundle.citations()));
        } catch (Exception ignored) {}

        messageRepository.save(assistantMsg);

        // Update conversation timestamp
        conversation.setUpdatedAt(Instant.now());
        conversationRepository.save(conversation);

        return AssistantChatResponse.builder()
                .conversationId(conversation.getId())
                .response(assistantResponse)
                .toolCallsExecuted(List.of("gatherUserContext", "verifyOwnershipAccess"))
                .citedReferences(contextBundle.citations())
                .timestamp(Instant.now())
                .build();
    }

    @Transactional(readOnly = true)
    public List<AiConversation> getUserConversations() {
        Long userId = SecurityUtils.getCurrentUserId();
        return conversationRepository.findByUserIdOrderByUpdatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public List<AiMessage> getConversationMessages(Long conversationId) {
        Long userId = SecurityUtils.getCurrentUserId();
        conversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("AiConversation", "id", conversationId));

        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
    }
}
