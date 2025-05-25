package com.example.quanlyonline.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quanlyonline.R;
import com.example.quanlyonline.controller.StudentController;
import com.example.quanlyonline.controller.ClassController;
import com.example.quanlyonline.model.Student;
import com.example.quanlyonline.ui.StudentAdapter;
import com.example.quanlyonline.ui.AddStudentDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class StudentListFragment extends Fragment implements StudentAdapter.OnStudentClickListener, StudentAdapter.OnStudentDeleteListener {

    private RecyclerView recyclerView;
    private StudentAdapter adapter;
    private StudentController studentController;
    private ClassController classController; // Thêm ClassController
    private List<Student> studentList;
    private FloatingActionButton fabAdd;
    private ProgressBar progressBar;
    private String classId = "class_10A1"; // Giả sử admin đang quản lý lớp class_10A1

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        String role = requireActivity().getIntent().getStringExtra("role");
        if (role == null || !role.equals("admin")) {
            Toast.makeText(getContext(), "Bạn không có quyền truy cập chức năng này", Toast.LENGTH_SHORT).show();
            requireActivity().finish();
            return null;
        }

        View view = inflater.inflate(R.layout.fragment_student_list_admin, container, false);

        recyclerView = view.findViewById(R.id.student_recycler_view);
        fabAdd = view.findViewById(R.id.fab_add_student);
        progressBar = view.findViewById(R.id.progress_bar);
        studentController = new StudentController();
        classController = new ClassController(); // Khởi tạo ClassController
        studentList = new ArrayList<>();

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new StudentAdapter(studentList, this, this);
        recyclerView.setAdapter(adapter);

        loadStudents();

        fabAdd.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.scale_animation));
            showAddStudentModal(null);
        });

        return view;
    }

    private void loadStudents() {
        progressBar.setVisibility(View.VISIBLE);
        studentController.getStudentsByClass(classId, new StudentController.OnStudentsResultListener() {
            @Override
            public void onSuccess(List<Student> students) {
                studentList.clear();
                studentList.addAll(students);
                adapter.updateStudents(studentList);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onStudentClick(Student student) {
        showAddStudentModal(student);
    }

    @Override
    public void onStudentDelete(Student student) {
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa học sinh " + student.getFullName() + " không?")
                .setPositiveButton("Có", (dialog, which) -> {
                    studentController.deleteStudent(classId, student.getStudentId(), new StudentController.OnOperationResultListener() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(getContext(), "Xóa học sinh thành công", Toast.LENGTH_SHORT).show();
                            loadStudents();
                        }

                        @Override
                        public void onFailure(String error) {
                            Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Không", null)
                .show();
    }

    private void showAddStudentModal(Student student) {
        // Truyền thêm ClassController vào AddStudentDialog
        AddStudentDialog dialog = new AddStudentDialog(studentController, classController, classId, student, () -> loadStudents());
        dialog.show(getParentFragmentManager(), "AddStudentDialog");
    }
}