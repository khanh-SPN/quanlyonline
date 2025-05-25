package com.example.quanlyonline.model;

public class Schedule {
    private String scheduleId;
    private ValueWrapper subject;
    private ValueWrapper date;
    private ValueWrapper time;
    private ValueWrapper teacherId;
    private ValueWrapper description;
    private ValueWrapper className;
    private ValueWrapper createdAt;
    private ValueWrapper updatedAt;

    public Schedule() {}

    // Class lồng nhau để ánh xạ các trường có dạng { "value": "..." }
    public static class ValueWrapper {
        private String value;
        private Long longValue; // Dành cho createdAt, updatedAt

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

    public String getScheduleId() { return scheduleId; }
    public void setScheduleId(String scheduleId) { this.scheduleId = scheduleId; }

    public String getSubject() { return subject != null ? subject.getValue() : null; }
    public void setSubject(String subject) {
        this.subject = new ValueWrapper();
        this.subject.setValue(subject);
    }

    public String getDate() { return date != null ? date.getValue() : null; }
    public void setDate(String date) {
        this.date = new ValueWrapper();
        this.date.setValue(date);
    }

    public String getTime() { return time != null ? time.getValue() : null; }
    public void setTime(String time) {
        this.time = new ValueWrapper();
        this.time.setValue(time);
    }

    public String getTeacherId() { return teacherId != null ? teacherId.getValue() : null; }
    public void setTeacherId(String teacherId) {
        this.teacherId = new ValueWrapper();
        this.teacherId.setValue(teacherId);
    }

    public String getDescription() { return description != null ? description.getValue() : null; }
    public void setDescription(String description) {
        this.description = new ValueWrapper();
        this.description.setValue(description);
    }

    public String getClassName() { return className != null ? className.getValue() : null; }
    public void setClassName(String className) {
        this.className = new ValueWrapper();
        this.className.setValue(className);
    }

    public long getCreatedAt() { return createdAt != null && createdAt.getLongValue() != null ? createdAt.getLongValue() : 0; }
    public void setCreatedAt(long createdAt) {
        this.createdAt = new ValueWrapper();
        this.createdAt.setLongValue(createdAt);
    }

    public long getUpdatedAt() { return updatedAt != null && updatedAt.getLongValue() != null ? updatedAt.getLongValue() : 0; }
    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = new ValueWrapper();
        this.updatedAt.setLongValue(updatedAt);
    }
}