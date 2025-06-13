package com.example.quanlyonline.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.quanlyonline.R;
import com.example.quanlyonline.ui.fragment.GroupChatFragment;
import com.example.quanlyonline.ui.fragment.TeacherProfileFragment;
import com.example.quanlyonline.ui.fragment.ChatListFragment;
import com.example.quanlyonline.ui.fragment.ScoreListFragment;
import com.example.quanlyonline.ui.fragment.ScheduleListFragment;
import com.example.quanlyonline.ui.fragment.SendNotificationFragment;
import com.example.quanlyonline.ui.fragment.StatisticsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class TeacherMainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private BottomNavigationView bottomNavigationView;
    private ImageButton logoutButton;
    private String userId;
    private String role;

    private static final String TAG = "TeacherMainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_QuanLyOnline_Teacher_NoActionBar);
        setContentView(R.layout.activity_teacher_main);

        // Khởi tạo DrawerLayout và NavigationView
        drawerLayout = findViewById(R.id.drawer_layout);
        navView = findViewById(R.id.nav_view);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        logoutButton = findViewById(R.id.logout_button);

        // Khởi tạo Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Thiết lập ActionBarDrawerToggle để mở/đóng drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Thiết lập chiều rộng của Navigation Drawer (4/5 màn hình)
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) navView.getLayoutParams();
        params.width = (int) (screenWidth * 0.8); // 4/5 chiều rộng màn hình
        navView.setLayoutParams(params);

        // Lấy user_id và role từ Intent
        userId = getIntent().getStringExtra("user_id");
        role = getIntent().getStringExtra("role");

        // Kiểm tra user_id và role
        if (userId == null || role == null || !role.equals("teacher")) {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        // Hiển thị TeacherProfileFragment làm fragment mặc định sau khi đăng nhập
        if (savedInstanceState == null) {
            loadFragment(new TeacherProfileFragment());
        }

        // Xử lý sự kiện nhấn vào các tab trong BottomNavigationView
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_scores) {
                selectedFragment = new ScoreListFragment();
            } else if (itemId == R.id.nav_schedules) {
                selectedFragment = new ScheduleListFragment();
            } else if (itemId == R.id.nav_notifications) {
                selectedFragment = new SendNotificationFragment();
            } else if (itemId == R.id.nav_statistics) {
                selectedFragment = new StatisticsFragment();
            } else if (itemId == R.id.nav_chat) {
                selectedFragment = new GroupChatFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                // Đóng drawer nếu đang mở
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                return true;
            }
            return false;
        });

        // Xử lý sự kiện nhấn vào các mục trong Navigation Drawer
        navView.setNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_profile) {
                selectedFragment = new TeacherProfileFragment();
            } else if (itemId == R.id.nav_scores) {
                selectedFragment = new ScoreListFragment();
            } else if (itemId == R.id.nav_schedules) {
                selectedFragment = new ScheduleListFragment();
            } else if (itemId == R.id.nav_notifications) {
                selectedFragment = new SendNotificationFragment();
            } else if (itemId == R.id.nav_one_on_one_chat) {
                selectedFragment = new ChatListFragment();
            } else if (itemId == R.id.nav_statistics) {
                selectedFragment = new StatisticsFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }

            // Đóng drawer sau khi chọn mục
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Thêm sự kiện click cho nút đăng xuất
        logoutButton.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_animation));
            Intent intent = new Intent(TeacherMainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Đóng drawer khi nhấn ra ngoài vùng menu (1/5 màn hình bên phải)
        drawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {}

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {}

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {}

            @Override
            public void onDrawerStateChanged(int newState) {
                if (newState == DrawerLayout.STATE_SETTLING && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    // Đóng drawer nếu người dùng chạm vào vùng ngoài menu
                    drawerLayout.setOnTouchListener((v, event) -> {
                        float x = event.getX();
                        if (x > params.width) { // Nếu chạm vào vùng ngoài menu (1/5 bên phải)
                            drawerLayout.closeDrawer(GravityCompat.START);
                            return true;
                        }
                        return false;
                    });
                }
            }
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}