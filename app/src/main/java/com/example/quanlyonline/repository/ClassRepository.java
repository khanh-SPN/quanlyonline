package com.example.quanlyonline.repository;

import android.util.Log;
import com.example.quanlyonline.model.Class;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassRepository {
    private static final String TAG = "ClassRepository";
    private DatabaseReference classesRef;
    private DatabaseReference usersRef;

    public ClassRepository() {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://quanlyonline-1c06a-default-rtdb.asia-southeast1.firebasedatabase.app");
        classesRef = database.getReference("classes");
        usersRef = database.getReference("users");
    }

    public void getAllClasses(OnClassesLoadedListener listener) {
        classesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Class> classes = new ArrayList<>();
                for (DataSnapshot classSnapshot : snapshot.getChildren()) {
                    Class classObj = new Class();
                    classObj.setClassId(classSnapshot.getKey());
                    classObj.setName(classSnapshot.child("name").child("value").getValue(String.class));
                    classObj.setTeacherId(classSnapshot.child("teacher_id").child("value").getValue(String.class));
                    classObj.setStudentCount(classSnapshot.child("student_count").child("value").getValue(Integer.class));

                    Map<String, Boolean> students = new HashMap<>();
                    for (DataSnapshot studentSnapshot : classSnapshot.child("students").getChildren()) {
                        students.put(studentSnapshot.getKey(), studentSnapshot.child("value").getValue(Boolean.class));
                    }
                    classObj.setStudents(students);

                    Map<String, Boolean> subjects = new HashMap<>();
                    for (DataSnapshot subjectSnapshot : classSnapshot.child("subjects").getChildren()) {
                        subjects.put(subjectSnapshot.getKey(), subjectSnapshot.child("value").getValue(Boolean.class));
                    }
                    classObj.setSubjects(subjects);

                    classes.add(classObj);
                }
                listener.onSuccess(classes);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Failed to load classes: " + error.getMessage());
                listener.onFailure(error.getMessage());
            }
        });
    }

    public void addClass(Class classObj, OnOperationListener listener) {
        String classId = classesRef.push().getKey();
        classObj.setClassId(classId);

        // Không cần kiểm tra số lượng học sinh nữa
        int studentCount = classObj.getStudents() != null ? classObj.getStudents().size() : 0;
        classObj.setStudentCount(studentCount);

        // Kiểm tra xem giáo viên đã quản lý lớp nào chưa
        usersRef.orderByChild("class_managed/value").equalTo(classObj.getTeacherId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    listener.onFailure("Giáo viên này đã quản lý một lớp khác");
                    return;
                }

                Map<String, Object> classData = new HashMap<>();
                classData.put("name", new HashMap<String, Object>() {{ put("value", classObj.getName()); }});
                classData.put("teacher_id", new HashMap<String, Object>() {{ put("value", classObj.getTeacherId()); }});
                classData.put("student_count", new HashMap<String, Object>() {{ put("value", studentCount); }});
                classData.put("students", classObj.getStudents());
                classData.put("subjects", classObj.getSubjects());

                usersRef.child(classObj.getTeacherId()).child("class_managed").child("value").setValue(classId)
                        .addOnSuccessListener(aVoid -> {
                            classesRef.child(classId).setValue(classData)
                                    .addOnSuccessListener(aVoid2 -> updateStudentsClass(classId, classObj.getStudents(), listener))
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

    public void updateClass(Class classObj, OnOperationListener listener) {
        // Không cần kiểm tra số lượng học sinh nữa
        int studentCount = classObj.getStudents() != null ? classObj.getStudents().size() : 0;
        classObj.setStudentCount(studentCount);

        // Kiểm tra xem giáo viên mới có đang quản lý lớp khác không
        usersRef.orderByChild("class_managed/value").equalTo(classObj.getTeacherId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean teacherHasOtherClass = false;
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String userId = userSnapshot.getKey();
                    if (!userId.equals(classObj.getTeacherId())) {
                        teacherHasOtherClass = true;
                        break;
                    }
                }
                if (teacherHasOtherClass) {
                    listener.onFailure("Giáo viên này đã quản lý một lớp khác");
                    return;
                }

                Map<String, Object> classData = new HashMap<>();
                classData.put("name", new HashMap<String, Object>() {{ put("value", classObj.getName()); }});
                classData.put("teacher_id", new HashMap<String, Object>() {{ put("value", classObj.getTeacherId()); }});
                classData.put("student_count", new HashMap<String, Object>() {{ put("value", studentCount); }});
                classData.put("students", classObj.getStudents());
                classData.put("subjects", classObj.getSubjects());

                classesRef.child(classObj.getClassId()).setValue(classData)
                        .addOnSuccessListener(aVoid -> updateStudentsClass(classObj.getClassId(), classObj.getStudents(), listener))
                        .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                listener.onFailure(error.getMessage());
            }
        });
    }

    public void deleteClass(String classId, OnOperationListener listener) {
        classesRef.child(classId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    listener.onFailure("Lớp không tồn tại");
                    return;
                }

                String teacherId = snapshot.child("teacher_id").child("value").getValue(String.class);
                Map<String, Boolean> students = new HashMap<>();
                for (DataSnapshot studentSnapshot : snapshot.child("students").getChildren()) {
                    students.put(studentSnapshot.getKey(), studentSnapshot.child("value").getValue(Boolean.class));
                }

                if (teacherId != null) {
                    usersRef.child(teacherId).child("class_managed").child("value").removeValue();
                }

                for (String studentId : students.keySet()) {
                    usersRef.child(studentId).child("class_id").child("value").removeValue();
                }

                classesRef.child(classId).removeValue()
                        .addOnSuccessListener(aVoid -> listener.onSuccess())
                        .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                listener.onFailure(error.getMessage());
            }
        });
    }

    private void updateStudentsClass(String classId, Map<String, Boolean> students, OnOperationListener listener) {
        if (students == null || students.isEmpty()) {
            listener.onSuccess();
            return;
        }
        for (String studentId : students.keySet()) {
            usersRef.child(studentId).child("class_id").child("value").setValue(classId);
        }
        listener.onSuccess();
    }

    public interface OnClassesLoadedListener {
        void onSuccess(List<Class> classes);
        void onFailure(String error);
    }

    public interface OnOperationListener {
        void onSuccess();
        void onFailure(String error);
    }
}