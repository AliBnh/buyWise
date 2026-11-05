package com.buywise.modules.ai.model;

import com.buywise.common.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity managing multi-turn conversational session threads with the AI purchase assistant.
 * 
 * Demonstrates:
 * - Bidirectional JPA Parent-Child Association: Maintains a one-to-many relationship with {@link AiMessage}.
 * - Defensive Cascade & Orphan Removal: Ensures child messages are automatically persisted or removed with thread.
 * - Lazy Loading: Configures FetchType.LAZY to prevent accidental loading of lengthy message histories.
 */
@Entity
@Table(name = "ai_conversations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiConversation extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 255)
    @Builder.Default
    private String title = "New Conversation";

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("createdAt ASC")
    @Builder.Default
    private List<AiMessage> messages = new ArrayList<>();

    /**
     * Synchronizes both sides of the bidirectional relationship in memory before JPA flush.
     * 
     * @param message New message to append to this conversation thread.
     */
    public void addMessage(AiMessage message) {
        messages.add(message);
        message.setConversation(this);
    }
}
