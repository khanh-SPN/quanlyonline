package com.example.quanlyonline.ui.activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.quanlyonline.R;
import com.example.quanlyonline.ui.fragment.ClassListFragment;
import com.example.quanlyonline.ui.fragment.StudentListFragment;
import com.example.quanlyonline.ui.fragment.TeacherListFragment;
import com.example.quanlyonline.ui.fragment.SubjectListFragment;
import com.example.quanlyonline.ui.fragment.ScoreListFragment;
import com.example.quanlyonline.ui.fragment.ScheduleListFragment;
import com.example.quanlyonline.ui.fragment.SendNotificationFragment;
import com.example.quanlyonline.ui.fragment.GroupChatFragment;
import com.example.quanlyonline.ui.fragment.StatisticsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String role = getIntent().getStringExtra("role");
        if (role != null && role.equals("admin")) {
            setTheme(R.style.Theme_QuanLyOnline_Admin);
        } else {
            setTheme(R.style.Theme_QuanLyOnline_Teacher);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        if (role != null) {
            if (role.equals("admin")) {
                // Admin: Quản lý lớp, học sinh, giáo viên, môn học
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

                loadFragment(new ClassListFragment());
            } else if (role.equals("teacher")) {
                // Giáo viên: Quản lý điểm số, lịch học, thông báo, chat nhóm, thống kê
                bottomNavigationView.setOnItemSelectedListener(item -> {
                    Fragment selectedFragment = null;
                    int itemId = item.getItemId();
                    if (itemId == R.id.nav_scores) {
                        selectedFragment = new ScoreListFragment();
                    } else if (itemId == R.id.nav_schedules) {
                        selectedFragment = new ScheduleListFragment();
                    } else if (itemId == R.id.nav_notifications) {
                        selectedFragment = new SendNotificationFragment();
                    } else if (itemId == R.id.nav_chat) {
                        selectedFragment = new GroupChatFragment();
                    } else if (itemId == R.id.nav_statistics) {
                        selectedFragment = new StatisticsFragment();
                    }

                    if (selectedFragment != null) {
                        loadFragment(selectedFragment);
                        return true;
                    }
                    return false;
                });

                loadFragment(new ScoreListFragment());
            } else {
                finish();
            }
        } else {
            finish();
        }
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}