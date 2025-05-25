package com.example.quanlyonline.repository;

import android.util.Log;
import com.example.quanlyonline.controller.ClassController;
import com.example.quanlyonline.controller.ScoreController;
import com.example.quanlyonline.model.Score;
import com.example.quanlyonline.model.Subject;
import com.example.quanlyonline.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatisticsRepository {
    private static final String TAG = "StatisticsRepository";
    private DatabaseReference usersRef;
    private DatabaseReference classesRef;
    private ScoreController scoreController;
    private ClassController classController;
    private SubjectRepository subjectRepository;

    public StatisticsRepository() {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://quanlyonline-1c06a-default-rtdb.asia-southeast1.firebasedatabase.app");
        usersRef = database.getReference("users");
        classesRef = database.getReference("classes");
        scoreController = new ScoreController();
        classController = new ClassController();
        subjectRepository = new SubjectRepository();
    }

    public void getStatisticsByClass(String classId, OnStatisticsLoadedListener listener) {
        // Load ánh xạ môn học từ subjects
        Map<String, String> subjectIdToNameMap = new HashMap<>();
        Map<String, String> subjectNameToIdMap = new HashMap<>();

        subjectRepository.getSubjectsByClass(classId, new SubjectRepository.OnSubjectsLoadedListener() {
            @Override
            public void onSuccess(List<Subject> subjects) {
                for (Subject subject : subjects) {
                    subjectIdToNameMap.put(subject.getSubjectId(), subject.getName());
                    subjectNameToIdMap.put(subject.getName(), subject.getSubjectId());
                }
                fetchStudentsAndScores(classId, subjectIdToNameMap, listener);
            }

            @Override
            public void onFailure(String error) {
                Log.w(TAG, "Failed to load subject mappings: " + error);
                // Tiếp tục lấy danh sách học sinh ngay cả khi không có ánh xạ
                fetchStudentsAndScores(classId, subjectIdToNameMap, listener);
            }
        });
    }

    private void fetchStudentsAndScores(String classId, Map<String, String> subjectIdToNameMap, OnStatisticsLoadedListener listener) {
        classesRef.child(classId).child("students").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<User> students = new ArrayList<>();
                if (!snapshot.exists()) {
                    listener.onSuccess(students);
                    return;
                }

                long totalStudents = snapshot.getChildrenCount();
                if (totalStudents == 0) {
                    listener.onSuccess(students);
                    return;
                }

                final long[] processedStudents = {0}; // Đếm số học sinh đã xử lý

                for (DataSnapshot studentIdSnapshot : snapshot.getChildren()) {
                    String studentId = studentIdSnapshot.getKey();
                    usersRef.child(studentId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot userSnapshot) {
                            if (!userSnapshot.exists()) {
                                Log.w(TAG, "Student not found in users: " + studentId);
                                processedStudents[0]++;
                                if (processedStudents[0] == totalStudents) {
                                    listener.onSuccess(students);
                                }
                                return;
                            }

                            User student = new User();
                            student.setUserId(userSnapshot.getKey());
                            student.setFullName(userSnapshot.child("full_name").child("value").getValue(String.class));
                            student.setClassId(userSnapshot.child("class_id").child("value").getValue(String.class));

                            scoreController.getScoresByStudent(classId, studentId, new ScoreController.OnScoresResultListener() {
                                @Override
                                public void onSuccess(Map<String, Map<String, Score>> scores) {
                                    double totalScore = 0.0;
                                    int scoreCount = 0;
                                    Map<String, Double> subjects = new HashMap<>();

                                    if (scores != null) {
                                        for (Map.Entry<String, Map<String, Score>> subjectEntry : scores.entrySet()) {
                                            String subjectId = subjectEntry.getKey();
                                            String subjectName = subjectIdToNameMap.getOrDefault(subjectId, subjectId);
                                            Map<String, Score> semesterScores = subjectEntry.getValue();
                                            for (Map.Entry<String, Score> semesterEntry : semesterScores.entrySet()) {
                                                String semester = semesterEntry.getKey();
                                                Score score = semesterEntry.getValue();
                                                // Lấy điểm giữa kỳ
                                                if (score.getScore() != 0.0) {
                                                    totalScore += score.getScore();
                                                    scoreCount++;
                                                    subjects.put(subjectName + "_" + semester + "_midterm", score.getScore());
                                                }
                                                // Lấy điểm cuối kỳ
                                                if (score.getFinalScore() != null) {
                                                    totalScore += score.getFinalScore();
                                                    scoreCount++;
                                                    subjects.put(subjectName + "_" + semester + "_final", score.getFinalScore());
                                                }
                                            }
                                        }
                                    }

                                    double averageScore = scoreCount > 0 ? totalScore / scoreCount : 0.0;
                                    student.setAverageScore(averageScore);
                                    student.setSubjects(subjects);
                                    students.add(student);

                                    processedStudents[0]++;
                                    if (processedStudents[0] == totalStudents) {
                                        listener.onSuccess(students);
                                    }
                                }

                                @Override
                                public void onFailure(String error) {
                                    Log.e(TAG, "Failed to load scores for student " + studentId + ": " + error);
                                    // Nếu không lấy được điểm, vẫn thêm học sinh vào danh sách với điểm 0
                                    student.setAverageScore(0.0);
                                    student.setSubjects(new HashMap<>());
                                    students.add(student);

                                    processedStudents[0]++;
                                    if (processedStudents[0] == totalStudents) {
                                        listener.onSuccess(students);
                                    }
                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            Log.e(TAG, "Failed to load student: " + error.getMessage());
                            processedStudents[0]++;
                            if (processedStudents[0] == totalStudents) {
                                listener.onSuccess(students);
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Failed to load students: " + error.getMessage());
                listener.onFailure(error.getMessage());
            }
        });
    }

    public interface OnStatisticsLoadedListener {
        void onSuccess(List<User> students);
        void onFailure(String error);
    }
}