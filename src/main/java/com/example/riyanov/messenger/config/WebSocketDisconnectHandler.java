package com.example.riyanov.messenger.config;

import com.example.riyanov.messenger.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class WebSocketDisconnectHandler {

    private final UserStatusService userStatusService;
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Long userId = (Long) accessor.getSessionAttributes().get("userId");

        if (userId != null) {
            userStatusService.setOffline(userId);

            Map<String, Object> status = Map.of(
                    "userId", userId,
                    "online", false
            );

            messagingTemplate.convertAndSend("/topic/status", (Object) status);
        }
    }
}
