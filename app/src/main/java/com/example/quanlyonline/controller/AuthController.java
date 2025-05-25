package com.example.quanlyonline.controller;

import com.example.quanlyonline.model.User;
import com.example.quanlyonline.service.AuthService;
import com.example.quanlyonline.repository.UserRepository;

public class AuthController {
    private AuthService authService;

    public AuthController() {
        authService = new AuthService();
    }

    public void login(String username, String password, OnLoginResultListener listener) {
        authService.login(username, password, new UserRepository.OnLoginListener() {
            @Override
            public void onSuccess(User user) {
                listener.onSuccess(user);
            }

            @Override
            public void onFailure(String error) {
                listener.onFailure(error);
            }
        });
    }

    public interface OnLoginResultListener {
        void onSuccess(User user);
        void onFailure(String error);
    }
}