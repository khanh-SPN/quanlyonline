package com.example.quanlyonline.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.quanlyonline.ui.SendNotificationDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class NotificationListFragment extends Fragment {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private NotificationController notificationController;
    private List<Notification> notificationList;
    private FloatingActionButton fabSend;
    private String receiverId = "user_002"; // Giả sử hiển thị thông báo của phụ huynh user_002

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification_list, container, false);

        recyclerView = view.findViewById(R.id.notification_recycler_view);
        fabSend = view.findViewById(R.id.fab_send_notification);
        notificationController = new NotificationController();
        notificationList = new ArrayList<>();

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NotificationAdapter(notificationList);
        recyclerView.setAdapter(adapter);

        loadNotifications();

        fabSend.setOnClickListener(v -> showSendNotificationModal());

        return view;
    }

    private void loadNotifications() {
        notificationController.getNotificationsByReceiver(receiverId, new NotificationController.OnNotificationsResultListener() {
            @Override
            public void onSuccess(List<Notification> notifications) {
                notificationList.clear();
                notificationList.addAll(notifications);
                adapter.updateNotifications(notificationList);
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showSendNotificationModal() {
        SendNotificationDialog dialog = new SendNotificationDialog(notificationController, () -> loadNotifications());
        dialog.show(getParentFragmentManager(), "SendNotificationDialog");
    }
}