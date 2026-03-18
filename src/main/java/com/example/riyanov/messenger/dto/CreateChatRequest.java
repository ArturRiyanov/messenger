package com.example.riyanov.messenger.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateChatRequest {
    private String name; // если null, то это личный чат
    private List<Long> participantIds; // минимум 2 участника
}