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
import com.example.quanlyonline.controller.NotificationController;
import com.example.quanlyonline.model.Notification;
import com.example.quanlyonline.ui.NotificationAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Student_Notification_List_Fragment extends Fragment {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private NotificationController notificationController;
    private List<Notification> notificationList;
    private ProgressBar progressBar;
    private TextView noNotificationsText;
    private String userId;
    private String userRole;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        String role = requireActivity().getIntent().getStringExtra("role");
        if (role == null || (!role.equals("teacher") && !role.equals("student"))) {
            Toast.makeText(requireContext(), "Bạn không có quyền truy cập chức năng này", Toast.LENGTH_SHORT).show();
            getParentFragmentManager().popBackStack();
            return null;
        }
        userRole = role;

        View view = inflater.inflate(R.layout.fragment_student_notification_list_fragment, container, false);

        recyclerView = view.findViewById(R.id.notification_recycler_view);
        progressBar = view.findViewById(R.id.progress_bar);
        noNotificationsText = view.findViewById(R.id.no_notifications_text);
        notificationController = new NotificationController();
        notificationList = new ArrayList<>();

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NotificationAdapter(notificationList);
        recyclerView.setAdapter(adapter);

        // Ẩn nút gửi thông báo nếu là học sinh
        if (userRole.equals("student")) {
            View fabSend = view.findViewById(R.id.fab_send_notification);
            if (fabSend != null) {
                fabSend.setVisibility(View.GONE);
            }
        }

        // Lấy userId từ Intent
        userId = requireActivity().getIntent().getStringExtra("user_id");
        if (userId == null) {
            Toast.makeText(requireContext(), "Không tìm thấy thông tin người dùng. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            getParentFragmentManager().popBackStack();
            return view;
        }

        loadNotifications();
        return view;
    }

    private void loadNotifications() {
        progressBar.setVisibility(View.VISIBLE);
        notificationController.getNotificationsByReceiver(userId, new NotificationController.OnNotificationsResultListener() {
            @Override
            public void onSuccess(List<Notification> notifications) {
                notificationList.clear();
                notificationList.addAll(notifications);
                Collections.sort(notificationList, (n1, n2) -> Long.compare(n2.getTimestamp(), n1.getTimestamp()));
                adapter.notifyDataSetChanged(); // Cập nhật lại adapter
                if (notificationList.isEmpty()) {
                    noNotificationsText.setVisibility(View.VISIBLE);
                } else {
                    noNotificationsText.setVisibility(View.GONE);
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(requireContext(), "Tải thông báo thất bại: " + error, Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}