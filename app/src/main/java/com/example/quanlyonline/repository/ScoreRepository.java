package com.example.quanlyonline.repository;

import android.util.Log;

import com.example.quanlyonline.controller.ScoreController;
import com.example.quanlyonline.model.Score;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class ScoreRepository {
    private static final String TAG = "ScoreRepository";
    private DatabaseReference scoresRef;

    public ScoreRepository() {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://quanlyonline-1c06a-default-rtdb.asia-southeast1.firebasedatabase.app");
        scoresRef = database.getReference("scores");
    }

    public void getScoresByStudent(String classId, String studentId, ScoreController.OnScoresResultListener listener) {
        if (studentId == null) {
            listener.onFailure("Student ID không hợp lệ");
            return;
        }
        scoresRef.child(studentId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Map<String, Map<String, Score>> scores = new HashMap<>();
                for (DataSnapshot subjectSnapshot : snapshot.getChildren()) {
                    String subject = subjectSnapshot.getKey();
                    Map<String, Score> semesterScores = new HashMap<>();
                    for (DataSnapshot semesterSnapshot : subjectSnapshot.getChildren()) {
                        String semester = semesterSnapshot.getKey();
                        Score score = new Score();
                        score.setScoreId(semesterSnapshot.getKey());
                        Double midtermScore = semesterSnapshot.child("midterm_score").child("value").getValue(Double.class);
                        Double finalScore = semesterSnapshot.child("final_score").child("value").getValue(Double.class);
                        score.setScore(midtermScore != null ? midtermScore : 0.0);
                        score.setFinalScore(finalScore);
                        score.setDate(semesterSnapshot.child("date").child("value").getValue(String.class));
                        score.setTeacherId(semesterSnapshot.child("teacher_id").child("value").getValue(String.class));
                        semesterScores.put(semester, score);
                    }
                    scores.put(subject, semesterScores);
                }
                listener.onSuccess(scores);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Failed to load scores: " + error.getMessage());
                listener.onFailure(error.getMessage());
            }
        });
    }

    public void addScore(String studentId, String subject, String semester, Score score, ScoreController.OnOperationResultListener listener) {
        if (studentId == null || subject == null || semester == null) {
            listener.onFailure("Thông tin không hợp lệ: studentId, subject hoặc semester không được null");
            return;
        }

        Map<String, Object> scoreData = new HashMap<>();
        if (score.getScore() != 0.0) {
            scoreData.put("midterm_score", new HashMap<String, Object>() {{ put("value", score.getScore()); }});
        }
        if (score.getFinalScore() != null) {
            scoreData.put("final_score", new HashMap<String, Object>() {{ put("value", score.getFinalScore()); }});
        }
        scoreData.put("date", new HashMap<String, Object>() {{ put("value", score.getDate()); }});
        scoreData.put("teacher_id", new HashMap<String, Object>() {{ put("value", score.getTeacherId()); }});

        scoresRef.child(studentId).child(subject).child(semester).setValue(scoreData)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public void updateScore(String studentId, String subject, String semester, Score score, ScoreController.OnOperationResultListener listener) {
        if (studentId == null || subject == null || semester == null) {
            listener.onFailure("Thông tin không hợp lệ: studentId, subject hoặc semester không được null");
            return;
        }

        Map<String, Object> scoreData = new HashMap<>();
        if (score.getScore() != 0.0) {
            scoreData.put("midterm_score", new HashMap<String, Object>() {{ put("value", score.getScore()); }});
        }
        if (score.getFinalScore() != null) {
            scoreData.put("final_score", new HashMap<String, Object>() {{ put("value", score.getFinalScore()); }});
        }
        scoreData.put("date", new HashMap<String, Object>() {{ put("value", score.getDate()); }});
        scoreData.put("teacher_id", new HashMap<String, Object>() {{ put("value", score.getTeacherId()); }});

        scoresRef.child(studentId).child(subject).child(semester).setValue(scoreData)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public void deleteScore(String studentId, String subject, String semester, ScoreController.OnOperationResultListener listener) {
        if (studentId == null || subject == null || semester == null) {
            listener.onFailure("Thông tin không hợp lệ: studentId, subject hoặc semester không được null");
            return;
        }

        scoresRef.child(studentId).child(subject).child(semester).removeValue()
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }
}