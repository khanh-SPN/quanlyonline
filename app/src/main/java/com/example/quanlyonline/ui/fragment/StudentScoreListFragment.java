package com.example.quanlyonline.ui.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
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
import com.example.quanlyonline.model.Class;
import com.example.quanlyonline.model.Score;
import com.example.quanlyonline.model.Student;
import com.example.quanlyonline.model.Subject;
import com.example.quanlyonline.repository.SubjectRepository;
import com.example.quanlyonline.ui.ScoreAdapter;
import com.example.quanlyonline.ui.AddScoreDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StudentScoreListFragment extends Fragment implements ScoreAdapter.OnScoreClickListener, ScoreAdapter.OnScoreDeleteListener {

    private static final String ARG_STUDENT_ID = "student_id";
    private static final String ARG_CLASS_ID = "class_id";
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000;

    private String studentId;
    private String classId;
    private String studentName;
    private String studentCode;
    private RecyclerView recyclerView;
    private ScoreAdapter adapter;
    private ScoreController scoreController;
    private ClassController classController;
    private StudentController studentController;
    private SubjectRepository subjectRepository;
    private List<ScoreAdapter.ScoreEntry> scoreEntries;
    private FloatingActionButton fabAdd;
    private ImageButton backButton;
    private ProgressBar progressBar;
    private TextView studentInfoText;
    private TextView averageScoreText;
    private TextView noScoresText;
    private Spinner semesterFilterSpinner;
    private List<String> subjectList;
    private Map<String, String> subjectIdToNameMap; // Ánh xạ từ ID môn học sang tên môn học
    private Map<String, String> subjectNameToIdMap; // Ánh xạ từ tên môn học sang ID môn học
    private String selectedSemesterFilter = "all"; // Mặc định hiển thị tất cả học kỳ
    private int retryCount = 0;
    private DatabaseReference scoresRef;

    public static StudentScoreListFragment newInstance(String studentId, String classId) {
        StudentScoreListFragment fragment = new StudentScoreListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_STUDENT_ID, studentId);
        args.putString(ARG_CLASS_ID, classId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_student_score_list, container, false);

        if (getArguments() != null) {
            studentId = getArguments().getString(ARG_STUDENT_ID);
            classId = getArguments().getString(ARG_CLASS_ID);
        }

        // Kiểm tra studentId không null
        if (studentId == null || classId == null) {
            Toast.makeText(getContext(), "Không thể tải thông tin học sinh: Dữ liệu không hợp lệ", Toast.LENGTH_SHORT).show();
            getParentFragmentManager().popBackStack();
            return view;
        }

        recyclerView = view.findViewById(R.id.score_recycler_view);
        fabAdd = view.findViewById(R.id.fab_add_score);
        backButton = view.findViewById(R.id.back_button);
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
        scoresRef = FirebaseDatabase.getInstance("https://quanlyonline-1c06a-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("scores");

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ScoreAdapter(scoreEntries, this, this);
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

        // Nút quay lại
        backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        // Load thông tin học sinh
        loadStudentInfo();

        fabAdd.setOnClickListener(v -> {
            List<Map<String, String>> availableSubjectSemesters = getAvailableSubjects();
            if (availableSubjectSemesters.isEmpty()) {
                Toast.makeText(getContext(), "Không còn môn học nào để thêm điểm số", Toast.LENGTH_SHORT).show();
                return;
            }
            Map<String, String> firstEntry = availableSubjectSemesters.get(0);
            showAddScoreModal(null, firstEntry.get("subjectId"), firstEntry.get("semester"));
        });

        return view;
    }

    private void loadStudentInfo() {
        progressBar.setVisibility(View.VISIBLE);
        studentController.getStudentsByClass(classId, new StudentController.OnStudentsResultListener() {
            @Override
            public void onSuccess(List<Student> students) {
                for (Student student : students) {
                    if (student != null && student.getUserId() != null && student.getUserId().equals(studentId)) {
                        studentName = student.getFullName() != null ? student.getFullName() : "Không xác định";
                        studentCode = student.getStudentId() != null ? student.getStudentId() : "N/A";
                        studentInfoText.setText("Học sinh: " + studentName + " (" + studentCode + ")");
                        // Kiểm tra xem studentId đã tồn tại trong scores chưa
                        scoresRef.child(studentId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (!snapshot.exists()) {
                                    // Nếu chưa tồn tại, tạo key rỗng
                                    scoresRef.child(studentId).setValue(new HashMap<>(), (error, ref) -> {
                                        if (error != null) {
                                            Log.e("StudentScoreList", "Failed to create userId key in scores: " + error.getMessage());
                                        }
                                        loadSubjectsAndScores();
                                    });
                                } else {
                                    // Nếu đã tồn tại, không ghi đè, chỉ load dữ liệu
                                    loadSubjectsAndScores();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e("StudentScoreList", "Failed to check student scores: " + error.getMessage());
                                loadSubjectsAndScores();
                            }
                        });
                        return;
                    }
                }
                // Nếu không tìm thấy học sinh, thử lại
                if (retryCount < MAX_RETRIES) {
                    retryCount++;
                    new Handler(Looper.getMainLooper()).postDelayed(() -> loadStudentInfo(), RETRY_DELAY_MS);
                } else {
                    Toast.makeText(getContext(), "Không tìm thấy thông tin học sinh sau " + MAX_RETRIES + " lần thử", Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().popBackStack();
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(getContext(), "Không thể tải thông tin học sinh: " + error, Toast.LENGTH_SHORT).show();
                getParentFragmentManager().popBackStack();
            }
        });
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
                        // Tiếp tục load scores với ID môn học nếu không lấy được ánh xạ
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

    private List<Map<String, String>> getAvailableSubjects() {
        List<Map<String, String>> availableSubjectSemesters = new ArrayList<>();
        Set<String> scoredSubjects = new HashSet<>();
        for (ScoreAdapter.ScoreEntry entry : scoreEntries) {
            scoredSubjects.add(entry.subjectId + "_" + entry.semester);
        }
        for (String subjectId : subjectList) {
            for (String semester : new String[]{"semester_1", "semester_2"}) {
                if (!scoredSubjects.contains(subjectId + "_" + semester)) {
                    Map<String, String> entry = new HashMap<>();
                    entry.put("subjectId", subjectId);
                    entry.put("semester", semester);
                    availableSubjectSemesters.add(entry);
                }
            }
        }
        return availableSubjectSemesters;
    }

    @Override
    public void onScoreClick(ScoreAdapter.ScoreEntry entry) {
        showAddScoreModal(entry.score, entry.subjectId, entry.semester);
    }

    @Override
    public void onScoreDelete(ScoreAdapter.ScoreEntry entry) {
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa điểm số " + entry.subjectName + " " + (entry.semester.equals("semester_1") ? "Kỳ 1" : "Kỳ 2") + " của học sinh này không?")
                .setPositiveButton("Có", (dialog, which) -> {
                    scoreController.deleteScore(studentId, entry.subjectId, entry.semester, new ScoreController.OnOperationResultListener() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(getContext(), "Xóa điểm số thành công", Toast.LENGTH_SHORT).show();
                            loadScores();
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

    private void showAddScoreModal(Score score, String subjectId, String semester) {
        AddScoreDialog dialog = new AddScoreDialog(scoreController, studentId, subjectId, semester, null, subjectList, subjectIdToNameMap, () -> loadScores());
        dialog.show(getParentFragmentManager(), "AddScoreDialog");
    }
}