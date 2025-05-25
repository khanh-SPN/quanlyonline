package com.example.quanlyonline.model;

public class Score {
    private String scoreId;
    private ValueWrapper score; // Điểm giữa kỳ (midterm_score)
    private ValueWrapper finalScore; // Điểm cuối kỳ (final_score)
    private ValueWrapper date;
    private ValueWrapper teacherId;

    public Score() {}

    public static class ValueWrapper {
        private String value;
        private Double doubleValue; // Dành cho score và finalScore
        private Long longValue;

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

        public Long getLongValue() {
            return longValue;
        }

        public void setLongValue(Long longValue) {
            this.longValue = longValue;
        }
    }

    public String getScoreId() { return scoreId; }
    public void setScoreId(String scoreId) { this.scoreId = scoreId; }

    public double getScore() {
        return score != null && score.getDoubleValue() != null ? score.getDoubleValue() : 0.0;
    }
    public void setScore(Double score) {
        this.score = new ValueWrapper();
        this.score.setDoubleValue(score != null ? score : 0.0);
    }

    public Double getFinalScore() {
        return finalScore != null && finalScore.getDoubleValue() != null ? finalScore.getDoubleValue() : null;
    }
    public void setFinalScore(Double finalScore) {
        this.finalScore = new ValueWrapper();
        this.finalScore.setDoubleValue(finalScore != null ? finalScore : 0.0);
    }

    public String getDate() { return date != null ? date.getValue() : null; }
    public void setDate(String date) {
        this.date = new ValueWrapper();
        this.date.setValue(date);
    }

    public String getTeacherId() { return teacherId != null ? teacherId.getValue() : null; }
    public void setTeacherId(String teacherId) {
        this.teacherId = new ValueWrapper();
        this.teacherId.setValue(teacherId);
    }
}