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
import com.example.quanlyonline.controller.StudentController;
import com.example.quanlyonline.model.Class;
import com.example.quanlyonline.model.Student;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddStudentDialog extends DialogFragment {

    private StudentController studentController;
    private ClassController classController;
    private Student student;
    private String classId;
    private EditText nameInput, dobInput, addressInput, studentIdInput;
    private Spinner classSpinner;
    private Button saveButton;
    private OnStudentSavedListener listener;
    private List<Class> classList;
    private List<String> classNames;
    private List<String> classIds;

    public AddStudentDialog(StudentController studentController, ClassController classController, String classId, Student student, OnStudentSavedListener listener) {
        this.studentController = studentController;
        this.classController = classController;
        this.classId = classId;
        this.student = student;
        this.listener = listener;
        this.classList = new ArrayList<>();
        this.classNames = new ArrayList<>();
        this.classIds = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_student_admin, container, false);

        nameInput = view.findViewById(R.id.student_name_input);
        dobInput = view.findViewById(R.id.student_dob_input);
        addressInput = view.findViewById(R.id.student_address_input);
        studentIdInput = view.findViewById(R.id.student_id_input);
        classSpinner = view.findViewById(R.id.class_spinner);
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

                if (student != null) {
                    nameInput.setText(student.getFullName());
                    dobInput.setText(student.getDateOfBirth());
                    addressInput.setText(student.getAddress());
                    studentIdInput.setText(student.getStudentId());
                    for (int i = 0; i < classIds.size(); i++) {
                        if (classIds.get(i).equals(student.getClassName())) {
                            classSpinner.setSelection(i);
                            break;
                        }
                    }
                } else if (classId != null) {
                    for (int i = 0; i < classIds.size(); i++) {
                        if (classIds.get(i).equals(classId)) {
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
            String dob = dobInput.getText().toString().trim();
            String address = addressInput.getText().toString().trim();
            String studentId = studentIdInput.getText().toString().trim();
            int selectedPosition = classSpinner.getSelectedItemPosition();
            if (selectedPosition == -1) {
                Toast.makeText(getContext(), "Vui lòng chọn lớp", Toast.LENGTH_SHORT).show();
                return;
            }
            String selectedClassId = classIds.get(selectedPosition);

            if (name.isEmpty() || dob.isEmpty() || address.isEmpty() || studentId.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            Student newStudent = student != null ? student : new Student();
            newStudent.setStudentId(studentId);
            newStudent.setFullName(name);
            newStudent.setDateOfBirth(dob);
            newStudent.setAddress(address);
            newStudent.setClassName(selectedClassId);
            newStudent.setCreatedAt(System.currentTimeMillis());
            newStudent.setUpdatedAt(System.currentTimeMillis());

            if (student == null) {
                studentController.addStudent(selectedClassId, newStudent, new StudentController.OnOperationResultListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getContext(), "Thêm học sinh thành công", Toast.LENGTH_SHORT).show();
                        listener.onStudentSaved();
                        dismiss();
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(getContext(), "Thêm học sinh thất bại: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                studentController.updateStudent(newStudent, new StudentController.OnOperationResultListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getContext(), "Cập nhật học sinh thành công", Toast.LENGTH_SHORT).show();
                        listener.onStudentSaved();
                        dismiss();
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(getContext(), "Cập nhật học sinh thất bại: " + error, Toast.LENGTH_SHORT).show();
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

    public interface OnStudentSavedListener {
        void onStudentSaved();
    }
}