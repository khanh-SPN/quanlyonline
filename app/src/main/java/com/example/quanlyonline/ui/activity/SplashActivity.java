package com.example.quanlyonline.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.quanlyonline.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Tìm ImageView chứa logo
        ImageView logo = findViewById(R.id.logo);

        // Tạo hiệu ứng phóng to cho logo
        Animation scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_animation);
        logo.startAnimation(scaleAnimation);

        // Chuyển đến màn hình đăng nhập sau 3 giây
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            // Hiệu ứng chuyển màn hình
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }, 3000);
    }
}