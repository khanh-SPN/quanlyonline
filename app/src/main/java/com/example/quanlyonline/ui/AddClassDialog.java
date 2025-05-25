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
import com.example.quanlyonline.controller.TeacherController;
import com.example.quanlyonline.model.Class;
import com.example.quanlyonline.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddClassDialog extends DialogFragment {

    private ClassController classController;
    private TeacherController teacherController;
    private Class classObj;
    private EditText nameInput;
    private Spinner teacherSpinner;
    private Button saveButton;
    private OnClassSavedListener listener;
    private List<User> teacherList;
    private List<String> teacherNames;
    private List<String> teacherIds;

    public AddClassDialog(ClassController classController, TeacherController teacherController, Class classObj, OnClassSavedListener listener) {
        this.classController = classController;
        this.teacherController = teacherController;
        this.classObj = classObj;
        this.listener = listener;
        this.teacherList = new ArrayList<>();
        this.teacherNames = new ArrayList<>();
        this.teacherIds = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_class_admin, container, false);

        nameInput = view.findViewById(R.id.class_name_input);
        teacherSpinner = view.findViewById(R.id.teacher_id_input);
        saveButton = view.findViewById(R.id.save_button);

        Animation slideUp = AnimationUtils.loadAnimation(getContext(), R.anim.slide_up);
        view.startAnimation(slideUp);

        // Load danh sách giáo viên
        teacherController.getAllTeachers(new TeacherController.OnTeachersResultListener() {
            @Override
            public void onSuccess(List<User> teachers) {
                teacherList.clear();
                teacherList.addAll(teachers);
                teacherNames.clear();
                teacherIds.clear();
                for (User teacher : teachers) {
                    teacherNames.add(teacher.getFullName() + " (" + teacher.getUserId() + ")");
                    teacherIds.add(teacher.getUserId());
                }
                ArrayAdapter<String> teacherAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, teacherNames);
                teacherAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                teacherSpinner.setAdapter(teacherAdapter);

                if (classObj != null) {
                    nameInput.setText(classObj.getName());
                    for (int i = 0; i < teacherIds.size(); i++) {
                        if (teacherIds.get(i).equals(classObj.getTeacherId())) {
                            teacherSpinner.setSelection(i);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(getContext(), "Không thể tải danh sách giáo viên: " + error, Toast.LENGTH_SHORT).show();
            }
        });

        saveButton.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.scale_animation));
            String name = nameInput.getText().toString().trim();
            int selectedPosition = teacherSpinner.getSelectedItemPosition();
            if (selectedPosition == -1) {
                Toast.makeText(getContext(), "Vui lòng chọn giáo viên", Toast.LENGTH_SHORT).show();
                return;
            }
            String teacherId = teacherIds.get(selectedPosition);

            Class newClass = classObj != null ? classObj : new Class();
            newClass.setName(name);
            newClass.setTeacherId(teacherId);

            // Không thêm học sinh giả lập nữa
            Map<String, Boolean> subjects = new HashMap<>();
            String[] subjectList = {"math", "literature", "english", "physics", "chemistry", "biology", "history", "geography", "civic_education"};
            for (String subject : subjectList) {
                subjects.put(subject, true);
            }
            newClass.setSubjects(subjects);

            if (classObj == null) {
                classController.addClass(newClass, new ClassController.OnOperationResultListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getContext(), "Thêm lớp thành công", Toast.LENGTH_SHORT).show();
                        listener.onClassSaved();
                        dismiss();
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                classController.updateClass(newClass, new ClassController.OnOperationResultListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getContext(), "Cập nhật lớp thành công", Toast.LENGTH_SHORT).show();
                        listener.onClassSaved();
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

    public interface OnClassSavedListener {
        void onClassSaved();
    }
}