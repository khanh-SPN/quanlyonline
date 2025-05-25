package com.example.quanlyonline.controller;

import com.example.quanlyonline.model.Score;
import com.example.quanlyonline.repository.ScoreRepository;

import java.util.Map;

public class ScoreController {
    private ScoreRepository scoreRepository;

    public ScoreController() {
        scoreRepository = new ScoreRepository();
    }

    public void getScoresByStudent(String classId, String studentId, OnScoresResultListener listener) {
        scoreRepository.getScoresByStudent(classId, studentId, listener);
    }

    public void addScore(String studentId, String subject, String semester, Score score, OnOperationResultListener listener) {
        scoreRepository.addScore(studentId, subject, semester, score, listener);
    }

    public void updateScore(String studentId, String subject, String semester, Score score, OnOperationResultListener listener) {
        scoreRepository.updateScore(studentId, subject, semester, score, listener);
    }

    public void deleteScore(String studentId, String subject, String semester, OnOperationResultListener listener) {
        scoreRepository.deleteScore(studentId, subject, semester, listener);
    }

    public interface OnScoresResultListener {
        void onSuccess(Map<String, Map<String, Score>> scores);
        void onFailure(String error);
    }

    public interface OnOperationResultListener {
        void onSuccess();
        void onFailure(String error);
    }
}