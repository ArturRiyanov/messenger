package com.example.riyanov.messenger.repository;

import com.example.riyanov.messenger.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    // Найти все чаты, в которых участвует пользователь
    @Query("SELECT c FROM Chat c JOIN c.participants p WHERE p.user.id = :userId")
    List<Chat> findAllByUserId(@Param("userId") Long userId);
    @Query("SELECT c FROM Chat c JOIN c.participants p1 JOIN c.participants p2 " +
            "WHERE c.isGroup = false AND p1.user.id = :userId1 AND p2.user.id = :userId2 AND " +
            "SIZE(c.participants) = 2")
    Optional<Chat> findPrivateChatBetween(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
}