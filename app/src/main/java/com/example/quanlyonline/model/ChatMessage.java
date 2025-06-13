package com.example.quanlyonline.model;

public class ChatMessage {
    private String messageId;
    private SenderId sender_id; // Đổi tên để khớp với Firebase
    private MessageText message_text; // Đổi tên để khớp với Firebase
    private Timestamp timestamp;

    // Constructor mặc định cho Firebase
    public ChatMessage() {}

    public ChatMessage(String senderIdValue, String messageTextValue, long timestampValue) {
        this.sender_id = new SenderId("id", senderIdValue);
        this.message_text = new MessageText("text", messageTextValue);
        this.timestamp = new Timestamp("time", timestampValue);
    }

    // Getters và Setters
    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public SenderId getSender_id() {
        return sender_id;
    }

    public void setSender_id(SenderId sender_id) {
        this.sender_id = sender_id;
    }

    public MessageText getMessage_text() {
        return message_text;
    }

    public void setMessage_text(MessageText message_text) {
        this.message_text = message_text;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    // Inner classes để tách key và value
    public static class SenderId {
        private String key;
        private String value;

        public SenderId() {}

        public SenderId(String key, String value) {
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

    public static class MessageText {
        private String key;
        private String value;

        public MessageText() {}

        public MessageText(String key, String value) {
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

    public static class Timestamp {
        private String key;
        private long value;

        public Timestamp() {}

        public Timestamp(String key, long value) {
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