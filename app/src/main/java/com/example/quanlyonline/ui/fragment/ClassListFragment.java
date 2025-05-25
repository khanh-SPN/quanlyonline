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
import com.example.quanlyonline.model.Class;
import com.example.quanlyonline.ui.ClassAdapter;
import com.example.quanlyonline.ui.AddClassDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ClassListFragment extends Fragment implements ClassAdapter.OnClassClickListener, ClassAdapter.OnClassDeleteListener {

    private RecyclerView recyclerView;
    private ClassAdapter adapter;
    private ClassController classController;
    private TeacherController teacherController;
    private List<Class> classList;
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

        View view = inflater.inflate(R.layout.fragment_class_list_admin, container, false);

        recyclerView = view.findViewById(R.id.class_recycler_view);
        fabAdd = view.findViewById(R.id.fab_add_class);
        progressBar = view.findViewById(R.id.progress_bar);
        classController = new ClassController();
        teacherController = new TeacherController();
        classList = new ArrayList<>();

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ClassAdapter(classList, this, this);
        recyclerView.setAdapter(adapter);

        loadClasses();

        fabAdd.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.scale_animation));
            showAddClassModal(null);
        });

        return view;
    }

    private void loadClasses() {
        progressBar.setVisibility(View.VISIBLE);
        classController.getAllClasses(new ClassController.OnClassesResultListener() {
            @Override
            public void onSuccess(List<Class> classes) {
                classList.clear();
                classList.addAll(classes);
                adapter.updateClasses(classList);
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
    public void onClassClick(Class classObj) {
        showAddClassModal(classObj);
    }

    @Override
    public void onClassDelete(Class classObj) {
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa lớp " + classObj.getName() + " không?")
                .setPositiveButton("Có", (dialog, which) -> {
                    classController.deleteClass(classObj.getClassId(), new ClassController.OnOperationResultListener() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(getContext(), "Xóa lớp thành công", Toast.LENGTH_SHORT).show();
                            loadClasses();
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

    private void showAddClassModal(Class classObj) {
        AddClassDialog dialog = new AddClassDialog(classController, teacherController, classObj, () -> loadClasses());
        dialog.show(getParentFragmentManager(), "AddClassDialog");
    }
}