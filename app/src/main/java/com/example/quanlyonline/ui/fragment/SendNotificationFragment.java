package com.example.quanlyonline.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quanlyonline.R;
import com.example.quanlyonline.controller.NotificationController;
import com.example.quanlyonline.controller.StudentController;
import com.example.quanlyonline.model.Notification;
import com.example.quanlyonline.model.Student;
import com.example.quanlyonline.ui.NotificationAdapter;

import java.util.ArrayList;
import java.util.List;

public class SendNotificationFragment extends Fragment {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private NotificationController notificationController;
    private StudentController studentController;
    private List<Notification> notificationList;
    private EditText titleInput, contentInput;
    private Spinner receiverSpinner;
    private Button sendButton;
    private ProgressBar progressBar;
    private String classId = "class_10A1"; // Giả sử giáo viên đang quản lý lớp class_10A1
    private List<Student> studentList;
    private List<String> studentNames;
    private List<String> studentIds;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        String role = requireActivity().getIntent().getStringExtra("role");
        if (role == null || !role.equals("teacher")) {
            Toast.makeText(getContext(), "Bạn không có quyền truy cập chức năng này", Toast.LENGTH_SHORT).show();
            requireActivity().finish();
            return null;
        }

        View view = inflater.inflate(R.layout.fragment_send_notification, container, false);

        recyclerView = view.findViewById(R.id.notification_recycler_view);
        titleInput = view.findViewById(R.id.title_input);
        contentInput = view.findViewById(R.id.content_input);
        receiverSpinner = view.findViewById(R.id.receiver_spinner);
        sendButton = view.findViewById(R.id.send_button);
        progressBar = view.findViewById(R.id.progress_bar);
        notificationController = new NotificationController();
        studentController = new StudentController();
        notificationList = new ArrayList<>();
        studentList = new ArrayList<>();
        studentNames = new ArrayList<>();
        studentIds = new ArrayList<>();

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NotificationAdapter(notificationList);
        recyclerView.setAdapter(adapter);

        // Load danh sách học sinh
        studentController.getStudentsByClass(classId, new StudentController.OnStudentsResultListener() {
            @Override
            public void onSuccess(List<Student> students) {
                studentList.clear();
                studentList.addAll(students);
                studentNames.clear();
                studentIds.clear();
                for (Student student : students) {
                    studentNames.add(student.getFullName() + " (" + student.getStudentId() + ")");
                    studentIds.add(student.getUserId());
                }
                ArrayAdapter<String> studentAdapter = new ArrayAdapter<>(getContext(),
                        android.R.layout.simple_spinner_item, studentNames);
                studentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                receiverSpinner.setAdapter(studentAdapter);
                loadNotifications();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(getContext(), "Không thể tải danh sách học sinh: " + error, Toast.LENGTH_SHORT).show();
            }
        });

        sendButton.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.scale_animation));
            String title = titleInput.getText().toString().trim();
            String content = contentInput.getText().toString().trim();
            int selectedPosition = receiverSpinner.getSelectedItemPosition();
            if (selectedPosition == -1) {
                Toast.makeText(getContext(), "Vui lòng chọn người nhận", Toast.LENGTH_SHORT).show();
                return;
            }
            String receiverId = studentIds.get(selectedPosition);

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng điền tiêu đề và nội dung", Toast.LENGTH_SHORT).show();
                return;
            }

            Notification notification = new Notification();
            notification.setSenderId("user_001"); // Giả sử giáo viên là user_001
            notification.setReceiverId(receiverId);
            notification.setTitle(title);
            notification.setContent(content);
            notification.setTimestamp(System.currentTimeMillis());
            notification.setRead(false);

            notificationController.sendNotification(receiverId, notification, new NotificationController.OnOperationResultListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(getContext(), "Gửi thông báo thành công", Toast.LENGTH_SHORT).show();
                    titleInput.setText("");
                    contentInput.setText("");
                    loadNotifications();
                }

                @Override
                public void onFailure(String error) {
                    Toast.makeText(getContext(), "Gửi thông báo thất bại: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        });

        return view;
    }

    private void loadNotifications() {
        progressBar.setVisibility(View.VISIBLE);
        notificationController.getNotificationsByReceiver("user_001", new NotificationController.OnNotificationsResultListener() {
            @Override
            public void onSuccess(List<Notification> notifications) {
                notificationList.clear();
                notificationList.addAll(notifications);
                adapter.updateNotifications(notificationList);
                recyclerView.scrollToPosition(notificationList.size() - 1);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}