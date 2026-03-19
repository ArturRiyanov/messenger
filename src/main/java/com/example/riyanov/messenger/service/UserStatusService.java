package com.example.riyanov.messenger.service;

import com.example.riyanov.messenger.entity.User;
import com.example.riyanov.messenger.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserStatusService {

    private final UserRepository userRepository;

    @Transactional
    public void setOnline(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setOnlineStatus(true);
            user.setLastOnline(LocalDateTime.now());
            userRepository.save(user);
        });
    }

    @Transactional
    public void setOffline(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setOnlineStatus(false);
            user.setLastOnline(LocalDateTime.now());
            userRepository.save(user);
        });
    }
}