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
import com.example.quanlyonline.controller.ClassController;
import com.example.quanlyonline.controller.TeacherController;
import com.example.quanlyonline.model.User;
import com.example.quanlyonline.ui.TeacherAdapter;
import com.example.quanlyonline.ui.AddTeacherDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class TeacherListFragment extends Fragment implements TeacherAdapter.OnTeacherClickListener, TeacherAdapter.OnTeacherDeleteListener {

    private RecyclerView recyclerView;
    private TeacherAdapter adapter;
    private TeacherController teacherController;
    private ClassController classController;
    private List<User> teacherList;
    private FloatingActionButton fabAdd;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        String role = requireActivity().getIntent().getStringExtra("role");
        if (role == null || !role.equals("admin")) {
            Toast.makeText(getContext(), "Bạn không có quyền truy cập chức năng này", Toast.LENGTH_SHORT).show();
            requireActivity().finish();
            return null;
        }

        View view = inflater.inflate(R.layout.fragment_teacher_list_admin, container, false);

        recyclerView = view.findViewById(R.id.teacher_recycler_view);
        fabAdd = view.findViewById(R.id.fab_add_teacher);
        progressBar = view.findViewById(R.id.progress_bar);
        teacherController = new TeacherController();
        classController = new ClassController();
        teacherList = new ArrayList<>();

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TeacherAdapter(teacherList, this, this);
        recyclerView.setAdapter(adapter);

        loadTeachers();

        fabAdd.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.scale_animation));
            showAddTeacherModal(null);
        });

        return view;
    }

    private void loadTeachers() {
        progressBar.setVisibility(View.VISIBLE);
        teacherController.getAllTeachers(new TeacherController.OnTeachersResultListener() {
            @Override
            public void onSuccess(List<User> teachers) {
                teacherList.clear();
                teacherList.addAll(teachers);
                adapter.updateTeachers(teacherList);
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
    public void onTeacherClick(User teacher) {
        showAddTeacherModal(teacher);
    }

    @Override
    public void onTeacherDelete(User teacher) {
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa giáo viên " + teacher.getFullName() + " không?")
                .setPositiveButton("Có", (dialog, which) -> {
                    teacherController.deleteTeacher(teacher.getUserId(), new TeacherController.OnOperationResultListener() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(getContext(), "Xóa giáo viên thành công", Toast.LENGTH_SHORT).show();
                            loadTeachers();
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

    private void showAddTeacherModal(User teacher) {
        AddTeacherDialog dialog = new AddTeacherDialog(teacherController, classController, teacher, () -> loadTeachers());
        dialog.show(getParentFragmentManager(), "AddTeacherDialog");
    }
}