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
import com.example.quanlyonline.controller.SubjectController;
import com.example.quanlyonline.model.Subject;
import com.example.quanlyonline.ui.SubjectAdapter;
import com.example.quanlyonline.ui.AddSubjectDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class SubjectListFragment extends Fragment implements SubjectAdapter.OnSubjectClickListener, SubjectAdapter.OnSubjectDeleteListener {

    private RecyclerView recyclerView;
    private SubjectAdapter adapter;
    private SubjectController subjectController;
    private ClassController classController;
    private List<Subject> subjectList;
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

        View view = inflater.inflate(R.layout.fragment_subject_list_admin, container, false);

        recyclerView = view.findViewById(R.id.subject_recycler_view);
        fabAdd = view.findViewById(R.id.fab_add_subject);
        progressBar = view.findViewById(R.id.progress_bar);
        subjectController = new SubjectController();
        classController = new ClassController();
        subjectList = new ArrayList<>();

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SubjectAdapter(subjectList, this, this);
        recyclerView.setAdapter(adapter);

        loadSubjects();

        fabAdd.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.scale_animation));
            showAddSubjectModal(null);
        });

        return view;
    }

    private void loadSubjects() {
        progressBar.setVisibility(View.VISIBLE);
        subjectController.getAllSubjects(new SubjectController.OnSubjectsResultListener() {
            @Override
            public void onSuccess(List<Subject> subjects) {
                subjectList.clear();
                subjectList.addAll(subjects);
                adapter.updateSubjects(subjectList);
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
    public void onSubjectClick(Subject subject) {
        showAddSubjectModal(subject);
    }

    @Override
    public void onSubjectDelete(Subject subject) {
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa môn học " + subject.getName() + " không?")
                .setPositiveButton("Có", (dialog, which) -> {
                    subjectController.deleteSubject(subject.getSubjectId(), subject.getClassId(), new SubjectController.OnOperationResultListener() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(getContext(), "Xóa môn học thành công", Toast.LENGTH_SHORT).show();
                            loadSubjects();
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

    private void showAddSubjectModal(Subject subject) {
        AddSubjectDialog dialog = new AddSubjectDialog(subjectController, classController, subject, () -> loadSubjects());
        dialog.show(getParentFragmentManager(), "AddSubjectDialog");
    }
}