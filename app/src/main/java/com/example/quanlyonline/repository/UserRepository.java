package com.example.quanlyonline.repository;

import android.util.Log;
import com.example.quanlyonline.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserRepository {
    private static final String TAG = "UserRepository";
    private DatabaseReference usersRef;

    public UserRepository() {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://quanlyonline-1c06a-default-rtdb.asia-southeast1.firebasedatabase.app");
        usersRef = database.getReference("users");
    }

    public void login(String username, String password, OnLoginListener listener) {
        Log.d(TAG, "Starting login query for username: " + username);
        usersRef.orderByChild("username/value").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Log.d(TAG, "Query result: " + snapshot.toString());
                if (!snapshot.exists()) {
                    Log.d(TAG, "No user found with username: " + username);
                    listener.onFailure("Tài khoản không tồn tại");
                    return;
                }

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    // Ánh xạ thủ công thay vì dùng getValue(User.class)
                    String storedPassword = userSnapshot.child("password").child("value").getValue(String.class);
                    String role = userSnapshot.child("role").child("value").getValue(String.class);

                    if (password.equals(storedPassword)) {
                        User user = new User();
                        user.setUserId(userSnapshot.getKey());
                        user.setRole(role);
                        // Ánh xạ các trường khác nếu cần
                        user.setUsername(userSnapshot.child("username").child("value").getValue(String.class));
                        user.setFullName(userSnapshot.child("full_name").child("value").getValue(String.class));
                        user.setPhone(userSnapshot.child("phone").child("value").getValue(String.class));
                        user.setEmail(userSnapshot.child("email").child("value").getValue(String.class));
                        user.setCreatedAt(userSnapshot.child("created_at").child("value").getValue(Long.class));
                        user.setUpdatedAt(userSnapshot.child("updated_at").child("value").getValue(Long.class));
                        user.setClassManaged(userSnapshot.child("class_managed").child("value").getValue(String.class));
                        listener.onSuccess(user);
                    } else {
                        listener.onFailure("Mật khẩu không đúng");
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Login query failed: " + error.getMessage());
                listener.onFailure(error.getMessage());
            }
        });
    }

    public interface OnLoginListener {
        void onSuccess(User user);
        void onFailure(String error);
    }
}