package com.example.quanlyonline.service;

import com.example.quanlyonline.model.Notification;
import com.example.quanlyonline.repository.NotificationRepository;

import java.util.List;

public class NotificationService {
    private NotificationRepository notificationRepository;

    public NotificationService() {
        notificationRepository = new NotificationRepository();
    }

    public void getNotificationsByReceiver(String receiverId, NotificationRepository.OnNotificationsLoadedListener listener) {
        notificationRepository.getNotificationsByReceiver(receiverId, listener);
    }

    public void sendNotification(String receiverId, Notification notification, NotificationRepository.OnOperationListener listener) {
        if (notification.getTitle().isEmpty() || notification.getContent().isEmpty()) {
            listener.onFailure("Vui lòng nhập đầy đủ tiêu đề và nội dung");
            return;
        }
        notificationRepository.sendNotification(receiverId, notification, listener);
    }
}