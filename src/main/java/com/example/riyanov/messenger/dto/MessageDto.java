package com.example.riyanov.messenger.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageDto {
    private Long id;
    private Long chatId;
    private UserDto sender;
    private String content;
    private LocalDateTime createdAt;
}