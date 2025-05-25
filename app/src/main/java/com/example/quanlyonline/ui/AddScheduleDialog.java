package com.example.quanlyonline.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.example.quanlyonline.R;
import com.example.quanlyonline.controller.ScheduleController;
import com.example.quanlyonline.model.Schedule;

public class AddScheduleDialog extends DialogFragment {

    private ScheduleController scheduleController;
    private Schedule schedule;
    private String className;
    private EditText subjectInput, dateInput, timeInput, descriptionInput;
    private Button saveButton;
    private OnScheduleSavedListener listener;

    public AddScheduleDialog(ScheduleController scheduleController, String className, Schedule schedule, OnScheduleSavedListener listener) {
        this.scheduleController = scheduleController;
        this.className = className;
        this.schedule = schedule;
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_schedule, container, false);

        subjectInput = view.findViewById(R.id.subject_input);
        dateInput = view.findViewById(R.id.date_input);
        timeInput = view.findViewById(R.id.time_input);
        descriptionInput = view.findViewById(R.id.description_input);
        saveButton = view.findViewById(R.id.save_button);

        Animation slideUp = AnimationUtils.loadAnimation(getContext(), R.anim.slide_up);
        view.startAnimation(slideUp);

        if (schedule != null) {
            subjectInput.setText(schedule.getSubject());
            dateInput.setText(schedule.getDate());
            timeInput.setText(schedule.getTime());
            descriptionInput.setText(schedule.getDescription());
        }

        saveButton.setOnClickListener(v -> {
            String subject = subjectInput.getText().toString().trim();
            String date = dateInput.getText().toString().trim();
            String time = timeInput.getText().toString().trim();
            String description = descriptionInput.getText().toString().trim();

            Schedule newSchedule = schedule != null ? schedule : new Schedule();
            newSchedule.setSubject(subject);
            newSchedule.setDate(date);
            newSchedule.setTime(time);
            newSchedule.setDescription(description);
            newSchedule.setClassName(className);
            newSchedule.setTeacherId("user_001"); // Giả sử giáo viên đăng nhập là user_001

            if (schedule == null) {
                scheduleController.addSchedule(className, newSchedule, new ScheduleController.OnOperationResultListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getContext(), "Thêm lịch học thành công", Toast.LENGTH_SHORT).show();
                        listener.onScheduleSaved();
                        dismiss();
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                scheduleController.updateSchedule(className, newSchedule, new ScheduleController.OnOperationResultListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getContext(), "Cập nhật lịch học thành công", Toast.LENGTH_SHORT).show();
                        listener.onScheduleSaved();
                        dismiss();
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
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

    public interface OnScheduleSavedListener {
        void onScheduleSaved();
    }
}