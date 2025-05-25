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
import java.util.List;

public class AddTeacherDialog extends DialogFragment {

    private TeacherController teacherController;
    private ClassController classController;
    private User teacher;
    private EditText nameInput, phoneInput, emailInput, usernameInput, passwordInput;
    private Spinner classManagedSpinner;
    private Button saveButton;
    private OnTeacherSavedListener listener;
    private List<Class> classList;
    private List<String> classNames;
    private List<String> classIds;

    public AddTeacherDialog(TeacherController teacherController, ClassController classController, User teacher, OnTeacherSavedListener listener) {
        this.teacherController = teacherController;
        this.classController = classController;
        this.teacher = teacher;
        this.listener = listener;
        this.classList = new ArrayList<>();
        this.classNames = new ArrayList<>();
        this.classIds = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_teacher_admin, container, false);

        nameInput = view.findViewById(R.id.teacher_name_input);
        phoneInput = view.findViewById(R.id.teacher_phone_input);
        emailInput = view.findViewById(R.id.teacher_email_input);
        usernameInput = view.findViewById(R.id.teacher_username_input);
        passwordInput = view.findViewById(R.id.teacher_password_input);
        classManagedSpinner = view.findViewById(R.id.teacher_class_managed_spinner);
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
                // Thêm tùy chọn "Không quản lý lớp"
                classNames.add("Không quản lý lớp");
                classIds.add("");
                for (Class classObj : classes) {
                    classNames.add(classObj.getName() + " (" + classObj.getClassId() + ")");
                    classIds.add(classObj.getClassId());
                }
                ArrayAdapter<String> classAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, classNames);
                classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                classManagedSpinner.setAdapter(classAdapter);

                if (teacher != null) {
                    nameInput.setText(teacher.getFullName());
                    phoneInput.setText(teacher.getPhone());
                    emailInput.setText(teacher.getEmail());
                    usernameInput.setText(teacher.getUsername());
                    passwordInput.setText(teacher.getPassword());
                    String classManaged = teacher.getClassManaged();
                    if (classManaged != null && !classManaged.isEmpty()) {
                        for (int i = 0; i < classIds.size(); i++) {
                            if (classIds.get(i).equals(classManaged)) {
                                classManagedSpinner.setSelection(i);
                                break;
                            }
                        }
                    } else {
                        classManagedSpinner.setSelection(0); // Không quản lý lớp
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
            String phone = phoneInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            int selectedPosition = classManagedSpinner.getSelectedItemPosition();
            String classManaged = classIds.get(selectedPosition);

            if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            User newTeacher = teacher != null ? teacher : new User();
            newTeacher.setUsername(username);
            newTeacher.setPassword(password);
            newTeacher.setRole("teacher");
            newTeacher.setFullName(name);
            newTeacher.setPhone(phone);
            newTeacher.setEmail(email);
            newTeacher.setClassManaged(classManaged.isEmpty() ? null : classManaged);
            newTeacher.setCreatedAt(System.currentTimeMillis());
            newTeacher.setUpdatedAt(System.currentTimeMillis());

            if (teacher == null) {
                teacherController.addTeacher(newTeacher, new TeacherController.OnOperationResultListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getContext(), "Thêm giáo viên thành công", Toast.LENGTH_SHORT).show();
                        listener.onTeacherSaved();
                        dismiss();
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(getContext(), "Thêm giáo viên thất bại: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                teacherController.updateTeacher(newTeacher, new TeacherController.OnOperationResultListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getContext(), "Cập nhật giáo viên thành công", Toast.LENGTH_SHORT).show();
                        listener.onTeacherSaved();
                        dismiss();
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(getContext(), "Cập nhật giáo viên thất bại: " + error, Toast.LENGTH_SHORT).show();
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

    public interface OnTeacherSavedListener {
        void onTeacherSaved();
    }
}