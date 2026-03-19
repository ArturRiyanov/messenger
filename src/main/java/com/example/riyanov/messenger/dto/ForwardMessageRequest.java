package com.example.riyanov.messenger.dto;

import lombok.Data;

@Data
public class ForwardMessageRequest {
    private Long targetChatId;
}
