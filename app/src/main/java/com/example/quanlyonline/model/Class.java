package com.example.quanlyonline.model;

import java.util.HashMap;
import java.util.Map;

public class Class {
    private String classId;
    private ValueWrapper name;
    private ValueWrapper teacherId;
    private ValueWrapper studentCount;
    private Map<String, ValueWrapper> students;
    private Map<String, ValueWrapper> subjects;

    public Class() {
        this.students = new HashMap<>();
        this.subjects = new HashMap<>();
    }

    // Class lồng nhau để ánh xạ các trường có dạng { "value": "..." }
    public static class ValueWrapper {
        private String value;
        private Integer intValue; // Dành cho studentCount

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public Integer getIntValue() {
            return intValue;
        }

        public void setIntValue(Integer intValue) {
            this.intValue = intValue;
        }
    }

    // Getters và setters
    public String getClassId() { return classId; }
    public void setClassId(String classId) { this.classId = classId; }

    public String getName() { return name != null ? name.getValue() : null; }
    public void setName(String name) {
        this.name = new ValueWrapper();
        this.name.setValue(name);
    }

    public String getTeacherId() { return teacherId != null ? teacherId.getValue() : null; }
    public void setTeacherId(String teacherId) {
        this.teacherId = new ValueWrapper();
        this.teacherId.setValue(teacherId);
    }

    public int getStudentCount() { return studentCount != null && studentCount.getIntValue() != null ? studentCount.getIntValue() : 0; }
    public void setStudentCount(int studentCount) {
        this.studentCount = new ValueWrapper();
        this.studentCount.setIntValue(studentCount);
    }

    public Map<String, Boolean> getStudents() {
        Map<String, Boolean> result = new HashMap<>();
        if (students != null) {
            for (Map.Entry<String, ValueWrapper> entry : students.entrySet()) {
                result.put(entry.getKey(), entry.getValue().getValue() != null && Boolean.parseBoolean(entry.getValue().getValue()));
            }
        }
        return result;
    }

    public void setStudents(Map<String, Boolean> students) {
        this.students = new HashMap<>();
        if (students != null) {
            for (Map.Entry<String, Boolean> entry : students.entrySet()) {
                ValueWrapper wrapper = new ValueWrapper();
                wrapper.setValue(String.valueOf(entry.getValue()));
                this.students.put(entry.getKey(), wrapper);
            }
        }
    }

    public Map<String, Boolean> getSubjects() {
        Map<String, Boolean> result = new HashMap<>();
        if (subjects != null) {
            for (Map.Entry<String, ValueWrapper> entry : subjects.entrySet()) {
                result.put(entry.getKey(), entry.getValue().getValue() != null && Boolean.parseBoolean(entry.getValue().getValue()));
            }
        }
        return result;
    }

    public void setSubjects(Map<String, Boolean> subjects) {
        this.subjects = new HashMap<>();
        if (subjects != null) {
            for (Map.Entry<String, Boolean> entry : subjects.entrySet()) {
                ValueWrapper wrapper = new ValueWrapper();
                wrapper.setValue(String.valueOf(entry.getValue()));
                this.subjects.put(entry.getKey(), wrapper);
            }
        }
    }
}