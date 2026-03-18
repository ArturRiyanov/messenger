package com.example.riyanov.messenger.dto;

import lombok.Data;

@Data
public class SendMessageRequest {
    private Long chatId;
    private String content;
}