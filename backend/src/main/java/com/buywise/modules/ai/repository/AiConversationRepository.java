package com.buywise.modules.ai.repository;

import com.buywise.modules.ai.model.AiConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AiConversationRepository extends JpaRepository<AiConversation, Long> {

    List<AiConversation> findByUserIdOrderByUpdatedAtDesc(Long userId);

    Optional<AiConversation> findByIdAndUserId(Long id, Long userId);
}
