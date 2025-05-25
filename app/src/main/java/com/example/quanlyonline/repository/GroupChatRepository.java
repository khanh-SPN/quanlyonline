package com.example.quanlyonline.repository;

import android.util.Log;
import com.example.quanlyonline.model.Message;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupChatRepository {
    private static final String TAG = "GroupChatRepository";
    private DatabaseReference messagesRef;
    private DatabaseReference usersRef;
    private DatabaseReference classesRef;

    public GroupChatRepository() {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://quanlyonline-1c06a-default-rtdb.asia-southeast1.firebasedatabase.app");
        messagesRef = database.getReference("messages");
        usersRef = database.getReference("users");
        classesRef = database.getReference("classes");
    }

    public void getMessages(String groupChatId, OnMessagesLoadedListener listener) {
        messagesRef.child(groupChatId).child("messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Message> messages = new ArrayList<>();
                for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                    Message message = new Message();
                    message.setMessageId(messageSnapshot.getKey());
                    message.setSenderId(messageSnapshot.child("sender_id").child("value").getValue(String.class));
                    message.setContent(messageSnapshot.child("content").child("value").getValue(String.class));
                    Long timestamp = messageSnapshot.child("timestamp").child("value").getValue(Long.class);
                    message.setTimestamp(timestamp != null ? timestamp : 0L);
                    messages.add(message);
                }
                listener.onSuccess(messages);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Failed to load messages: " + error.getMessage());
                listener.onFailure(error.getMessage());
            }
        });
    }

    public void sendMessage(String groupChatId, Message message, OnOperationListener listener) {
        String messageId = messagesRef.child(groupChatId).child("messages").push().getKey();
        message.setMessageId(messageId);

        Map<String, Object> messageData = new HashMap<>();
        messageData.put("sender_id", new HashMap<String, Object>() {{ put("value", message.getSenderId()); }});
        messageData.put("content", new HashMap<String, Object>() {{ put("value", message.getContent()); }});
        messageData.put("timestamp", new HashMap<String, Object>() {{ put("value", message.getTimestamp()); }});

        messagesRef.child(groupChatId).child("messages").child(messageId).setValue(messageData)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public void setupGroupChat(String groupChatId, String classId, OnOperationListener listener) {
        classesRef.child(classId).child("students").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Map<String, Boolean> participants = new HashMap<>();
                participants.put("user_001", true); // Giáo viên
                for (DataSnapshot studentSnapshot : snapshot.getChildren()) {
                    String studentId = studentSnapshot.getKey();
                    participants.put(studentId, true);
                }

                messagesRef.child(groupChatId).child("participants").setValue(participants)
                        .addOnSuccessListener(aVoid -> listener.onSuccess())
                        .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                listener.onFailure(error.getMessage());
            }
        });
    }

    public void addParticipant(String groupChatId, String userId, OnOperationListener listener) {
        Map<String, Object> participantData = new HashMap<>();
        participantData.put("value", true);

        messagesRef.child(groupChatId).child("participants").child(userId).setValue(participantData)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public void getUserName(String userId, OnUserNameLoadedListener listener) {
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String userName = snapshot.child("full_name").child("value").getValue(String.class);
                    listener.onSuccess(userName != null ? userName : userId);
                } else {
                    listener.onSuccess(userId);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Failed to load user name: " + error.getMessage());
                listener.onSuccess(userId);
            }
        });
    }

    public void getStudentsByClass(String classId, OnStudentsLoadedListener listener) {
        classesRef.child(classId).child("students").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<String> studentIds = new ArrayList<>();
                for (DataSnapshot studentSnapshot : snapshot.getChildren()) {
                    String studentId = studentSnapshot.getKey();
                    studentIds.add(studentId);
                }
                listener.onSuccess(studentIds);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Failed to load students: " + error.getMessage());
                listener.onFailure(error.getMessage());
            }
        });
    }

    public void getParticipants(String groupChatId, OnParticipantsLoadedListener listener) {
        messagesRef.child(groupChatId).child("participants").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<String> participantIds = new ArrayList<>();
                for (DataSnapshot participantSnapshot : snapshot.getChildren()) {
                    String participantId = participantSnapshot.getKey();
                    participantIds.add(participantId);
                }
                listener.onSuccess(participantIds);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Failed to load participants: " + error.getMessage());
                listener.onFailure(error.getMessage());
            }
        });
    }

    public interface OnMessagesLoadedListener {
        void onSuccess(List<Message> messages);
        void onFailure(String error);
    }

    public interface OnOperationListener {
        void onSuccess();
        void onFailure(String error);
    }

    public interface OnUserNameLoadedListener {
        void onSuccess(String userName);
    }

    public interface OnStudentsLoadedListener {
        void onSuccess(List<String> studentIds);
        void onFailure(String error);
    }

    public interface OnParticipantsLoadedListener {
        void onSuccess(List<String> participantIds);
        void onFailure(String error);
    }
}