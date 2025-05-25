package com.example.quanlyonline.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quanlyonline.R;
import com.example.quanlyonline.controller.ClassController;
import com.example.quanlyonline.controller.ScoreController;
import com.example.quanlyonline.controller.StudentController;
import com.example.quanlyonline.model.Score;
import com.example.quanlyonline.model.Subject;
import com.example.quanlyonline.repository.SubjectRepository;
import com.example.quanlyonline.ui.ScoreAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentScoreFragment extends Fragment {

    private RecyclerView recyclerView;
    private ScoreAdapter adapter;
    private ScoreController scoreController;
    private ClassController classController;
    private StudentController studentController;
    private SubjectRepository subjectRepository;
    private List<ScoreAdapter.ScoreEntry> scoreEntries;
    private ProgressBar progressBar;
    private TextView studentInfoText;
    private TextView averageScoreText;
    private TextView noScoresText;
    private Spinner semesterFilterSpinner;
    private List<String> subjectList;
    private Map<String, String> subjectIdToNameMap;
    private Map<String, String> subjectNameToIdMap;
    private String selectedSemesterFilter = "all";
    private String studentId;
    private String classId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        String role = requireActivity().getIntent().getStringExtra("role");
        if (role == null || !role.equals("student")) {
            Toast.makeText(getContext(), "Bạn không có quyền truy cập chức năng này", Toast.LENGTH_SHORT).show();
            getParentFragmentManager().popBackStack();
            return null;
        }

        View view = inflater.inflate(R.layout.fragment_student_score, container, false);

        recyclerView = view.findViewById(R.id.score_recycler_view);
        progressBar = view.findViewById(R.id.progress_bar);
        studentInfoText = view.findViewById(R.id.student_info_text);
        averageScoreText = view.findViewById(R.id.average_score_text);
        noScoresText = view.findViewById(R.id.no_scores_text);
        semesterFilterSpinner = view.findViewById(R.id.semester_filter_spinner);
        scoreController = new ScoreController();
        classController = new ClassController();
        studentController = new StudentController();
        subjectRepository = new SubjectRepository();
        scoreEntries = new ArrayList<>();
        subjectList = new ArrayList<>();
        subjectIdToNameMap = new HashMap<>();
        subjectNameToIdMap = new HashMap<>();

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ScoreAdapter(scoreEntries, null, null); // Học sinh không có quyền chỉnh sửa
        recyclerView.setAdapter(adapter);

        // Thiết lập Spinner cho học kỳ lọc
        List<String> semesterFilterList = new ArrayList<>();
        semesterFilterList.add("Tất cả");
        semesterFilterList.add("Học kỳ 1");
        semesterFilterList.add("Học kỳ 2");
        ArrayAdapter<String> semesterFilterAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, semesterFilterList);
        semesterFilterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        semesterFilterSpinner.setAdapter(semesterFilterAdapter);

        semesterFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        selectedSemesterFilter = "all";
                        break;
                    case 1:
                        selectedSemesterFilter = "semester_1";
                        break;
                    case 2:
                        selectedSemesterFilter = "semester_2";
                        break;
                }
                filterScores();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Lấy studentId và classId từ Intent
        studentId = requireActivity().getIntent().getStringExtra("user_id");
        if (studentId == null) {
            Toast.makeText(getContext(), "Không tìm thấy thông tin người dùng. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            getParentFragmentManager().popBackStack();
            return view;
        }

        DatabaseReference usersRef = FirebaseDatabase.getInstance("https://quanlyonline-1c06a-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("users");
        usersRef.child(studentId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    classId = snapshot.child("class_id").child("value").getValue(String.class);
                    String fullName = snapshot.child("full_name").child("value").getValue(String.class);
                    String studentCode = snapshot.child("student_id").child("value").getValue(String.class);
                    if (classId == null) {
                        Toast.makeText(getContext(), "Không tìm thấy thông tin lớp của học sinh", Toast.LENGTH_LONG).show();
                        getParentFragmentManager().popBackStack();
                        return;
                    }
                    studentInfoText.setText("Học sinh: " + fullName + " (" + studentCode + ")");
                    loadSubjectsAndScores();
                } else {
                    Toast.makeText(getContext(), "Không tìm thấy thông tin học sinh", Toast.LENGTH_LONG).show();
                    getParentFragmentManager().popBackStack();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Lỗi khi lấy thông tin học sinh: " + error.getMessage(), Toast.LENGTH_LONG).show();
                getParentFragmentManager().popBackStack();
            }
        });

        return view;
    }

    private void loadSubjectsAndScores() {
        // Load danh sách môn học của lớp từ classes
        classController.getAllClasses(new ClassController.OnClassesResultListener() {
            @Override
            public void onSuccess(List<com.example.quanlyonline.model.Class> classes) {
                for (com.example.quanlyonline.model.Class classObj : classes) {
                    if (classObj.getClassId().equals(classId)) {
                        Map<String, Boolean> subjects = classObj.getSubjects();
                        subjectList.clear();
                        for (String subject : subjects.keySet()) {
                            subjectList.add(subject);
                        }
                        break;
                    }
                }

                // Load ánh xạ môn học từ subjects
                subjectRepository.getSubjectsByClass(classId, new SubjectRepository.OnSubjectsLoadedListener() {
                    @Override
                    public void onSuccess(List<Subject> subjects) {
                        subjectIdToNameMap.clear();
                        subjectNameToIdMap.clear();
                        for (Subject subject : subjects) {
                            subjectIdToNameMap.put(subject.getSubjectId(), subject.getName());
                            subjectNameToIdMap.put(subject.getName(), subject.getSubjectId());
                        }
                        loadScores();
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(getContext(), "Không thể tải ánh xạ môn học: " + error, Toast.LENGTH_SHORT).show();
                        loadScores();
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(getContext(), "Không thể tải danh sách môn học: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadScores() {
        progressBar.setVisibility(View.VISIBLE);
        scoreController.getScoresByStudent(classId, studentId, new ScoreController.OnScoresResultListener() {
            @Override
            public void onSuccess(Map<String, Map<String, Score>> scores) {
                scoreEntries.clear();
                double totalScore = 0.0;
                int scoreCount = 0;
                for (Map.Entry<String, Map<String, Score>> subjectEntry : scores.entrySet()) {
                    String subjectId = subjectEntry.getKey();
                    String subjectName = subjectIdToNameMap.getOrDefault(subjectId, subjectId);
                    Map<String, Score> semesterScores = subjectEntry.getValue();
                    for (Map.Entry<String, Score> semesterEntry : semesterScores.entrySet()) {
                        String semester = semesterEntry.getKey();
                        Score score = semesterEntry.getValue();
                        scoreEntries.add(new ScoreAdapter.ScoreEntry(subjectId, subjectName, semester, score));
                        if (score.getScore() != 0.0) {
                            totalScore += score.getScore();
                            scoreCount++;
                        }
                        if (score.getFinalScore() != null) {
                            totalScore += score.getFinalScore();
                            scoreCount++;
                        }
                    }
                }
                // Sắp xếp theo môn học và học kỳ
                Collections.sort(scoreEntries, new Comparator<ScoreAdapter.ScoreEntry>() {
                    @Override
                    public int compare(ScoreAdapter.ScoreEntry o1, ScoreAdapter.ScoreEntry o2) {
                        int subjectCompare = o1.subjectName.compareTo(o2.subjectName);
                        if (subjectCompare != 0) return subjectCompare;
                        return o1.semester.compareTo(o2.semester);
                    }
                });
                filterScores();
                if (scoreCount > 0) {
                    double averageScore = totalScore / scoreCount;
                    averageScoreText.setText("Điểm trung bình: " + String.format("%.2f", averageScore));
                    noScoresText.setVisibility(View.GONE);
                } else {
                    averageScoreText.setText("Điểm trung bình: N/A");
                    noScoresText.setVisibility(View.VISIBLE);
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(getContext(), "Không thể tải danh sách điểm: " + error, Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void filterScores() {
        List<ScoreAdapter.ScoreEntry> filteredEntries = new ArrayList<>();
        for (ScoreAdapter.ScoreEntry entry : scoreEntries) {
            if (selectedSemesterFilter.equals("all") || entry.semester.equals(selectedSemesterFilter)) {
                filteredEntries.add(entry);
            }
        }
        adapter.updateScores(filteredEntries);
    }
}