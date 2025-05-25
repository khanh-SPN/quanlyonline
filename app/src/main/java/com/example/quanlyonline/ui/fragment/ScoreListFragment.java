package com.example.quanlyonline.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quanlyonline.R;
import com.example.quanlyonline.controller.StudentController;
import com.example.quanlyonline.model.Student;
import com.example.quanlyonline.ui.StudentAdapter;

import java.util.ArrayList;
import java.util.List;

public class ScoreListFragment extends Fragment implements StudentAdapter.OnStudentClickListener, StudentAdapter.OnStudentDeleteListener {

    private RecyclerView recyclerView;
    private StudentAdapter adapter;
    private StudentController studentController;
    private List<Student> studentList;
    private List<Student> filteredStudentList;
    private SearchView searchView;
    private ProgressBar progressBar;
    private String classId = "class_10A1"; // Giả sử giáo viên đang quản lý lớp class_10A1

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        String role = requireActivity().getIntent().getStringExtra("role");
        if (role == null || !role.equals("teacher")) {
            Toast.makeText(getContext(), "Bạn không có quyền truy cập chức năng này", Toast.LENGTH_SHORT).show();
            requireActivity().finish();
            return null;
        }

        View view = inflater.inflate(R.layout.fragment_score_list, container, false);

        recyclerView = view.findViewById(R.id.student_recycler_view);
        searchView = view.findViewById(R.id.search_view);
        progressBar = view.findViewById(R.id.progress_bar);
        studentController = new StudentController();
        studentList = new ArrayList<>();
        filteredStudentList = new ArrayList<>();

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new StudentAdapter(filteredStudentList, this, this);
        recyclerView.setAdapter(adapter);

        loadStudents();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterStudents(newText);
                return true;
            }
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
                filteredStudentList.clear();
                filteredStudentList.addAll(students);
                adapter.updateStudents(filteredStudentList);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(getContext(), "Không thể tải danh sách học sinh: " + error, Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void filterStudents(String query) {
        filteredStudentList.clear();
        if (query.isEmpty()) {
            filteredStudentList.addAll(studentList);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Student student : studentList) {
                if (student.getFullName() != null && student.getFullName().toLowerCase().contains(lowerQuery) ||
                        student.getStudentId() != null && student.getStudentId().toLowerCase().contains(lowerQuery)) {
                    filteredStudentList.add(student);
                }
            }
        }
        if (filteredStudentList.isEmpty() && !query.isEmpty()) {
            Toast.makeText(getContext(), "Không tìm thấy học sinh", Toast.LENGTH_SHORT).show();
        }
        adapter.updateStudents(filteredStudentList);
    }

    @Override
    public void onStudentClick(Student student) {
        // Chuyển sang fragment danh sách điểm của học sinh
        StudentScoreListFragment fragment = StudentScoreListFragment.newInstance(student.getUserId(), classId);
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onStudentDelete(Student student) {
        // Xóa học sinh không cần thiết trong chức năng này, có thể bỏ qua
    }
}