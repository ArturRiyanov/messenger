package com.example.riyanov.messenger.controller;

import com.example.riyanov.messenger.dto.ChatDto;
import com.example.riyanov.messenger.dto.CreateChatRequest;
import com.example.riyanov.messenger.dto.MessageDto;
import com.example.riyanov.messenger.dto.SendMessageRequest;
import com.example.riyanov.messenger.dto.UpdateMessageRequest;  // нужно создать
import com.example.riyanov.messenger.security.CustomUserDetails;
import com.example.riyanov.messenger.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<ChatDto> createChat(@RequestBody CreateChatRequest request,
                                              @AuthenticationPrincipal CustomUserDetails userDetails) {
        ChatDto chat = chatService.createChat(request, userDetails.getId());
        return ResponseEntity.ok(chat);
    }

    @GetMapping
    public ResponseEntity<List<ChatDto>> getUserChats(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(chatService.getUserChats(userDetails.getId()));
    }

    @PostMapping("/messages")
    public ResponseEntity<MessageDto> sendMessage(@RequestBody SendMessageRequest request,
                                                  @AuthenticationPrincipal CustomUserDetails userDetails) {
        MessageDto message = chatService.sendMessage(request, userDetails.getId());
        return ResponseEntity.ok(message);
    }

    @GetMapping("/{chatId}/messages")
    public ResponseEntity<List<MessageDto>> getChatMessages(@PathVariable Long chatId,
                                                            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(chatService.getChatMessages(chatId, userDetails.getId()));
    }

    // ========== НОВЫЕ ЭНДПОИНТЫ ==========

    @PutMapping("/messages/{messageId}")
    public ResponseEntity<MessageDto> updateMessage(@PathVariable Long messageId,
                                                    @RequestBody UpdateMessageRequest request,
                                                    @AuthenticationPrincipal CustomUserDetails userDetails) {
        MessageDto updated = chatService.updateMessage(messageId, request.getContent(), userDetails.getId());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long messageId,
                                              @AuthenticationPrincipal CustomUserDetails userDetails) {
        chatService.deleteMessage(messageId, userDetails.getId());
        return ResponseEntity.noContent().build();
    }
}