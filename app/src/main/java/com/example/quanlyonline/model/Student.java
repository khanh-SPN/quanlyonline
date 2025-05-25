package com.example.quanlyonline.model;

public class Student {
    private String userId; // Thêm userId để lưu key của node trong users
    private String studentId; // Mã sinh viên người dùng nhập
    private ValueWrapper fullName;
    private ValueWrapper className;
    private ValueWrapper dateOfBirth;
    private ValueWrapper address;
    private ValueWrapper createdAt;
    private ValueWrapper updatedAt;

    public Student() {}

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

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getFullName() { return fullName != null ? fullName.getValue() : null; }
    public void setFullName(String fullName) {
        this.fullName = new ValueWrapper();
        this.fullName.setValue(fullName);
    }

    public String getClassName() { return className != null ? className.getValue() : null; }
    public void setClassName(String className) {
        this.className = new ValueWrapper();
        this.className.setValue(className);
    }

    public String getDateOfBirth() { return dateOfBirth != null ? dateOfBirth.getValue() : null; }
    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = new ValueWrapper();
        this.dateOfBirth.setValue(dateOfBirth);
    }

    public String getAddress() { return address != null ? address.getValue() : null; }
    public void setAddress(String address) {
        this.address = new ValueWrapper();
        this.address.setValue(address);
    }

    public long getCreatedAt() {
        return createdAt != null && createdAt.getLongValue() != null ? createdAt.getLongValue() : 0;
    }
    public void setCreatedAt(Long createdAt) {
        this.createdAt = new ValueWrapper();
        this.createdAt.setLongValue(createdAt != null ? createdAt : 0L);
    }

    public long getUpdatedAt() {
        return updatedAt != null && updatedAt.getLongValue() != null ? updatedAt.getLongValue() : 0;
    }
    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = new ValueWrapper();
        this.updatedAt.setLongValue(updatedAt != null ? updatedAt : 0L);
    }
}