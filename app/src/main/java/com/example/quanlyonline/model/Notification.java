package com.example.quanlyonline.model;

public class Notification {
    private String notificationId;
    private ValueWrapper senderId;
    private ValueWrapper receiverId;
    private ValueWrapper title;
    private ValueWrapper content;
    private ValueWrapper timestamp;
    private ValueWrapper read;

    public Notification() {}

    // Class lồng nhau để ánh xạ các trường có dạng { "value": "..." }
    public static class ValueWrapper {
        private String value;
        private Long longValue; // Dành cho timestamp
        private Boolean booleanValue; // Dành cho read

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

        public Boolean getBooleanValue() {
            return booleanValue;
        }

        public void setBooleanValue(Boolean booleanValue) {
            this.booleanValue = booleanValue;
        }
    }

    public String getNotificationId() { return notificationId; }
    public void setNotificationId(String notificationId) { this.notificationId = notificationId; }

    public String getSenderId() { return senderId != null ? senderId.getValue() : null; }
    public void setSenderId(String senderId) {
        this.senderId = new ValueWrapper();
        this.senderId.setValue(senderId);
    }

    public String getReceiverId() { return receiverId != null ? receiverId.getValue() : null; }
    public void setReceiverId(String receiverId) {
        this.receiverId = new ValueWrapper();
        this.receiverId.setValue(receiverId);
    }

    public String getTitle() { return title != null ? title.getValue() : null; }
    public void setTitle(String title) {
        this.title = new ValueWrapper();
        this.title.setValue(title);
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

    public boolean isRead() { return read != null && read.getBooleanValue() != null ? read.getBooleanValue() : false; }
    public void setRead(boolean read) {
        this.read = new ValueWrapper();
        this.read.setBooleanValue(read);
    }
}