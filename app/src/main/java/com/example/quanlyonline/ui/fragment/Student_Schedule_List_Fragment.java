package com.example.quanlyonline.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quanlyonline.R;
import com.example.quanlyonline.controller.ScheduleController;
import com.example.quanlyonline.model.Schedule;
import com.example.quanlyonline.ui.ScheduleAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Student_Schedule_List_Fragment extends Fragment {

    private RecyclerView recyclerView;
    private ScheduleAdapter adapter;
    private ScheduleController scheduleController;
    private List<Schedule> scheduleList;
    private ProgressBar progressBar;
    private TextView noSchedulesText;
    private String classId;
    private String userRole;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        String role = requireActivity().getIntent().getStringExtra("role");
        if (role == null || (!role.equals("teacher") && !role.equals("student"))) {
            Toast.makeText(getContext(), "Bạn không có quyền truy cập chức năng này", Toast.LENGTH_SHORT).show();
            requireActivity().finish();
            return null;
        }
        userRole = role;

        View view = inflater.inflate(R.layout.fragment_student_schedule_list_fragment, container, false);

        recyclerView = view.findViewById(R.id.schedule_recycler_view);
        progressBar = view.findViewById(R.id.progress_bar);
        noSchedulesText = view.findViewById(R.id.no_schedules_text);
        scheduleController = new ScheduleController();
        scheduleList = new ArrayList<>();

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ScheduleAdapter(scheduleList, userRole.equals("teacher") ? this::onScheduleClick : null);
        recyclerView.setAdapter(adapter);

        // Ẩn nút thêm lịch nếu là học sinh
        if (userRole.equals("student")) {
            View fabAdd = view.findViewById(R.id.fab_add_schedule);
            if (fabAdd != null) {
                fabAdd.setVisibility(View.GONE);
            }
        }

        // Lấy classId từ Firebase
        String userId = requireActivity().getIntent().getStringExtra("user_id");
        if (userId == null) {
            Toast.makeText(getContext(), "Không tìm thấy thông tin người dùng. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            getParentFragmentManager().popBackStack();
            return view;
        }

        DatabaseReference usersRef = FirebaseDatabase.getInstance("https://quanlyonline-1c06a-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("users");
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    classId = snapshot.child("class_id").child("value").getValue(String.class);
                    if (classId == null) {
                        Toast.makeText(getContext(), "Không tìm thấy thông tin lớp", Toast.LENGTH_LONG).show();
                        getParentFragmentManager().popBackStack();
                        return;
                    }
                    loadSchedules();
                } else {
                    Toast.makeText(getContext(), "Không tìm thấy thông tin người dùng", Toast.LENGTH_LONG).show();
                    getParentFragmentManager().popBackStack();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Lỗi khi lấy thông tin người dùng: " + error.getMessage(), Toast.LENGTH_LONG).show();
                getParentFragmentManager().popBackStack();
            }
        });

        return view;
    }

    private void loadSchedules() {
        progressBar.setVisibility(View.VISIBLE);
        scheduleController.getSchedulesByClass(classId, new ScheduleController.OnSchedulesResultListener() {
            @Override
            public void onSuccess(List<Schedule> schedules) {
                scheduleList.clear();
                scheduleList.addAll(schedules);
                Collections.sort(scheduleList, (s1, s2) -> s1.getDate().compareTo(s2.getDate()));
                adapter.updateSchedules(scheduleList);
                if (scheduleList.isEmpty()) {
                    noSchedulesText.setVisibility(View.VISIBLE);
                } else {
                    noSchedulesText.setVisibility(View.GONE);
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(getContext(), "Tải lịch học thất bại: " + error, Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void onScheduleClick(Schedule schedule) {
        // Chỉ giáo viên mới có quyền chỉnh sửa lịch học
        // (Đã xử lý bằng cách ẩn nút chỉnh sửa cho học sinh)
    }
}