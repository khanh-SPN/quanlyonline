package com.example.quanlyonline.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.quanlyonline.R;
import com.example.quanlyonline.model.User;
import com.example.quanlyonline.repository.UserRepository;

public class LoginActivity extends AppCompatActivity {

    private UserRepository userRepository;
    private EditText emailInput, passwordInput;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userRepository = new UserRepository();
        emailInput = findViewById(R.id.username_input);
        passwordInput = findViewById(R.id.password_input);
        loginButton = findViewById(R.id.login_button);

        loginButton.setOnClickListener(v -> {
            String username = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Vui lòng nhập tên đăng nhập và mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }

            userRepository.login(username, password, new UserRepository.OnLoginListener() {
                @Override
                public void onSuccess(User user) {
                    String role = user.getRole();
                    String userId = user.getUserId();
                    Intent intent;
                    if ("admin".equals(role)) {
                        intent = new Intent(LoginActivity.this, AdminMainActivity.class);
                    } else if ("teacher".equals(role)) {
                        intent = new Intent(LoginActivity.this, TeacherMainActivity.class);
                    } else if ("student".equals(role)) {
                        intent = new Intent(LoginActivity.this, Student_Main_Activity.class);
                    }
                    else {
                        Toast.makeText(LoginActivity.this, "Vai trò không hợp lệ", Toast.LENGTH_LONG).show();
                        return;
                    }
                    intent.putExtra("role", role);
                    intent.putExtra("user_id", userId);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onFailure(String error) {
                    Toast.makeText(LoginActivity.this, "Đăng nhập thất bại: " + error, Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}