package com.example.quanlyonline.repository;

import android.util.Log;
import com.example.quanlyonline.model.Subject;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubjectRepository {
    private static final String TAG = "SubjectRepository";
    private DatabaseReference subjectsRef;
    private DatabaseReference classesRef;
    private DatabaseReference scoresRef;

    public SubjectRepository() {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://quanlyonline-1c06a-default-rtdb.asia-southeast1.firebasedatabase.app");
        subjectsRef = database.getReference("subjects");
        classesRef = database.getReference("classes");
        scoresRef = database.getReference("scores");
    }

    public void getAllSubjects(OnSubjectsLoadedListener listener) {
        subjectsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Subject> subjects = new ArrayList<>();
                for (DataSnapshot subjectSnapshot : snapshot.getChildren()) {
                    Subject subject = new Subject();
                    subject.setSubjectId(subjectSnapshot.getKey());
                    subject.setName(subjectSnapshot.child("name").child("value").getValue(String.class));
                    subject.setClassId(subjectSnapshot.child("class_id").child("value").getValue(String.class));
                    subjects.add(subject);
                }
                listener.onSuccess(subjects);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Failed to load subjects: " + error.getMessage());
                listener.onFailure(error.getMessage());
            }
        });
    }
    public void getSubjectsByClass(String classId, OnSubjectsLoadedListener listener) {
        subjectsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Subject> subjects = new ArrayList<>();
                for (DataSnapshot subjectSnapshot : snapshot.getChildren()) {
                    String subjectClassId = subjectSnapshot.child("class_id").child("value").getValue(String.class);
                    if (subjectClassId != null && subjectClassId.equals(classId)) {
                        Subject subject = new Subject();
                        subject.setSubjectId(subjectSnapshot.getKey());
                        subject.setName(subjectSnapshot.child("name").child("value").getValue(String.class));
                        subject.setClassId(subjectClassId);
                        subjects.add(subject);
                    }
                }
                listener.onSuccess(subjects);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Failed to load subjects: " + error.getMessage());
                listener.onFailure(error.getMessage());
            }
        });
    }

    public void addSubject(Subject subject, OnOperationListener listener) {
        String subjectId = subjectsRef.push().getKey();
        subject.setSubjectId(subjectId);

        Map<String, Object> subjectData = new HashMap<>();
        subjectData.put("name", new HashMap<String, Object>() {{ put("value", subject.getName()); }});
        subjectData.put("class_id", new HashMap<String, Object>() {{ put("value", subject.getClassId()); }});

        subjectsRef.child(subjectId).setValue(subjectData)
                .addOnSuccessListener(aVoid -> {
                    classesRef.child(subject.getClassId()).child("subjects").child(subjectId).setValue(new HashMap<String, Object>() {{ put("value", true); }})
                            .addOnSuccessListener(aVoid2 -> listener.onSuccess())
                            .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public void updateSubject(Subject subject, OnOperationListener listener) {
        Map<String, Object> subjectData = new HashMap<>();
        subjectData.put("name", new HashMap<String, Object>() {{ put("value", subject.getName()); }});
        subjectData.put("class_id", new HashMap<String, Object>() {{ put("value", subject.getClassId()); }});

        subjectsRef.child(subject.getSubjectId()).setValue(subjectData)
                .addOnSuccessListener(aVoid -> {
                    classesRef.child(subject.getClassId()).child("subjects").child(subject.getSubjectId()).setValue(new HashMap<String, Object>() {{ put("value", true); }})
                            .addOnSuccessListener(aVoid2 -> listener.onSuccess())
                            .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public void deleteSubject(String subjectId, String classId, OnOperationListener listener) {
        scoresRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot studentSnapshot : snapshot.getChildren()) {
                    if (studentSnapshot.child(subjectId).exists()) {
                        scoresRef.child(studentSnapshot.getKey()).child(subjectId).removeValue();
                    }
                }

                subjectsRef.child(subjectId).removeValue()
                        .addOnSuccessListener(aVoid -> {
                            classesRef.child(classId).child("subjects").child(subjectId).removeValue()
                                    .addOnSuccessListener(aVoid2 -> listener.onSuccess())
                                    .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
                        })
                        .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                listener.onFailure(error.getMessage());
            }
        });
    }


    public interface OnSubjectsLoadedListener {
        void onSuccess(List<Subject> subjects);
        void onFailure(String error);
    }

    public interface OnOperationListener {
        void onSuccess();
        void onFailure(String error);
    }
}