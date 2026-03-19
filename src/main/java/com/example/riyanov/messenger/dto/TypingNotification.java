package com.example.riyanov.messenger.dto;

import lombok.Data;

@Data
public class TypingNotification {
    private Long chatId;
    private Long userId;
    private String username;
    private boolean typing; // true – начал печатать, false – закончил
}