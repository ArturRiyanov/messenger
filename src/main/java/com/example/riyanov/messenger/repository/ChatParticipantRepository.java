package com.example.riyanov.messenger.repository;

import com.example.riyanov.messenger.entity.ChatParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {
    boolean existsByChatIdAndUserId(Long chatId, Long userId);
}