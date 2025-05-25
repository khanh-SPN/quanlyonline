package com.example.quanlyonline.service;

import com.example.quanlyonline.model.User;
import com.example.quanlyonline.repository.UserRepository;

public class AuthService {
    private UserRepository userRepository;

    public AuthService() {
        userRepository = new UserRepository();
    }

    public void login(String username, String password, UserRepository.OnLoginListener listener) {
        if (username.isEmpty() || password.isEmpty()) {
            listener.onFailure("Vui lòng nhập đầy đủ thông tin");
            return;
        }
        userRepository.login(username, password, listener);
    }
}