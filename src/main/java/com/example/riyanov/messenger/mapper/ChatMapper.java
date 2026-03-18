package com.example.riyanov.messenger.mapper;

import com.example.riyanov.messenger.dto.ChatDto;
import com.example.riyanov.messenger.dto.MessageDto;
import com.example.riyanov.messenger.dto.UserDto;
import com.example.riyanov.messenger.entity.Chat;
import com.example.riyanov.messenger.entity.ChatParticipant;
import com.example.riyanov.messenger.entity.Message;
import com.example.riyanov.messenger.entity.User;

import java.util.List;
import java.util.stream.Collectors;

public class ChatMapper {

    public static UserDto toUserDto(User user) {
        if (user == null) return null;
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setOnlineStatus(user.getOnlineStatus());
        return dto;
    }

    public static MessageDto toMessageDto(Message message) {
        if (message == null) return null;
        MessageDto dto = new MessageDto();
        dto.setId(message.getId());
        dto.setChatId(message.getChat().getId());
        dto.setSender(toUserDto(message.getSender()));
        dto.setContent(message.getContent());
        dto.setCreatedAt(message.getCreatedAt());
        return dto;
    }

    public static ChatDto toChatDto(Chat chat) {
        if (chat == null) return null;
        ChatDto dto = new ChatDto();
        dto.setId(chat.getId());
        dto.setName(chat.getName());
        dto.setIsGroup(chat.getIsGroup());
        dto.setCreatedAt(chat.getCreatedAt());

        // Преобразование участников
        List<UserDto> participants = chat.getParticipants().stream()
                .map(ChatParticipant::getUser)
                .map(ChatMapper::toUserDto)
                .collect(Collectors.toList());
        dto.setParticipants(participants);

        // Последнее сообщение (можно взять последнее из списка, если есть)
        if (chat.getMessages() != null && !chat.getMessages().isEmpty()) {
            Message lastMessage = chat.getMessages().get(chat.getMessages().size() - 1);
            dto.setLastMessage(toMessageDto(lastMessage));
        }
        return dto;
    }
}