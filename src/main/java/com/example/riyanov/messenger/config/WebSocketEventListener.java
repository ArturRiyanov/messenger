package com.example.riyanov.messenger.config;

import com.example.riyanov.messenger.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        // Извлекаем исходное сообщение CONNECT
        Object connectMessageObj = sha.getHeader("simpConnectMessage");
        if (connectMessageObj instanceof Message) {
            StompHeaderAccessor connectSha = StompHeaderAccessor.wrap((Message<?>) connectMessageObj);
            Map<String, Object> sessionAttributes = connectSha.getSessionAttributes();
            if (sessionAttributes == null) {
                // Fallback: пробуем заголовок simpSessionAttributes
                Object attr = connectSha.getHeader("simpSessionAttributes");
                if (attr instanceof Map) {
                    sessionAttributes = (Map<String, Object>) attr;
                }
            }
            Long userId = sessionAttributes != null ? (Long) sessionAttributes.get("userId") : null;
            if (userId != null) {
                userRepository.findById(userId).ifPresent(user -> {
                    user.setOnlineStatus(true);
                    userRepository.save(user);
                    broadcastUserStatus(userId, true);
                });
            }
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        Map<String, Object> sessionAttributes = sha.getSessionAttributes();
        if (sessionAttributes == null) {
            Object attr = sha.getHeader("simpSessionAttributes");
            if (attr instanceof Map) {
                sessionAttributes = (Map<String, Object>) attr;
            }
        }
        Long userId = sessionAttributes != null ? (Long) sessionAttributes.get("userId") : null;
        if (userId != null) {
            userRepository.findById(userId).ifPresent(user -> {
                user.setOnlineStatus(false);
                userRepository.save(user);
                broadcastUserStatus(userId, false);
            });
        }
    }

    private void broadcastUserStatus(Long userId, boolean online) {
        Map<String, Object> statusUpdate = new HashMap<>();
        statusUpdate.put("userId", userId);
        statusUpdate.put("online", online);
        // Явное приведение для устранения неоднозначности вызова
        messagingTemplate.convertAndSend("/topic/user.status", (Object) statusUpdate);
    }
}