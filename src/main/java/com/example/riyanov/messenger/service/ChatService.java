package com.example.riyanov.messenger.service;

import com.example.riyanov.messenger.dto.ChatDto;
import com.example.riyanov.messenger.dto.CreateChatRequest;
import com.example.riyanov.messenger.dto.MessageDto;
import com.example.riyanov.messenger.dto.SendMessageRequest;
import com.example.riyanov.messenger.entity.Chat;
import com.example.riyanov.messenger.entity.ChatParticipant;
import com.example.riyanov.messenger.entity.Message;
import com.example.riyanov.messenger.entity.User;
import com.example.riyanov.messenger.mapper.ChatMapper;
import com.example.riyanov.messenger.repository.ChatParticipantRepository;
import com.example.riyanov.messenger.repository.ChatRepository;
import com.example.riyanov.messenger.repository.MessageRepository;
import com.example.riyanov.messenger.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChatParticipantRepository participantRepository;
    private final SimpMessagingTemplate messagingTemplate;  // <-- добавлено для WebSocket-уведомлений

    @Transactional
    public ChatDto createChat(CreateChatRequest request, Long creatorId) {
        if (request.getParticipantIds().size() < 2) {
            throw new IllegalArgumentException("Chat must have at least 2 participants");
        }

        List<User> participants = userRepository.findAllById(request.getParticipantIds());
        if (participants.size() != request.getParticipantIds().size()) {
            throw new IllegalArgumentException("Some users not found");
        }

        Chat chat = new Chat();
        chat.setName(request.getName());
        chat.setIsGroup(request.getName() != null);
        chat.setCreatedAt(LocalDateTime.now());

        for (User user : participants) {
            ChatParticipant cp = new ChatParticipant();
            cp.setChat(chat);
            cp.setUser(user);
            cp.setJoinedAt(LocalDateTime.now());
            cp.setRole(user.getId().equals(creatorId) ? "ADMIN" : "MEMBER");
            chat.getParticipants().add(cp);
        }

        chat = chatRepository.save(chat);
        return ChatMapper.toChatDto(chat);
    }

    @Transactional
    public MessageDto sendMessage(SendMessageRequest request, Long senderId) {
        Chat chat = chatRepository.findById(request.getChatId())
                .orElseThrow(() -> new IllegalArgumentException("Chat not found"));
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("Sender not found"));

        boolean isParticipant = participantRepository.existsByChatIdAndUserId(chat.getId(), senderId);
        if (!isParticipant) {
            throw new IllegalArgumentException("User is not a participant of this chat");
        }

        Message message = new Message();
        message.setChat(chat);
        message.setSender(sender);
        message.setContent(request.getContent());
        message.setCreatedAt(LocalDateTime.now());
        message = messageRepository.save(message);

        // Отправляем WebSocket-событие о создании сообщения
        sendWebSocketEvent(chat.getId(), "CREATE", ChatMapper.toMessageDto(message));

        return ChatMapper.toMessageDto(message);
    }

    @Transactional(readOnly = true)
    public List<ChatDto> getUserChats(Long userId) {
        List<Chat> chats = chatRepository.findAllByUserId(userId);
        return chats.stream()
                .map(ChatMapper::toChatDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MessageDto> getChatMessages(Long chatId, Long userId) {
        boolean isParticipant = participantRepository.existsByChatIdAndUserId(chatId, userId);
        if (!isParticipant) {
            throw new IllegalArgumentException("User is not a participant of this chat");
        }
        return messageRepository.findByChatIdOrderByCreatedAtAsc(chatId).stream()
                .map(ChatMapper::toMessageDto)
                .collect(Collectors.toList());
    }

    // ========== НОВЫЕ МЕТОДЫ ==========

    @Transactional
    public MessageDto updateMessage(Long messageId, String newContent, Long currentUserId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));

        // Проверяем, что текущий пользователь — автор сообщения
        if (!message.getSender().getId().equals(currentUserId)) {
            throw new IllegalArgumentException("You are not allowed to edit this message");
        }

        message.setContent(newContent);
        message.setUpdatedAt(LocalDateTime.now());  // обновляем дату
        Message updatedMessage = messageRepository.save(message);

        // Отправляем событие об обновлении
        sendWebSocketEvent(updatedMessage.getChat().getId(), "UPDATE", ChatMapper.toMessageDto(updatedMessage));

        return ChatMapper.toMessageDto(updatedMessage);
    }

    @Transactional
    public void deleteMessage(Long messageId, Long currentUserId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));

        if (!message.getSender().getId().equals(currentUserId)) {
            throw new IllegalArgumentException("You are not allowed to delete this message");
        }

        Long chatId = message.getChat().getId();
        messageRepository.delete(message);

        // Отправляем событие об удалении
        sendWebSocketEvent(chatId, "DELETE", messageId);
    }

    /**
     * Вспомогательный метод для отправки WebSocket-событий в топик чата.
     *
     * @param chatId  идентификатор чата
     * @param action  тип события: "CREATE", "UPDATE", "DELETE"
     * @param payload данные (для CREATE/UPDATE — DTO сообщения, для DELETE — id сообщения)
     */
    private void sendWebSocketEvent(Long chatId, String action, Object payload) {
        Map<String, Object> event = new HashMap<>();
        event.put("action", action);

        if ("CREATE".equals(action) || "UPDATE".equals(action)) {
            event.put("message", payload);  // payload — MessageDto
        } else if ("DELETE".equals(action)) {
            event.put("messageId", payload); // payload — Long messageId
            event.put("chatId", chatId);
        }

        messagingTemplate.convertAndSend("/topic/chat." + chatId, (Object) event);
    }
}