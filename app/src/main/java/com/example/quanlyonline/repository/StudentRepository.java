package com.example.quanlyonline.repository;

import android.util.Log;
import com.example.quanlyonline.model.Student;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class StudentRepository {
    private static final String TAG = "StudentRepository";
    private DatabaseReference usersRef;
    private DatabaseReference classesRef;
    private DatabaseReference scoresRef;
    private DatabaseReference statisticsRef;

    public StudentRepository() {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://quanlyonline-1c06a-default-rtdb.asia-southeast1.firebasedatabase.app");
        usersRef = database.getReference("users");
        classesRef = database.getReference("classes");
        scoresRef = database.getReference("scores");
        statisticsRef = database.getReference("statistics");
    }

    public void getStudentsByClass(String classId, OnStudentsLoadedListener listener) {
        classesRef.child(classId).child("students").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Student> students = new ArrayList<>();
                if (!snapshot.exists()) {
                    listener.onSuccess(students);
                    return;
                }

                long totalStudents = snapshot.getChildrenCount();
                if (totalStudents == 0) {
                    listener.onSuccess(students);
                    return;
                }

                AtomicInteger processedStudents = new AtomicInteger(0);
                for (DataSnapshot studentIdSnapshot : snapshot.getChildren()) {
                    String studentId = studentIdSnapshot.getKey();
                    usersRef.child(studentId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot userSnapshot) {
                            if (userSnapshot.exists()) {
                                Student student = new Student();
                                student.setUserId(userSnapshot.getKey());
                                student.setStudentId(userSnapshot.child("student_id").child("value").getValue(String.class));
                                student.setFullName(userSnapshot.child("full_name").child("value").getValue(String.class));
                                student.setClassName(userSnapshot.child("class_id").child("value").getValue(String.class));
                                student.setDateOfBirth(userSnapshot.child("date_of_birth").child("value").getValue(String.class));
                                student.setAddress(userSnapshot.child("address").child("value").getValue(String.class));
                                Long createdAt = userSnapshot.child("created_at").child("value").getValue(Long.class);
                                Long updatedAt = userSnapshot.child("updated_at").child("value").getValue(Long.class);
                                student.setCreatedAt(createdAt != null ? createdAt : 0L);
                                student.setUpdatedAt(updatedAt != null ? updatedAt : 0L);
                                students.add(student);
                            } else {
                                Log.w(TAG, "Student not found in users: " + studentId);
                            }

                            if (processedStudents.incrementAndGet() == totalStudents) {
                                listener.onSuccess(students);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            Log.e(TAG, "Failed to load student: " + error.getMessage());
                            if (processedStudents.incrementAndGet() == totalStudents) {
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

    public void addStudent(String classId, Student student, OnOperationListener listener) {
        String userId = usersRef.push().getKey();
        student.setUserId(userId);

        usersRef.orderByChild("student_id/value").equalTo(student.getStudentId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    listener.onFailure("Mã sinh viên này đã được sử dụng");
                    return;
                }

                Map<String, Object> updates = new HashMap<>();
                Map<String, Object> userData = new HashMap<>();
                userData.put("username", new HashMap<String, Object>() {{ put("value", student.getStudentId()); }});
                userData.put("password", new HashMap<String, Object>() {{ put("value", student.getStudentId()); }});
                userData.put("role", new HashMap<String, Object>() {{ put("value", "student"); }});
                userData.put("full_name", new HashMap<String, Object>() {{ put("value", student.getFullName()); }});
                userData.put("class_id", new HashMap<String, Object>() {{ put("value", classId); }});
                userData.put("student_id", new HashMap<String, Object>() {{ put("value", student.getStudentId()); }});
                userData.put("date_of_birth", new HashMap<String, Object>() {{ put("value", student.getDateOfBirth()); }});
                userData.put("address", new HashMap<String, Object>() {{ put("value", student.getAddress()); }});
                userData.put("created_at", new HashMap<String, Object>() {{ put("value", student.getCreatedAt()); }});
                userData.put("updated_at", new HashMap<String, Object>() {{ put("value", student.getUpdatedAt()); }});

                updates.put("users/" + userId, userData);
                updates.put("classes/" + classId + "/students/" + userId, new HashMap<String, Object>() {{ put("value", true); }});

                FirebaseDatabase.getInstance().getReference().updateChildren(updates)
                        .addOnSuccessListener(aVoid -> updateStudentCount(classId, listener))
                        .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                listener.onFailure(error.getMessage());
            }
        });
    }

    public void updateStudent(Student student, OnOperationListener listener) {
        Map<String, Object> studentData = new HashMap<>();
        studentData.put("student_id", new HashMap<String, Object>() {{ put("value", student.getStudentId()); }});
        studentData.put("role", new HashMap<String, Object>() {{ put("value", "student"); }});
        studentData.put("full_name", new HashMap<String, Object>() {{ put("value", student.getFullName()); }});
        studentData.put("class_id", new HashMap<String, Object>() {{ put("value", student.getClassName()); }});
        studentData.put("date_of_birth", new HashMap<String, Object>() {{ put("value", student.getDateOfBirth()); }});
        studentData.put("address", new HashMap<String, Object>() {{ put("value", student.getAddress()); }});
        studentData.put("created_at", new HashMap<String, Object>() {{ put("value", student.getCreatedAt()); }});
        studentData.put("updated_at", new HashMap<String, Object>() {{ put("value", student.getUpdatedAt()); }});

        usersRef.child(student.getUserId()).setValue(studentData)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public void deleteStudent(String classId, String userId, OnOperationListener listener) {
        scoresRef.child(userId).removeValue();
        statisticsRef.child(userId).removeValue();

        usersRef.child(userId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    classesRef.child(classId).child("students").child(userId).removeValue()
                            .addOnSuccessListener(aVoid2 -> updateStudentCount(classId, listener))
                            .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    private void updateStudentCount(String classId, OnOperationListener listener) {
        classesRef.child(classId).child("students").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                int count = (int) snapshot.getChildrenCount();
                classesRef.child(classId).child("student_count").child("value").setValue(count)
                        .addOnSuccessListener(aVoid -> listener.onSuccess())
                        .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                listener.onFailure(error.getMessage());
            }
        });
    }

    public interface OnStudentsLoadedListener {
        void onSuccess(List<Student> students);
        void onFailure(String error);
    }

    public interface OnOperationListener {
        void onSuccess();
        void onFailure(String error);
    }
}