package com.example.quanlyonline.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.quanlyonline.R;
import com.example.quanlyonline.StudentProfileFragment;
import com.example.quanlyonline.ui.fragment.ChatListFragment;
import com.example.quanlyonline.ui.fragment.Student_Group_Chat_Fragment;
import com.example.quanlyonline.ui.fragment.Student_Notification_List_Fragment;
import com.example.quanlyonline.ui.fragment.Student_Schedule_List_Fragment;
import com.example.quanlyonline.ui.fragment.Student_Score_Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class Student_Main_Activity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private BottomNavigationView bottomNavigationView;
    private ImageButton logoutButton;
    private String userId;
    private String role;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_QuanLyOnline_Student);
        setContentView(R.layout.activity_student_main);

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

        if (userId == null || role == null || !role.equals("student")) {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        // Hiển thị StudentProfileFragment làm fragment mặc định sau khi đăng nhập
        if (savedInstanceState == null) {
            loadFragment(new StudentProfileFragment());
        }

        // Xử lý sự kiện nhấn vào các tab trong BottomNavigationView
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.nav_schedules) {
                selectedFragment = new Student_Schedule_List_Fragment();
            } else if (itemId == R.id.nav_scores) {
                selectedFragment = new Student_Score_Fragment();
            } else if (itemId == R.id.nav_notifications) {
                selectedFragment = new Student_Notification_List_Fragment();
            } else if (itemId == R.id.nav_group_chat) {
                selectedFragment = new Student_Group_Chat_Fragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                return true;
            }
            return false;
        });

        // Xử lý sự kiện nhấn vào các mục trong Navigation Drawer
        navView.setNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_profile) {
                selectedFragment = new StudentProfileFragment();
            } else if (itemId == R.id.nav_scores) {
                selectedFragment = new Student_Score_Fragment();
            } else if (itemId == R.id.nav_schedules) {
                selectedFragment = new Student_Schedule_List_Fragment();
            } else if (itemId == R.id.nav_one_on_one_chat) {
                selectedFragment = new ChatListFragment();
            } else if (itemId == R.id.nav_notifications) {
                selectedFragment = new Student_Notification_List_Fragment();
            } else if (itemId == R.id.nav_group_chat) {
                selectedFragment = new Student_Group_Chat_Fragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }

            // Đóng drawer sau khi chọn mục
            if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            }
            return true;
        });

        // Thêm sự kiện click cho nút đăng xuất
        logoutButton.setOnClickListener(v -> {
            Intent intent = new Intent(Student_Main_Activity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}