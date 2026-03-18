package com.example.riyanov.messenger.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String type = "Bearer";
    private String username;

    public LoginResponse(String token, String username) {
        this.token = token;
        this.username = username;
    }
}
