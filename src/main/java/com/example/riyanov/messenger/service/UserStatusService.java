package com.example.riyanov.messenger.service;

import com.example.riyanov.messenger.entity.User;
import com.example.riyanov.messenger.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserStatusService {

    private final UserRepository userRepository;

    public void setOnline(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setOnlineStatus(true);
            user.setLastOnline(LocalDateTime.now());
            userRepository.save(user);
        });
    }

    public void setOffline(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setOnlineStatus(false);
            user.setLastOnline(LocalDateTime.now());
            userRepository.save(user);
        });
    }
}
