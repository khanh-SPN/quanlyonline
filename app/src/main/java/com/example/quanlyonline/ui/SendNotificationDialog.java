package com.example.quanlyonline.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.example.quanlyonline.R;
import com.example.quanlyonline.controller.NotificationController;
import com.example.quanlyonline.model.Notification;

public class SendNotificationDialog extends DialogFragment {

    private NotificationController notificationController;
    private EditText titleInput, contentInput;
    private Spinner receiverSpinner;
    private Button sendButton;
    private OnNotificationSentListener listener;

    public SendNotificationDialog(NotificationController notificationController, OnNotificationSentListener listener) {
        this.notificationController = notificationController;
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_send_notification, container, false);

        titleInput = view.findViewById(R.id.title_input);
        contentInput = view.findViewById(R.id.content_input);
        receiverSpinner = view.findViewById(R.id.receiver_spinner);
        sendButton = view.findViewById(R.id.send_button);

        Animation slideUp = AnimationUtils.loadAnimation(getContext(), R.anim.slide_up);
        view.startAnimation(slideUp);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.receivers, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        receiverSpinner.setAdapter(adapter);

        sendButton.setOnClickListener(v -> {
            String title = titleInput.getText().toString().trim();
            String content = contentInput.getText().toString().trim();
            String receiverId = receiverSpinner.getSelectedItemPosition() == 0 ? "user_002" : "user_003"; // Giả sử

            Notification notification = new Notification();
            notification.setTitle(title);
            notification.setContent(content);
            notification.setSenderId("user_001"); // Giả sử giáo viên là user_001
            notification.setReceiverId(receiverId);
            notification.setRead(false);

            notificationController.sendNotification(receiverId, notification, new NotificationController.OnOperationResultListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(getContext(), "Gửi thông báo thành công", Toast.LENGTH_SHORT).show();
                    listener.onNotificationSent();
                    dismiss();
                }

                @Override
                public void onFailure(String error) {
                    Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                }
            });
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    public interface OnNotificationSentListener {
        void onNotificationSent();
    }
}