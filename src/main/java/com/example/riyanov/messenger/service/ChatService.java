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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChatParticipantRepository participantRepository;

    @Transactional
    public ChatDto createChat(CreateChatRequest request, Long creatorId) {
        // Проверка, что участников минимум 2
        if (request.getParticipantIds().size() < 2) {
            throw new IllegalArgumentException("Chat must have at least 2 participants");
        }

        // Загружаем пользователей
        List<User> participants = userRepository.findAllById(request.getParticipantIds());
        if (participants.size() != request.getParticipantIds().size()) {
            throw new IllegalArgumentException("Some users not found");
        }

        // Создаём чат
        Chat chat = new Chat();
        chat.setName(request.getName());
        chat.setIsGroup(request.getName() != null);
        chat.setCreatedAt(LocalDateTime.now());

// Добавляем участников
        for (User user : participants) {
            ChatParticipant cp = new ChatParticipant();
            cp.setChat(chat);
            cp.setUser(user);
            cp.setJoinedAt(LocalDateTime.now());
            cp.setRole(user.getId().equals(creatorId) ? "ADMIN" : "MEMBER");
            chat.getParticipants().add(cp);
        }

        chat = chatRepository.save(chat); // сохранится и чат, и все участники (благодаря каскаду)

        return ChatMapper.toChatDto(chat);
    }

    @Transactional
    public MessageDto sendMessage(SendMessageRequest request, Long senderId) {
        Chat chat = chatRepository.findById(request.getChatId())
                .orElseThrow(() -> new IllegalArgumentException("Chat not found"));
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("Sender not found"));

        // Проверка, что отправитель является участником чата
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
        // Проверка доступа
        boolean isParticipant = participantRepository.existsByChatIdAndUserId(chatId, userId);
        if (!isParticipant) {
            throw new IllegalArgumentException("User is not a participant of this chat");
        }
        return messageRepository.findByChatIdOrderByCreatedAtAsc(chatId).stream()
                .map(ChatMapper::toMessageDto)
                .collect(Collectors.toList());
    }
}