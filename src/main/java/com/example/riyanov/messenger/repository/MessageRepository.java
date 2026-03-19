package com.example.riyanov.messenger.repository;

import com.example.riyanov.messenger.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByChatIdOrderByCreatedAtAsc(Long chatId);
    void deleteByChatId(Long chatId);
    List<Message> findByChatIdAndPinnedTrue(Long chatId); // добавить
}