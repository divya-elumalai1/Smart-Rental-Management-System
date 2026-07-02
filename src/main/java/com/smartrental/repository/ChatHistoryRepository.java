package com.smartrental.repository;

import com.smartrental.model.ChatHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for {@link ChatHistory} entity.
 * Provides lookup queries by user ordered by creation date.
 */
@Repository
public interface ChatHistoryRepository extends JpaRepository<ChatHistory, UUID> {

    /**
     * Find all chat history records for a given user, newest first.
     */
    List<ChatHistory> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
