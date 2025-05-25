package com.example.quanlyonline.model;

import java.util.HashMap;
import java.util.Map;

public class Statistics {
    private String studentId;
    private ValueWrapper averageScore;
    private Map<String, ValueWrapper> subjects;
    private ValueWrapper lastUpdated;

    public Statistics() {
        this.subjects = new HashMap<>();
    }

    // Class lồng nhau để ánh xạ các trường có dạng { "value": "..." }
    public static class ValueWrapper {
        private String value;
        private Double doubleValue; // Dành cho averageScore và subjects

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public Double getDoubleValue() {
            return doubleValue;
        }

        public void setDoubleValue(Double doubleValue) {
            this.doubleValue = doubleValue;
        }
    }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public double getAverageScore() { return averageScore != null && averageScore.getDoubleValue() != null ? averageScore.getDoubleValue() : 0.0; }
    public void setAverageScore(double averageScore) {
        this.averageScore = new ValueWrapper();
        this.averageScore.setDoubleValue(averageScore);
    }

    public Map<String, Double> getSubjects() {
        Map<String, Double> result = new HashMap<>();
        if (subjects != null) {
            for (Map.Entry<String, ValueWrapper> entry : subjects.entrySet()) {
                result.put(entry.getKey(), entry.getValue().getDoubleValue() != null ? entry.getValue().getDoubleValue() : 0.0);
            }
        }
        return result;
    }

    public void setSubjects(Map<String, Double> subjects) {
        this.subjects = new HashMap<>();
        if (subjects != null) {
            for (Map.Entry<String, Double> entry : subjects.entrySet()) {
                ValueWrapper wrapper = new ValueWrapper();
                wrapper.setDoubleValue(entry.getValue());
                this.subjects.put(entry.getKey(), wrapper);
            }
        }
    }

    public String getLastUpdated() { return lastUpdated != null ? lastUpdated.getValue() : null; }
    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = new ValueWrapper();
        this.lastUpdated.setValue(lastUpdated);
    }
}