package com.example.riyanov.messenger.controller;

import com.example.riyanov.messenger.dto.*;
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

    // ========== НОВЫЕ ЭНДПОИНТЫ ==========

    @PostMapping("/{chatId}/messages/{messageId}/pin")
    public ResponseEntity<Void> pinMessage(@PathVariable Long chatId, @PathVariable Long messageId,
                                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        chatService.pinMessage(chatId, messageId, userDetails.getId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{chatId}/messages/{messageId}/pin")
    public ResponseEntity<Void> unpinMessage(@PathVariable Long chatId, @PathVariable Long messageId,
                                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        chatService.unpinMessage(chatId, messageId, userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{chatId}/pinned")
    public ResponseEntity<List<MessageDto>> getPinnedMessages(@PathVariable Long chatId,
                                                              @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(chatService.getPinnedMessages(chatId, userDetails.getId()));
    }

    @PostMapping("/{chatId}/messages/{messageId}/reply")
    public ResponseEntity<MessageDto> replyToMessage(@PathVariable Long chatId, @PathVariable Long messageId,
                                                     @RequestBody SendMessageRequest request,
                                                     @AuthenticationPrincipal CustomUserDetails userDetails) {
        MessageDto reply = chatService.replyToMessage(chatId, messageId, request.getContent(), userDetails.getId());
        return ResponseEntity.ok(reply);
    }

    @PostMapping("/{chatId}/messages/{messageId}/forward")
    public ResponseEntity<MessageDto> forwardMessage(@PathVariable Long chatId, @PathVariable Long messageId,
                                                     @RequestBody ForwardMessageRequest request,
                                                     @AuthenticationPrincipal CustomUserDetails userDetails) {
        MessageDto forwarded = chatService.forwardMessage(chatId, messageId, request.getTargetChatId(), userDetails.getId());
        return ResponseEntity.ok(forwarded);
    }

    @DeleteMapping("/{chatId}")
    public ResponseEntity<Void> deleteChat(@PathVariable Long chatId,
                                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        chatService.deleteChat(chatId, userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{chatId}")
    public ResponseEntity<ChatDto> updateChat(@PathVariable Long chatId,
                                              @RequestBody UpdateChatRequest request,
                                              @AuthenticationPrincipal CustomUserDetails userDetails) {
        ChatDto updated = chatService.updateChat(chatId, request, userDetails.getId());
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{chatId}/invite")
    public ResponseEntity<Void> inviteToChat(@PathVariable Long chatId,
                                             @RequestBody InviteRequest request,
                                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        chatService.inviteToChat(chatId, request.getUserId(), userDetails.getId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{chatId}/messages")
    public ResponseEntity<Void> clearChatHistory(@PathVariable Long chatId,
                                                 @AuthenticationPrincipal CustomUserDetails userDetails) {
        chatService.clearChatHistory(chatId, userDetails.getId());
        return ResponseEntity.noContent().build();
    }
}