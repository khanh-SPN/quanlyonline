package com.example.quanlyonline.repository;

import android.util.Log;
import com.example.quanlyonline.model.Notification;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationRepository {
    private static final String TAG = "NotificationRepository";
    private DatabaseReference notificationsRef;

    public NotificationRepository() {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://quanlyonline-1c06a-default-rtdb.asia-southeast1.firebasedatabase.app");
        notificationsRef = database.getReference("notifications");
    }

    public void getNotificationsByReceiver(String receiverId, OnNotificationsLoadedListener listener) {
        notificationsRef.child(receiverId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Notification> notifications = new ArrayList<>();
                for (DataSnapshot notificationSnapshot : snapshot.getChildren()) {
                    Notification notification = new Notification();
                    notification.setNotificationId(notificationSnapshot.getKey());
                    notification.setSenderId(notificationSnapshot.child("sender_id").child("value").getValue(String.class));
                    notification.setReceiverId(notificationSnapshot.child("receiver_id").child("value").getValue(String.class));
                    notification.setTitle(notificationSnapshot.child("title").child("value").getValue(String.class));
                    notification.setContent(notificationSnapshot.child("content").child("value").getValue(String.class));
                    notification.setTimestamp(notificationSnapshot.child("timestamp").child("value").getValue(Long.class));
                    notification.setRead(notificationSnapshot.child("read").child("value").getValue(Boolean.class));
                    notifications.add(notification);
                }
                listener.onSuccess(notifications);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Failed to load notifications: " + error.getMessage());
                listener.onFailure(error.getMessage());
            }
        });
    }

    public void sendNotification(String receiverId, Notification notification, OnOperationListener listener) {
        String notificationId = notificationsRef.child(receiverId).push().getKey();
        notification.setNotificationId(notificationId);

        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("sender_id", new HashMap<String, Object>() {{ put("value", notification.getSenderId()); }});
        notificationData.put("receiver_id", new HashMap<String, Object>() {{ put("value", receiverId); }});
        notificationData.put("title", new HashMap<String, Object>() {{ put("value", notification.getTitle()); }});
        notificationData.put("content", new HashMap<String, Object>() {{ put("value", notification.getContent()); }});
        notificationData.put("timestamp", new HashMap<String, Object>() {{ put("value", notification.getTimestamp()); }});
        notificationData.put("read", new HashMap<String, Object>() {{ put("value", notification.isRead()); }});

        notificationsRef.child(receiverId).child(notificationId).setValue(notificationData)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public interface OnNotificationsLoadedListener {
        void onSuccess(List<Notification> notifications);
        void onFailure(String error);
    }

    public interface OnOperationListener {
        void onSuccess();
        void onFailure(String error);
    }
}