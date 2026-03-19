package com.example.riyanov.messenger.dto;

import lombok.Data;

@Data
public class ReadReceipt {
    private Long chatId;
    private Long userId;
    private Long lastReadMessageId;
}