package com.example.quanlyonline.model;

public class Message {
    private String messageId;
    private ValueWrapper senderId;
    private ValueWrapper content;
    private ValueWrapper timestamp;

    public Message() {}

    // Class lồng nhau để ánh xạ các trường có dạng { "value": "..." }
    public static class ValueWrapper {
        private String value;
        private Long longValue; // Dành cho timestamp

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public Long getLongValue() {
            return longValue;
        }

        public void setLongValue(Long longValue) {
            this.longValue = longValue;
        }
    }

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getSenderId() { return senderId != null ? senderId.getValue() : null; }
    public void setSenderId(String senderId) {
        this.senderId = new ValueWrapper();
        this.senderId.setValue(senderId);
    }

    public String getContent() { return content != null ? content.getValue() : null; }
    public void setContent(String content) {
        this.content = new ValueWrapper();
        this.content.setValue(content);
    }

    public long getTimestamp() { return timestamp != null && timestamp.getLongValue() != null ? timestamp.getLongValue() : 0; }
    public void setTimestamp(long timestamp) {
        this.timestamp = new ValueWrapper();
        this.timestamp.setLongValue(timestamp);
    }
}