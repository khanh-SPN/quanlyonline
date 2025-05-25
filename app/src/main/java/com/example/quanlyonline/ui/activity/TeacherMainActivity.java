package com.example.quanlyonline.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.quanlyonline.R;
import com.example.quanlyonline.ui.fragment.ScoreListFragment;
import com.example.quanlyonline.ui.fragment.ScheduleListFragment;
import com.example.quanlyonline.ui.fragment.SendNotificationFragment;
import com.example.quanlyonline.ui.fragment.GroupChatFragment;
import com.example.quanlyonline.ui.fragment.StatisticsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.view.animation.AnimationUtils;
import android.widget.ImageButton;

public class TeacherMainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private ImageButton logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_QuanLyOnline_Teacher);
        setContentView(R.layout.activity_teacher_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        logoutButton = findViewById(R.id.logout_button);

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

        // Thêm sự kiện click cho nút đăng xuất
        logoutButton.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_animation));
            Intent intent = new Intent(TeacherMainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        loadFragment(new ScoreListFragment());
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}