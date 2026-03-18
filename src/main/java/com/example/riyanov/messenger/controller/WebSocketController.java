package com.example.riyanov.messenger.controller;

import com.example.riyanov.messenger.dto.MessageDto;
import com.example.riyanov.messenger.dto.SendMessageRequest;
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
        System.out.println("Sending message, userId from session: " + userId);
        if (userId == null) {
            throw new IllegalArgumentException("User not authenticated");
        }
        MessageDto message = chatService.sendMessage(request, userId);
        messagingTemplate.convertAndSend("/topic/chat." + request.getChatId(), message);
    }
}