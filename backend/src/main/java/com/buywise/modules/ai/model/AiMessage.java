package com.buywise.modules.ai.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "ai_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    @JsonIgnore
    private AiConversation conversation;

    @Column(name = "sender_type", nullable = false, length = 50)
    private String senderType; // USER, ASSISTANT, SYSTEM

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "tool_calls_json", columnDefinition = "TEXT")
    private String toolCallsJson;

    @Column(name = "tool_results_json", columnDefinition = "TEXT")
    private String toolResultsJson;

    @Column(name = "cited_references_json", columnDefinition = "TEXT")
    private String citedReferencesJson;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
