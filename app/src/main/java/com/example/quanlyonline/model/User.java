package com.example.quanlyonline.model;

import java.util.HashMap;
import java.util.Map;

public class User {
    private String userId;
    private ValueWrapper username;
    private ValueWrapper password;
    private ValueWrapper role;
    private ValueWrapper fullName;
    private ValueWrapper phone;
    private ValueWrapper email;
    private ValueWrapper createdAt;
    private ValueWrapper updatedAt;
    private ValueWrapper classManaged;
    private ValueWrapper classId;
    private ValueWrapper childId;
    private ValueWrapper studentId;
    private double averageScore;
    private Map<String, Double> subjects;

    public User() {
        this.subjects = new HashMap<>();
    }

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

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username != null ? username.getValue() : null; }
    public void setUsername(String username) {
        this.username = new ValueWrapper();
        this.username.setValue(username);
    }

    public String getPassword() { return password != null ? password.getValue() : null; }
    public void setPassword(String password) {
        this.password = new ValueWrapper();
        this.password.setValue(password);
    }

    public String getRole() { return role != null ? role.getValue() : null; }
    public void setRole(String role) {
        this.role = new ValueWrapper();
        this.role.setValue(role);
    }

    public String getFullName() { return fullName != null ? fullName.getValue() : null; }
    public void setFullName(String fullName) {
        this.fullName = new ValueWrapper();
        this.fullName.setValue(fullName);
    }

    public String getPhone() { return phone != null ? phone.getValue() : null; }
    public void setPhone(String phone) {
        this.phone = new ValueWrapper();
        this.phone.setValue(phone);
    }

    public String getEmail() { return email != null ? email.getValue() : null; }
    public void setEmail(String email) {
        this.email = new ValueWrapper();
        this.email.setValue(email);
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

    public String getClassManaged() { return classManaged != null ? classManaged.getValue() : null; }
    public void setClassManaged(String classManaged) {
        this.classManaged = new ValueWrapper();
        this.classManaged.setValue(classManaged);
    }

    public String getClassId() { return classId != null ? classId.getValue() : null; }
    public void setClassId(String classId) {
        this.classId = new ValueWrapper();
        this.classId.setValue(classId);
    }

    public String getChildId() { return childId != null ? childId.getValue() : null; }
    public void setChildId(String childId) {
        this.childId = new ValueWrapper();
        this.childId.setValue(childId);
    }

    public String getStudentId() { return studentId != null ? studentId.getValue() : null; }
    public void setStudentId(String studentId) {
        this.studentId = new ValueWrapper();
        this.studentId.setValue(studentId);
    }

    public double getAverageScore() { return averageScore; }
    public void setAverageScore(double averageScore) { this.averageScore = averageScore; }

    public Map<String, Double> getSubjects() { return subjects; }
    public void setSubjects(Map<String, Double> subjects) { this.subjects = subjects; }
}