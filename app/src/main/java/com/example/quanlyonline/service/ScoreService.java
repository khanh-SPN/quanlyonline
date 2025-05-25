package com.example.quanlyonline.service;

import com.example.quanlyonline.model.Score;
import com.example.quanlyonline.repository.ScoreRepository;
import com.example.quanlyonline.controller.ScoreController;

import java.util.Map;

public class ScoreService {
    private ScoreRepository scoreRepository;

    public ScoreService() {
        scoreRepository = new ScoreRepository();
    }

    public void getScoresByStudent(String classId, String studentId, ScoreController.OnScoresResultListener listener) {
        scoreRepository.getScoresByStudent(classId, studentId, listener);
    }

    public void addScore(String studentId, String subject, String semester, Score score, ScoreController.OnOperationResultListener listener) {
        // Kiểm tra điểm giữa kỳ (midterm_score)
        if (score.getScore() != 0.0 && (score.getScore() < 0 || score.getScore() > 10)) {
            listener.onFailure("Điểm giữa kỳ phải từ 0 đến 10");
            return;
        }
        // Kiểm tra điểm cuối kỳ (final_score)
        if (score.getFinalScore() != null && (score.getFinalScore() < 0 || score.getFinalScore() > 10)) {
            listener.onFailure("Điểm cuối kỳ phải từ 0 đến 10");
            return;
        }
        // Đảm bảo ít nhất một loại điểm được nhập
        if (score.getScore() == 0.0 && score.getFinalScore() == null) {
            listener.onFailure("Vui lòng nhập ít nhất một loại điểm");
            return;
        }
        scoreRepository.addScore(studentId, subject, semester, score, listener);
    }

    public void updateScore(String studentId, String subject, String semester, Score score, ScoreController.OnOperationResultListener listener) {
        // Kiểm tra điểm giữa kỳ (midterm_score)
        if (score.getScore() != 0.0 && (score.getScore() < 0 || score.getScore() > 10)) {
            listener.onFailure("Điểm giữa kỳ phải từ 0 đến 10");
            return;
        }
        // Kiểm tra điểm cuối kỳ (final_score)
        if (score.getFinalScore() != null && (score.getFinalScore() < 0 || score.getFinalScore() > 10)) {
            listener.onFailure("Điểm cuối kỳ phải từ 0 đến 10");
            return;
        }
        // Đảm bảo ít nhất một loại điểm được nhập
        if (score.getScore() == 0.0 && score.getFinalScore() == null) {
            listener.onFailure("Vui lòng nhập ít nhất một loại điểm");
            return;
        }
        scoreRepository.updateScore(studentId, subject, semester, score, listener);
    }

    public void deleteScore(String studentId, String subject, String semester, ScoreController.OnOperationResultListener listener) {
        scoreRepository.deleteScore(studentId, subject, semester, listener);
    }
}