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
import com.example.quanlyonline.controller.ClassController;
import com.example.quanlyonline.controller.SubjectController;
import com.example.quanlyonline.model.Class;
import com.example.quanlyonline.model.Subject;

import java.util.ArrayList;
import java.util.List;

public class AddSubjectDialog extends DialogFragment {

    private SubjectController subjectController;
    private ClassController classController;
    private Subject subject;
    private EditText nameInput;
    private Spinner classSpinner;
    private Button saveButton;
    private OnSubjectSavedListener listener;
    private List<Class> classList;
    private List<String> classNames;
    private List<String> classIds;

    public AddSubjectDialog(SubjectController subjectController, ClassController classController, Subject subject, OnSubjectSavedListener listener) {
        this.subjectController = subjectController;
        this.classController = classController;
        this.subject = subject;
        this.listener = listener;
        this.classList = new ArrayList<>();
        this.classNames = new ArrayList<>();
        this.classIds = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_subject_admin, container, false);

        nameInput = view.findViewById(R.id.subject_name_input);
        classSpinner = view.findViewById(R.id.subject_class_id_input);
        saveButton = view.findViewById(R.id.save_button);

        Animation slideUp = AnimationUtils.loadAnimation(getContext(), R.anim.slide_up);
        view.startAnimation(slideUp);

        // Load danh sách lớp
        classController.getAllClasses(new ClassController.OnClassesResultListener() {
            @Override
            public void onSuccess(List<Class> classes) {
                classList.clear();
                classList.addAll(classes);
                classNames.clear();
                classIds.clear();
                for (Class classObj : classes) {
                    classNames.add(classObj.getName() + " (" + classObj.getClassId() + ")");
                    classIds.add(classObj.getClassId());
                }
                ArrayAdapter<String> classAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, classNames);
                classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                classSpinner.setAdapter(classAdapter);

                if (subject != null) {
                    nameInput.setText(subject.getName());
                    for (int i = 0; i < classIds.size(); i++) {
                        if (classIds.get(i).equals(subject.getClassId())) {
                            classSpinner.setSelection(i);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(getContext(), "Không thể tải danh sách lớp: " + error, Toast.LENGTH_SHORT).show();
            }
        });

        saveButton.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.scale_animation));
            String name = nameInput.getText().toString().trim();
            int selectedPosition = classSpinner.getSelectedItemPosition();
            if (selectedPosition == -1) {
                Toast.makeText(getContext(), "Vui lòng chọn lớp", Toast.LENGTH_SHORT).show();
                return;
            }
            String classId = classIds.get(selectedPosition);

            Subject newSubject = subject != null ? subject : new Subject();
            newSubject.setName(name);
            newSubject.setClassId(classId);

            if (subject == null) {
                subjectController.addSubject(newSubject, new SubjectController.OnOperationResultListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getContext(), "Thêm môn học thành công", Toast.LENGTH_SHORT).show();
                        listener.onSubjectSaved();
                        dismiss();
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                subjectController.updateSubject(newSubject, new SubjectController.OnOperationResultListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getContext(), "Cập nhật môn học thành công", Toast.LENGTH_SHORT).show();
                        listener.onSubjectSaved();
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

    public interface OnSubjectSavedListener {
        void onSubjectSaved();
    }
}