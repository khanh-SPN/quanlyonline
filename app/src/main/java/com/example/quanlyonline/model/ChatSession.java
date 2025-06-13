package com.example.quanlyonline.model;

import java.util.HashMap;

public class ChatSession {
    private String chatId;
    private Participants participants;
    private LastMessage last_message; // Đổi tên để khớp với Firebase
    private LastTimestamp last_timestamp; // Đổi tên để khớp với Firebase

    // Constructor mặc định cho Firebase
    public ChatSession() {}

    public ChatSession(String chatId, HashMap<String, Boolean> participantsValue, String lastMessageValue, long lastTimestampValue) {
        this.chatId = chatId;
        this.participants = new Participants("participants", participantsValue);
        this.last_message = new LastMessage("text", lastMessageValue);
        this.last_timestamp = new LastTimestamp("time", lastTimestampValue);
    }

    // Getters và Setters
    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public Participants getParticipants() {
        return participants;
    }

    public void setParticipants(Participants participants) {
        this.participants = participants;
    }

    public LastMessage getLast_message() {
        return last_message;
    }

    public void setLast_message(LastMessage last_message) {
        this.last_message = last_message;
    }

    public LastTimestamp getLast_timestamp() {
        return last_timestamp;
    }

    public void setLast_timestamp(LastTimestamp last_timestamp) {
        this.last_timestamp = last_timestamp;
    }

    // Inner classes để tách key và value
    public static class Participants {
        private String key;
        private HashMap<String, Boolean> value;

        public Participants() {}

        public Participants(String key, HashMap<String, Boolean> value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public HashMap<String, Boolean> getValue() {
            return value;
        }

        public void setValue(HashMap<String, Boolean> value) {
            this.value = value;
        }
    }

    public static class LastMessage {
        private String key;
        private String value;

        public LastMessage() {}

        public LastMessage(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public static class LastTimestamp {
        private String key;
        private long value;

        public LastTimestamp() {}

        public LastTimestamp(String key, long value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public long getValue() {
            return value;
        }

        public void setValue(long value) {
            this.value = value;
        }
    }
}