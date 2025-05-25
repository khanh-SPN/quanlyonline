package com.example.quanlyonline.repository;

import android.util.Log;
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

public class TeacherRepository {
    private static final String TAG = "TeacherRepository";
    private DatabaseReference usersRef;
    private DatabaseReference classesRef;

    public TeacherRepository() {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://quanlyonline-1c06a-default-rtdb.asia-southeast1.firebasedatabase.app");
        usersRef = database.getReference("users");
        classesRef = database.getReference("classes");
    }

    public void getAllTeachers(OnTeachersLoadedListener listener) {
        usersRef.orderByChild("role/value").equalTo("teacher").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<User> teachers = new ArrayList<>();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    User teacher = new User();
                    teacher.setUserId(userSnapshot.getKey());
                    teacher.setUsername(userSnapshot.child("username").child("value").getValue(String.class));
                    teacher.setPassword(userSnapshot.child("password").child("value").getValue(String.class));
                    teacher.setRole(userSnapshot.child("role").child("value").getValue(String.class));
                    teacher.setFullName(userSnapshot.child("full_name").child("value").getValue(String.class));
                    teacher.setPhone(userSnapshot.child("phone").child("value").getValue(String.class));
                    teacher.setEmail(userSnapshot.child("email").child("value").getValue(String.class));
                    teacher.setCreatedAt(userSnapshot.child("created_at").child("value").getValue(Long.class));
                    teacher.setUpdatedAt(userSnapshot.child("updated_at").child("value").getValue(Long.class));
                    teacher.setClassManaged(userSnapshot.child("class_managed").child("value").getValue(String.class));
                    teachers.add(teacher);
                }
                listener.onSuccess(teachers);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Failed to load teachers: " + error.getMessage());
                listener.onFailure(error.getMessage());
            }
        });
    }

    public void addTeacher(User teacher, OnOperationListener listener) {
        String teacherId = usersRef.push().getKey();
        teacher.setUserId(teacherId);

        // Kiểm tra xem giáo viên đã quản lý lớp nào chưa
        if (teacher.getClassManaged() != null && !teacher.getClassManaged().isEmpty()) {
            usersRef.orderByChild("class_managed/value").equalTo(teacher.getClassManaged()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        listener.onFailure("Lớp này đã được quản lý bởi giáo viên khác");
                        return;
                    }

                    Map<String, Object> teacherData = new HashMap<>();
                    teacherData.put("username", new HashMap<String, Object>() {{ put("value", teacher.getUsername()); }});
                    teacherData.put("password", new HashMap<String, Object>() {{ put("value", teacher.getPassword()); }});
                    teacherData.put("role", new HashMap<String, Object>() {{ put("value", "teacher"); }});
                    teacherData.put("full_name", new HashMap<String, Object>() {{ put("value", teacher.getFullName()); }});
                    teacherData.put("phone", new HashMap<String, Object>() {{ put("value", teacher.getPhone()); }});
                    teacherData.put("email", new HashMap<String, Object>() {{ put("value", teacher.getEmail()); }});
                    teacherData.put("created_at", new HashMap<String, Object>() {{ put("value", teacher.getCreatedAt()); }});
                    teacherData.put("updated_at", new HashMap<String, Object>() {{ put("value", teacher.getUpdatedAt()); }});
                    teacherData.put("class_managed", new HashMap<String, Object>() {{ put("value", teacher.getClassManaged()); }});

                    usersRef.child(teacherId).setValue(teacherData)
                            .addOnSuccessListener(aVoid -> listener.onSuccess())
                            .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    listener.onFailure(error.getMessage());
                }
            });
        } else {
            Map<String, Object> teacherData = new HashMap<>();
            teacherData.put("username", new HashMap<String, Object>() {{ put("value", teacher.getUsername()); }});
            teacherData.put("password", new HashMap<String, Object>() {{ put("value", teacher.getPassword()); }});
            teacherData.put("role", new HashMap<String, Object>() {{ put("value", "teacher"); }});
            teacherData.put("full_name", new HashMap<String, Object>() {{ put("value", teacher.getFullName()); }});
            teacherData.put("phone", new HashMap<String, Object>() {{ put("value", teacher.getPhone()); }});
            teacherData.put("email", new HashMap<String, Object>() {{ put("value", teacher.getEmail()); }});
            teacherData.put("created_at", new HashMap<String, Object>() {{ put("value", teacher.getCreatedAt()); }});
            teacherData.put("updated_at", new HashMap<String, Object>() {{ put("value", teacher.getUpdatedAt()); }});
            teacherData.put("class_managed", new HashMap<String, Object>() {{ put("value", teacher.getClassManaged()); }});

            usersRef.child(teacherId).setValue(teacherData)
                    .addOnSuccessListener(aVoid -> listener.onSuccess())
                    .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
        }
    }

    public void updateTeacher(User teacher, OnOperationListener listener) {
        if (teacher.getClassManaged() != null && !teacher.getClassManaged().isEmpty()) {
            usersRef.orderByChild("class_managed/value").equalTo(teacher.getClassManaged()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    boolean classManagedByOther = false;
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        String userId = userSnapshot.getKey();
                        if (!userId.equals(teacher.getUserId())) {
                            classManagedByOther = true;
                            break;
                        }
                    }
                    if (classManagedByOther) {
                        listener.onFailure("Lớp này đã được quản lý bởi giáo viên khác");
                        return;
                    }

                    Map<String, Object> teacherData = new HashMap<>();
                    teacherData.put("username", new HashMap<String, Object>() {{ put("value", teacher.getUsername()); }});
                    teacherData.put("password", new HashMap<String, Object>() {{ put("value", teacher.getPassword()); }});
                    teacherData.put("role", new HashMap<String, Object>() {{ put("value", "teacher"); }});
                    teacherData.put("full_name", new HashMap<String, Object>() {{ put("value", teacher.getFullName()); }});
                    teacherData.put("phone", new HashMap<String, Object>() {{ put("value", teacher.getPhone()); }});
                    teacherData.put("email", new HashMap<String, Object>() {{ put("value", teacher.getEmail()); }});
                    teacherData.put("created_at", new HashMap<String, Object>() {{ put("value", teacher.getCreatedAt()); }});
                    teacherData.put("updated_at", new HashMap<String, Object>() {{ put("value", teacher.getUpdatedAt()); }});
                    teacherData.put("class_managed", new HashMap<String, Object>() {{ put("value", teacher.getClassManaged()); }});

                    usersRef.child(teacher.getUserId()).setValue(teacherData)
                            .addOnSuccessListener(aVoid -> listener.onSuccess())
                            .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    listener.onFailure(error.getMessage());
                }
            });
        } else {
            Map<String, Object> teacherData = new HashMap<>();
            teacherData.put("username", new HashMap<String, Object>() {{ put("value", teacher.getUsername()); }});
            teacherData.put("password", new HashMap<String, Object>() {{ put("value", teacher.getPassword()); }});
            teacherData.put("role", new HashMap<String, Object>() {{ put("value", "teacher"); }});
            teacherData.put("full_name", new HashMap<String, Object>() {{ put("value", teacher.getFullName()); }});
            teacherData.put("phone", new HashMap<String, Object>() {{ put("value", teacher.getPhone()); }});
            teacherData.put("email", new HashMap<String, Object>() {{ put("value", teacher.getEmail()); }});
            teacherData.put("created_at", new HashMap<String, Object>() {{ put("value", teacher.getCreatedAt()); }});
            teacherData.put("updated_at", new HashMap<String, Object>() {{ put("value", teacher.getUpdatedAt()); }});
            teacherData.put("class_managed", new HashMap<String, Object>() {{ put("value", teacher.getClassManaged()); }});

            usersRef.child(teacher.getUserId()).setValue(teacherData)
                    .addOnSuccessListener(aVoid -> listener.onSuccess())
                    .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
        }
    }

    public void deleteTeacher(String teacherId, OnOperationListener listener) {
        usersRef.child(teacherId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    listener.onFailure("Giáo viên không tồn tại");
                    return;
                }

                String classManaged = snapshot.child("class_managed").child("value").getValue(String.class);

                // Xóa class_managed của giáo viên và cập nhật teacher_id trong classes
                if (classManaged != null) {
                    classesRef.child(classManaged).child("teacher_id").child("value").removeValue();
                }

                usersRef.child(teacherId).removeValue()
                        .addOnSuccessListener(aVoid -> listener.onSuccess())
                        .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                listener.onFailure(error.getMessage());
            }
        });
    }

    public interface OnTeachersLoadedListener {
        void onSuccess(List<User> teachers);
        void onFailure(String error);
    }

    public interface OnOperationListener {
        void onSuccess();
        void onFailure(String error);
    }
}