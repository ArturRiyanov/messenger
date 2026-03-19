package com.example.riyanov.messenger.controller;

import com.example.riyanov.messenger.dto.ReadReceipt;
import com.example.riyanov.messenger.dto.SendMessageRequest;
import com.example.riyanov.messenger.dto.TypingNotification;
import com.example.riyanov.messenger.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload SendMessageRequest request,
                            SimpMessageHeaderAccessor headerAccessor) {
        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
        if (userId == null) throw new IllegalArgumentException("Not authenticated");
        chatService.sendMessage(request, userId);
    }

    @MessageMapping("/chat.typing")
    public void typingIndicator(@Payload TypingNotification notification,
                                SimpMessageHeaderAccessor headerAccessor) {
        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
        String username = (String) headerAccessor.getSessionAttributes().get("username");

        if (userId == null) return;

        notification.setUserId(userId);
        notification.setUsername(username);
        // НЕ затираем typing, оставляем как есть

        messagingTemplate.convertAndSend(
                "/topic/chat." + notification.getChatId() + ".typing",
                notification
        );
    }


    @MessageMapping("/chat.read")
    public void readReceipt(@Payload ReadReceipt receipt,
                            SimpMessageHeaderAccessor headerAccessor) {
        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
        if (userId == null) return;
        receipt.setUserId(userId);
        // Здесь можно сохранить в БД lastReadMessageId (опционально)
        messagingTemplate.convertAndSend("/topic/chat." + receipt.getChatId() + ".read", receipt);
    }
}