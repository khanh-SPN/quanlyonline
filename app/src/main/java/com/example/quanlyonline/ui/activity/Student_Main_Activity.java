package com.example.quanlyonline.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.quanlyonline.R;
import com.example.quanlyonline.ui.fragment.Student_Group_Chat_Fragment;
import com.example.quanlyonline.ui.fragment.Student_Notification_List_Fragment;
import com.example.quanlyonline.ui.fragment.Student_Schedule_List_Fragment;
import com.example.quanlyonline.ui.fragment.Student_Score_Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.widget.ImageButton;
import android.widget.Toast;

public class Student_Main_Activity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private ImageButton logoutButton;
    private String userId;
    private String role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_QuanLyOnline_Student);
        setContentView(R.layout.activity_student_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        logoutButton = findViewById(R.id.logout_button);

        // Lấy user_id và role từ Intent
        userId = getIntent().getStringExtra("user_id");
        role = getIntent().getStringExtra("role");

        if (userId == null || role == null || !role.equals("student")) {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.nav_schedules) {
                selectedFragment = new Student_Schedule_List_Fragment();
            } else if (itemId == R.id.nav_scores) {
                selectedFragment = new Student_Score_Fragment();
            } else if (itemId == R.id.nav_notifications) {
                selectedFragment = new Student_Notification_List_Fragment();
            } else if (itemId == R.id.nav_chat) {
                selectedFragment = new Student_Group_Chat_Fragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                return true;
            }
            return false;
        });

        // Thêm sự kiện click cho nút đăng xuất
        logoutButton.setOnClickListener(v -> {
            Intent intent = new Intent(Student_Main_Activity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        loadFragment(new Student_Schedule_List_Fragment());
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}