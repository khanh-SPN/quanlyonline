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
import com.example.quanlyonline.controller.StatisticsController;
import com.example.quanlyonline.model.User;
import com.example.quanlyonline.ui.StatisticsAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class StatisticsFragment extends Fragment {

    private RecyclerView recyclerView;
    private StatisticsAdapter adapter;
    private StatisticsController statisticsController;
    private List<User> studentList;
    private ProgressBar progressBar;
    private TextView classAverageText;
    private String classId;
    private DatabaseReference classesRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        String role = requireActivity().getIntent().getStringExtra("role");
        if (role == null || !role.equals("teacher")) {
            Toast.makeText(getContext(), "Bạn không có quyền truy cập chức năng này", Toast.LENGTH_SHORT).show();
            requireActivity().finish();
            return null;
        }

        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        recyclerView = view.findViewById(R.id.statistics_recycler_view);
        progressBar = view.findViewById(R.id.progress_bar);
        classAverageText = view.findViewById(R.id.class_average_text);
        statisticsController = new StatisticsController();
        studentList = new ArrayList<>();
        classesRef = FirebaseDatabase.getInstance("https://quanlyonline-1c06a-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("classes");

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new StatisticsAdapter(studentList);
        recyclerView.setAdapter(adapter);

        // Lấy userId từ Intent
        String userId = requireActivity().getIntent().getStringExtra("user_id");
        if (userId == null) {
            Toast.makeText(getContext(), "Không tìm thấy thông tin người dùng. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            getParentFragmentManager().popBackStack();
            return view;
        }

        // Tìm classId dựa trên teacher_id
        classesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot classSnapshot : snapshot.getChildren()) {
                    String teacherId = classSnapshot.child("teacher_id").child("value").getValue(String.class);
                    if (teacherId != null && teacherId.equals(userId)) {
                        classId = classSnapshot.getKey();
                        break;
                    }
                }
                if (classId == null) {
                    Toast.makeText(getContext(), "Không tìm thấy lớp do giáo viên " + userId + " quản lý", Toast.LENGTH_LONG).show();
                    getParentFragmentManager().popBackStack();
                    return;
                }

                loadStatistics();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Lỗi khi lấy thông tin lớp: " + error.getMessage(), Toast.LENGTH_LONG).show();
                getParentFragmentManager().popBackStack();
            }
        });

        return view;
    }

    private void loadStatistics() {
        progressBar.setVisibility(View.VISIBLE);
        statisticsController.getStatisticsByClass(classId, new StatisticsController.OnStatisticsResultListener() {
            @Override
            public void onSuccess(List<User> students) {
                studentList.clear();
                studentList.addAll(students);
                adapter.updateStudents(studentList);

                // Tính điểm trung bình của lớp
                double classTotalScore = 0.0;
                int studentWithScoresCount = 0;
                for (User student : studentList) {
                    if (student.getAverageScore() > 0.0) {
                        classTotalScore += student.getAverageScore();
                        studentWithScoresCount++;
                    }
                }
                if (studentWithScoresCount > 0) {
                    double classAverageScore = classTotalScore / studentWithScoresCount;
                    classAverageText.setText("Điểm trung bình lớp: " + String.format("%.2f", classAverageScore));
                } else {
                    classAverageText.setText("Điểm trung bình lớp: N/A");
                }

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(getContext(), "Tải thống kê thất bại: " + error, Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}