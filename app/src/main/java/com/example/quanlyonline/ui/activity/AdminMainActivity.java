package com.example.quanlyonline.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.quanlyonline.R;
import com.example.quanlyonline.ui.fragment.ClassListFragment;
import com.example.quanlyonline.ui.fragment.StudentListFragment;
import com.example.quanlyonline.ui.fragment.TeacherListFragment;
import com.example.quanlyonline.ui.fragment.SubjectListFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.view.animation.AnimationUtils;
import android.widget.ImageButton;

public class AdminMainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private ImageButton logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_QuanLyOnline_Admin);
        setContentView(R.layout.activity_admin_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        logoutButton = findViewById(R.id.logout_button);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.nav_classes) {
                selectedFragment = new ClassListFragment();
            } else if (itemId == R.id.nav_students) {
                selectedFragment = new StudentListFragment();
            } else if (itemId == R.id.nav_teachers) {
                selectedFragment = new TeacherListFragment();
            } else if (itemId == R.id.nav_subjects) {
                selectedFragment = new SubjectListFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                return true;
            }
            return false;
        });

        // Thêm sự kiện click cho nút đăng xuất
        logoutButton.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_animation));
            // Chuyển hướng về LoginActivity
            Intent intent = new Intent(AdminMainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Xóa toàn bộ stack activity
            startActivity(intent);
            finish(); // Kết thúc AdminMainActivity
        });

        loadFragment(new ClassListFragment());
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}