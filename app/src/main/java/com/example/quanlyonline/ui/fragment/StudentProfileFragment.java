package com.example.quanlyonline;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.quanlyonline.ui.fragment.Student_Schedule_List_Fragment;
import com.example.quanlyonline.ui.fragment.Student_Score_Fragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StudentProfileFragment extends Fragment {

    private TextView tvName, tvUsername, tvEmail, tvPhone, tvClass;
    private Button btnViewScores, btnViewSchedule;
    private DatabaseReference userRef;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_student_profile, container, false);

        // Khởi tạo các thành phần giao diện
        tvName = view.findViewById(R.id.tv_name);
        tvUsername = view.findViewById(R.id.tv_username);
        tvEmail = view.findViewById(R.id.tv_email);
        tvPhone = view.findViewById(R.id.tv_phone);
        tvClass = view.findViewById(R.id.tv_class);
        btnViewScores = view.findViewById(R.id.btn_view_scores);
        btnViewSchedule = view.findViewById(R.id.btn_view_schedule);

        // Lấy userId từ Intent của Activity
        Intent intent = getActivity().getIntent();
        if (intent != null) {
            userId = intent.getStringExtra("user_id");
        }

        // Kiểm tra userId và tải thông tin từ Firebase
        if (userId != null && !userId.isEmpty()) {
            userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // Ánh xạ dữ liệu từ DataSnapshot
                        String name = snapshot.child("full_name").child("value").getValue(String.class);
                        String username = snapshot.child("username").child("value").getValue(String.class);
                        String email = snapshot.child("email").child("value").getValue(String.class);
                        String phone = snapshot.child("phone").child("value").getValue(String.class);
                        String classId = snapshot.child("class_id").child("value").getValue(String.class);

                        // Cập nhật giao diện với dữ liệu từ Firebase
                        tvName.setText("Tên: " + (name != null ? name : "Không có dữ liệu"));
                        tvUsername.setText("Tên đăng nhập: " + (username != null ? username : "Không có dữ liệu"));
                        tvEmail.setText("Email: " + (email != null ? email : "Không có dữ liệu"));
                        tvPhone.setText("Số điện thoại: " + (phone != null ? phone : "Không có dữ liệu"));
                        tvClass.setText("Lớp học: " + (classId != null ? classId : "Không có dữ liệu"));
                    } else {
                        // Nếu không tìm thấy dữ liệu
                        tvName.setText("Tên: Không tìm thấy dữ liệu");
                        tvUsername.setText("Tên đăng nhập: Không tìm thấy dữ liệu");
                        tvEmail.setText("Email: Không tìm thấy dữ liệu");
                        tvPhone.setText("Số điện thoại: Không tìm thấy dữ liệu");
                        tvClass.setText("Lớp học: Không tìm thấy dữ liệu");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Xử lý lỗi nếu truy vấn Firebase thất bại
                    tvName.setText("Tên: Lỗi tải dữ liệu");
                    tvUsername.setText("Tên đăng nhập: Lỗi tải dữ liệu");
                    tvEmail.setText("Email: Lỗi tải dữ liệu");
                    tvPhone.setText("Số điện thoại: Lỗi tải dữ liệu");
                    tvClass.setText("Lớp học: Lỗi tải dữ liệu");
                }
            });
        } else {
            // Nếu không có userId, hiển thị thông báo lỗi
            tvName.setText("Tên: Không tìm thấy người dùng");
            tvUsername.setText("Tên đăng nhập: Không tìm thấy người dùng");
            tvEmail.setText("Email: Không tìm thấy người dùng");
            tvPhone.setText("Số điện thoại: Không tìm thấy người dùng");
            tvClass.setText("Lớp học: Không tìm thấy người dùng");
        }

        // Xử lý sự kiện nhấn nút "Xem điểm"
        btnViewScores.setOnClickListener(v -> {
            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.fragment_container, new Student_Score_Fragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        // Xử lý sự kiện nhấn nút "Xem lịch học"
        btnViewSchedule.setOnClickListener(v -> {
            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.fragment_container, new Student_Schedule_List_Fragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        return view;
    }
}