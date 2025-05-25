package com.example.quanlyonline.controller;

import com.example.quanlyonline.model.Notification;
import com.example.quanlyonline.repository.NotificationRepository;
import com.example.quanlyonline.service.NotificationService;

import java.util.List;

public class NotificationController {
    private NotificationService notificationService;

    public NotificationController() {
        notificationService = new NotificationService();
    }

    public void getNotificationsByReceiver(String receiverId, OnNotificationsResultListener listener) {
        notificationService.getNotificationsByReceiver(receiverId, new NotificationRepository.OnNotificationsLoadedListener() {
            @Override
            public void onSuccess(List<Notification> notifications) {
                listener.onSuccess(notifications);
            }

            @Override
            public void onFailure(String error) {
                listener.onFailure(error);
            }
        });
    }

    public void sendNotification(String receiverId, Notification notification, OnOperationResultListener listener) {
        notificationService.sendNotification(receiverId, notification, new NotificationRepository.OnOperationListener() {
            @Override
            public void onSuccess() {
                listener.onSuccess();
            }

            @Override
            public void onFailure(String error) {
                listener.onFailure(error);
            }
        });
    }

    public interface OnNotificationsResultListener {
        void onSuccess(List<Notification> notifications);
        void onFailure(String error);
    }

    public interface OnOperationResultListener {
        void onSuccess();
        void onFailure(String error);
    }
}