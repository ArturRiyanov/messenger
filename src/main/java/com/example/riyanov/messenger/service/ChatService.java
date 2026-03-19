package com.example.riyanov.messenger.service;

import com.example.riyanov.messenger.dto.*;
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
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final ChatParticipantRepository participantRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // Создание чата (личного или группового)
    @Transactional
    public ChatDto createChat(CreateChatRequest request, Long creatorId) {
        // Проверка, что участников минимум 2
        if (request.getParticipantIds() == null || request.getParticipantIds().size() < 2) {
            throw new IllegalArgumentException("Чат должен содержать минимум 2 участников");
        }

        // Для личного чата (name == null) проверим, не существует ли уже такой чат
        if (request.getName() == null && request.getParticipantIds().size() == 2) {
            Long user1 = request.getParticipantIds().get(0);
            Long user2 = request.getParticipantIds().get(1);
            // Порядок не важен, ищем любой private чат между ними
            var existing = chatRepository.findPrivateChatBetween(user1, user2);
            if (existing.isPresent()) {
                return ChatMapper.toChatDto(existing.get());
            }
        }

        // Создаём чат
        Chat chat = new Chat();
        chat.setName(request.getName());
        chat.setIsGroup(request.getName() != null); // если есть имя – группа
        chat.setCreatedAt(LocalDateTime.now());
        chat = chatRepository.save(chat);

        // Добавляем участников
        for (Long userId : request.getParticipantIds()) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + userId));
            ChatParticipant participant = new ChatParticipant();
            participant.setChat(chat);
            participant.setUser(user);
            participant.setJoinedAt(LocalDateTime.now());
            participant.setRole("MEMBER");
            participantRepository.save(participant);
        }

        return ChatMapper.toChatDto(chat);
    }

    // Получить все чаты пользователя
    @Transactional(readOnly = true)
    public List<ChatDto> getUserChats(Long userId) {
        List<Chat> chats = chatRepository.findAllByUserId(userId);
        return chats.stream().map(ChatMapper::toChatDto).collect(Collectors.toList());
    }

    // Отправить сообщение
    @Transactional
    public MessageDto sendMessage(SendMessageRequest request, Long senderId) {
        Chat chat = chatRepository.findById(request.getChatId())
                .orElseThrow(() -> new IllegalArgumentException("Чат не найден"));
        // Проверяем, что отправитель – участник чата
        if (!participantRepository.existsByChatIdAndUserId(chat.getId(), senderId)) {
            throw new SecurityException("Вы не являетесь участником этого чата");
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("Отправитель не найден"));

        Message message = new Message();
        message.setChat(chat);
        message.setSender(sender);
        message.setContent(request.getContent());
        message.setCreatedAt(LocalDateTime.now());
        message = messageRepository.save(message);

        MessageDto messageDto = ChatMapper.toMessageDto(message);

        // Отправляем сообщение через WebSocket всем участникам чата
        messagingTemplate.convertAndSend("/topic/chat." + chat.getId(),
                new WebSocketMessage("CREATE", messageDto));

        return messageDto;
    }

    // Получить сообщения чата
    @Transactional(readOnly = true)
    public List<MessageDto> getChatMessages(Long chatId, Long userId) {
        // Проверяем доступ
        if (!participantRepository.existsByChatIdAndUserId(chatId, userId)) {
            throw new SecurityException("У вас нет доступа к этому чату");
        }
        List<Message> messages = messageRepository.findByChatIdOrderByCreatedAtAsc(chatId);
        return messages.stream().map(ChatMapper::toMessageDto).collect(Collectors.toList());
    }

    // Обновить сообщение
    @Transactional
    public MessageDto updateMessage(Long messageId, String newContent, Long userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Сообщение не найдено"));
        // Только автор может редактировать
        if (!message.getSender().getId().equals(userId)) {
            throw new SecurityException("Вы можете редактировать только свои сообщения");
        }
        message.setContent(newContent);
        message.setUpdatedAt(LocalDateTime.now());
        message = messageRepository.save(message);

        MessageDto dto = ChatMapper.toMessageDto(message);
        messagingTemplate.convertAndSend("/topic/chat." + message.getChat().getId(),
                new WebSocketMessage("UPDATE", dto));
        return dto;
    }

    // Удалить сообщение
    @Transactional
    public void deleteMessage(Long messageId, Long userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Сообщение не найдено"));
        if (!message.getSender().getId().equals(userId)) {
            throw new SecurityException("Вы можете удалять только свои сообщения");
        }
        Long chatId = message.getChat().getId();
        messageRepository.delete(message);
        messagingTemplate.convertAndSend("/topic/chat." + chatId,
                new WebSocketMessage("DELETE", messageId));
    }

    // Вспомогательный класс для WebSocket-уведомлений
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class WebSocketMessage {
        private String action; // "CREATE", "UPDATE", "DELETE"
        private Object payload;
    }


    // ========== НОВЫЕ МЕТОДЫ ==========

    @Transactional
    public void pinMessage(Long chatId, Long messageId, Long userId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Чат не найден"));
        if (!participantRepository.existsByChatIdAndUserId(chatId, userId)) {
            throw new SecurityException("Вы не участник чата");
        }
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Сообщение не найдено"));
        if (!message.getChat().getId().equals(chatId)) {
            throw new IllegalArgumentException("Сообщение не принадлежит чату");
        }
        message.setPinned(true);
        messageRepository.save(message);
        // Можно также обновить pinnedMessage в чате, если нужно хранить только одно
        chat.setPinnedMessage(message);
        chatRepository.save(chat);

        // Уведомление через WebSocket
        messagingTemplate.convertAndSend("/topic/chat." + chatId,
                new WebSocketMessage("PIN", messageId));
    }

    @Transactional
    public void unpinMessage(Long chatId, Long messageId, Long userId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Чат не найден"));
        if (!participantRepository.existsByChatIdAndUserId(chatId, userId)) {
            throw new SecurityException("Вы не участник чата");
        }
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Сообщение не найдено"));
        if (!message.getChat().getId().equals(chatId)) {
            throw new IllegalArgumentException("Сообщение не принадлежит чату");
        }
        message.setPinned(false);
        messageRepository.save(message);
        // Если это было закреплённое сообщение чата, убираем его
        if (chat.getPinnedMessage() != null && chat.getPinnedMessage().getId().equals(messageId)) {
            chat.setPinnedMessage(null);
            chatRepository.save(chat);
        }
        messagingTemplate.convertAndSend("/topic/chat." + chatId,
                new WebSocketMessage("UNPIN", messageId));
    }

    // ========== МЕТОДЫ ДЛЯ РАБОТЫ С ЗАКРЕПЛЁННЫМИ СООБЩЕНИЯМИ ==========

    @Transactional(readOnly = true)
    public List<MessageDto> getPinnedMessages(Long chatId, Long userId) {
        // Проверяем доступ к чату
        if (!participantRepository.existsByChatIdAndUserId(chatId, userId)) {
            throw new SecurityException("У вас нет доступа к этому чату");
        }
        List<Message> pinned = messageRepository.findByChatIdAndPinnedTrue(chatId);
        return pinned.stream().map(ChatMapper::toMessageDto).collect(Collectors.toList());
    }

    @Transactional
    public MessageDto replyToMessage(Long chatId, Long parentMessageId, String content, Long userId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Чат не найден"));
        if (!participantRepository.existsByChatIdAndUserId(chatId, userId)) {
            throw new SecurityException("Вы не участник чата");
        }
        User sender = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        Message parent = messageRepository.findById(parentMessageId)
                .orElseThrow(() -> new IllegalArgumentException("Родительское сообщение не найдено"));
        if (!parent.getChat().getId().equals(chatId)) {
            throw new IllegalArgumentException("Родительское сообщение не из этого чата");
        }

        Message reply = new Message();
        reply.setChat(chat);
        reply.setSender(sender);
        reply.setContent(content);
        reply.setParentMessage(parent);
        reply.setType(Message.MessageType.REPLY);
        reply.setCreatedAt(LocalDateTime.now());
        reply = messageRepository.save(reply);

        MessageDto dto = ChatMapper.toMessageDto(reply);
        messagingTemplate.convertAndSend("/topic/chat." + chatId,
                new WebSocketMessage("CREATE", dto));
        return dto;
    }

    @Transactional
    public MessageDto forwardMessage(Long sourceChatId, Long messageId, Long targetChatId, Long userId) {
        // Проверяем, что пользователь участник исходного чата
        if (!participantRepository.existsByChatIdAndUserId(sourceChatId, userId)) {
            throw new SecurityException("Вы не участник исходного чата");
        }
        // Проверяем, что пользователь участник целевого чата
        if (!participantRepository.existsByChatIdAndUserId(targetChatId, userId)) {
            throw new SecurityException("Вы не участник целевого чата");
        }

        Message original = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Сообщение не найдено"));
        if (!original.getChat().getId().equals(sourceChatId)) {
            throw new IllegalArgumentException("Сообщение не из исходного чата");
        }

        Chat targetChat = chatRepository.findById(targetChatId)
                .orElseThrow(() -> new IllegalArgumentException("Целевой чат не найден"));
        User sender = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        Message forward = new Message();
        forward.setChat(targetChat);
        forward.setSender(sender);
        forward.setContent("(Переслано) " + original.getContent()); // можно добавить информацию об отправителе
        forward.setParentMessage(original);
        forward.setType(Message.MessageType.FORWARD);
        forward.setCreatedAt(LocalDateTime.now());
        forward = messageRepository.save(forward);

        MessageDto dto = ChatMapper.toMessageDto(forward);
        messagingTemplate.convertAndSend("/topic/chat." + targetChatId,
                new WebSocketMessage("CREATE", dto));
        return dto;
    }

    @Transactional
    public void deleteChat(Long chatId, Long userId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Чат не найден"));
        // Проверим права: может удалять только создатель или администратор? Для простоты разрешим любому участнику.
        if (!participantRepository.existsByChatIdAndUserId(chatId, userId)) {
            throw new SecurityException("Вы не участник чата");
        }
        // Удаляем все сообщения, участников, сам чат (каскадно)
        chatRepository.delete(chat);
    }

    @Transactional
    public ChatDto updateChat(Long chatId, UpdateChatRequest request, Long userId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Чат не найден"));
        if (!participantRepository.existsByChatIdAndUserId(chatId, userId)) {
            throw new SecurityException("Вы не участник чата");
        }
        // Можно проверить, что пользователь администратор, если нужно
        if (request.getName() != null) {
            chat.setName(request.getName());
        }
        chat = chatRepository.save(chat);
        return ChatMapper.toChatDto(chat);
    }

    @Transactional
    public void inviteToChat(Long chatId, Long newUserId, Long inviterId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Чат не найден"));
        if (!participantRepository.existsByChatIdAndUserId(chatId, inviterId)) {
            throw new SecurityException("Вы не участник чата");
        }
        if (participantRepository.existsByChatIdAndUserId(chatId, newUserId)) {
            throw new IllegalArgumentException("Пользователь уже в чате");
        }
        User newUser = userRepository.findById(newUserId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        ChatParticipant participant = new ChatParticipant();
        participant.setChat(chat);
        participant.setUser(newUser);
        participant.setJoinedAt(LocalDateTime.now());
        participant.setRole("MEMBER");
        participantRepository.save(participant);

        // Уведомление об изменении состава чата (можно отправить участникам)
    }

    @Transactional
    public void clearChatHistory(Long chatId, Long userId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Чат не найден"));
        if (!participantRepository.existsByChatIdAndUserId(chatId, userId)) {
            throw new SecurityException("Вы не участник чата");
        }
        // Удаляем все сообщения чата
        messageRepository.deleteByChatId(chatId); // нужно добавить метод в MessageRepository
    }
}