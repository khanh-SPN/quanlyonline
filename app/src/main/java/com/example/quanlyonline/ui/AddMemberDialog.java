package com.example.quanlyonline.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.example.quanlyonline.R;
import com.example.quanlyonline.controller.GroupChatController;

import java.util.ArrayList;
import java.util.List;

public class AddMemberDialog extends DialogFragment {

    private GroupChatController groupChatController;
    private String groupChatId;
    private List<String> studentIds;
    private List<String> participantIds;
    private List<String> availableStudents;
    private Spinner studentSpinner;
    private Button addButton;
    private OnMemberAddedListener listener;

    public AddMemberDialog(GroupChatController groupChatController, String groupChatId, List<String> studentIds, List<String> participantIds, OnMemberAddedListener listener) {
        this.groupChatController = groupChatController;
        this.groupChatId = groupChatId;
        this.studentIds = studentIds;
        this.participantIds = participantIds;
        this.listener = listener;
        this.availableStudents = new ArrayList<>();
        for (String studentId : studentIds) {
            if (!participantIds.contains(studentId)) {
                availableStudents.add(studentId);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_member, container, false);

        studentSpinner = view.findViewById(R.id.student_spinner);
        addButton = view.findViewById(R.id.add_button);

        if (availableStudents.isEmpty()) {
            Toast.makeText(getContext(), "Không có học sinh nào để thêm", Toast.LENGTH_SHORT).show();
            dismiss();
            return view;
        }

        ArrayAdapter<String> studentAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, availableStudents);
        studentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        studentSpinner.setAdapter(studentAdapter);

        addButton.setOnClickListener(v -> {
            String selectedStudentId = studentSpinner.getSelectedItem().toString();
            groupChatController.addParticipant(groupChatId, selectedStudentId, new GroupChatController.OnOperationResultListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(getContext(), "Thêm thành viên thành công", Toast.LENGTH_SHORT).show();
                    listener.onMemberAdded();
                    dismiss();
                }

                @Override
                public void onFailure(String error) {
                    Toast.makeText(getContext(), "Thêm thành viên thất bại: " + error, Toast.LENGTH_SHORT).show();
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

    public interface OnMemberAddedListener {
        void onMemberAdded();
    }
}