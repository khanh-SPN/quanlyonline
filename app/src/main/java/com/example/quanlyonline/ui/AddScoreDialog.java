package com.example.quanlyonline.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.example.quanlyonline.R;
import com.example.quanlyonline.controller.ScoreController;
import com.example.quanlyonline.model.Score;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AddScoreDialog extends DialogFragment {

    private ScoreController scoreController;
    private Score score;
    private String studentId;
    private String subject;
    private String semester;
    private List<String> subjectList;
    private Map<String, String> subjectIdToNameMap; // Ánh xạ từ ID môn học sang tên môn học
    private EditText midtermScoreInput, finalScoreInput, dateInput;
    private Spinner subjectSpinner, semesterSpinner;
    private Button saveButton;
    private OnScoreSavedListener listener;

    public AddScoreDialog(ScoreController scoreController, String studentId, String subject, String semester, String type, List<String> subjectList, Map<String, String> subjectIdToNameMap, OnScoreSavedListener listener) {
        this.scoreController = scoreController;
        this.studentId = studentId;
        this.subject = subject;
        this.semester = semester;
        this.subjectList = subjectList;
        this.subjectIdToNameMap = subjectIdToNameMap;
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_score, container, false);

        midtermScoreInput = view.findViewById(R.id.midterm_score_input);
        finalScoreInput = view.findViewById(R.id.final_score_input);
        dateInput = view.findViewById(R.id.date_input);
        subjectSpinner = view.findViewById(R.id.subject_spinner);
        semesterSpinner = view.findViewById(R.id.semester_spinner);
        saveButton = view.findViewById(R.id.save_button);

        // Chuẩn bị danh sách tên môn học để hiển thị
        List<String> subjectNames = new ArrayList<>();
        for (String subjectId : subjectList) {
            String subjectName = subjectIdToNameMap.getOrDefault(subjectId, subjectId);
            subjectNames.add(subjectName);
        }

        // Thiết lập Spinner cho môn học
        ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, subjectNames);
        subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subjectSpinner.setAdapter(subjectAdapter);

        // Thiết lập Spinner cho học kỳ
        ArrayAdapter<CharSequence> semesterAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.semesters, android.R.layout.simple_spinner_item);
        semesterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        semesterSpinner.setAdapter(semesterAdapter);

        // Tự động lấy ngày hiện tại
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        dateInput.setText(currentDate);
        dateInput.setEnabled(false); // Không cho phép chỉnh sửa ngày

        if (score != null) {
            midtermScoreInput.setText(String.valueOf(score.getScore())); // Điểm giữa kỳ
            finalScoreInput.setText(String.valueOf(score.getFinalScore() != null ? score.getFinalScore() : "")); // Điểm cuối kỳ
            dateInput.setText(score.getDate());
            String subjectName = subjectIdToNameMap.getOrDefault(subject, subject);
            for (int i = 0; i < subjectNames.size(); i++) {
                if (subjectNames.get(i).equals(subjectName)) {
                    subjectSpinner.setSelection(i);
                    break;
                }
            }
            String[] semesters = getResources().getStringArray(R.array.semesters);
            for (int i = 0; i < semesters.length; i++) {
                if (semesters[i].equalsIgnoreCase(semester)) {
                    semesterSpinner.setSelection(i);
                    break;
                }
            }
            subjectSpinner.setEnabled(false); // Không cho phép thay đổi môn khi sửa điểm
            semesterSpinner.setEnabled(false); // Không cho phép thay đổi học kỳ khi sửa điểm
        } else {
            String subjectName = subjectIdToNameMap.getOrDefault(subject, subject);
            for (int i = 0; i < subjectNames.size(); i++) {
                if (subjectNames.get(i).equals(subjectName)) {
                    subjectSpinner.setSelection(i);
                    break;
                }
            }
            String[] semesters = getResources().getStringArray(R.array.semesters);
            for (int i = 0; i < semesters.length; i++) {
                if (semesters[i].equalsIgnoreCase(semester)) {
                    semesterSpinner.setSelection(i);
                    break;
                }
            }
        }

        saveButton.setOnClickListener(v -> {
            String midtermScoreValue = midtermScoreInput.getText().toString().trim();
            String finalScoreValue = finalScoreInput.getText().toString().trim();
            String date = dateInput.getText().toString().trim();
            String selectedSubjectName = subjectSpinner.getSelectedItem() != null ? subjectSpinner.getSelectedItem().toString() : null;
            String selectedSubjectId = null;
            for (Map.Entry<String, String> entry : subjectIdToNameMap.entrySet()) {
                if (entry.getValue().equals(selectedSubjectName)) {
                    selectedSubjectId = entry.getKey();
                    break;
                }
            }

            // Ánh xạ giá trị học kỳ từ Spinner
            String selectedSemesterDisplay = semesterSpinner.getSelectedItem() != null ? semesterSpinner.getSelectedItem().toString() : null;
            String selectedSemester = null;
            if ("Học Kỳ 1".equals(selectedSemesterDisplay)) {
                selectedSemester = "semester_1";
            } else if ("Học Kỳ 2".equals(selectedSemesterDisplay)) {
                selectedSemester = "semester_2";
            } else {
                Toast.makeText(getContext(), "Vui lòng chọn học kỳ hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            Double midtermScore = null;
            Double finalScore = null;

            if (!midtermScoreValue.isEmpty()) {
                try {
                    midtermScore = Double.parseDouble(midtermScoreValue);
                    if (midtermScore < 0 || midtermScore > 10) {
                        Toast.makeText(getContext(), "Điểm giữa kỳ phải nằm trong khoảng từ 0 đến 10", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Điểm giữa kỳ không hợp lệ", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            if (!finalScoreValue.isEmpty()) {
                try {
                    finalScore = Double.parseDouble(finalScoreValue);
                    if (finalScore < 0 || finalScore > 10) {
                        Toast.makeText(getContext(), "Điểm cuối kỳ phải nằm trong khoảng từ 0 đến 10", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Điểm cuối kỳ không hợp lệ", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            if (midtermScore == null && finalScore == null) {
                Toast.makeText(getContext(), "Vui lòng điền ít nhất một loại điểm", Toast.LENGTH_SHORT).show();
                return;
            }

            Score newScore = score != null ? score : new Score();
            if (midtermScore != null) {
                newScore.setScore(midtermScore);
            }
            if (finalScore != null) {
                newScore.setFinalScore(finalScore);
            }
            newScore.setDate(date);
            newScore.setTeacherId("user_001"); // Giả sử giáo viên đăng nhập là user_001

            if (score == null) {
                scoreController.addScore(studentId, selectedSubjectId, selectedSemester, newScore, new ScoreController.OnOperationResultListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getContext(), "Thêm điểm số thành công", Toast.LENGTH_SHORT).show();
                        listener.onScoreSaved();
                        dismiss();
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(getContext(), "Thêm điểm số thất bại: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                scoreController.updateScore(studentId, selectedSubjectId, selectedSemester, newScore, new ScoreController.OnOperationResultListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getContext(), "Cập nhật điểm số thành công", Toast.LENGTH_SHORT).show();
                        listener.onScoreSaved();
                        dismiss();
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(getContext(), "Cập nhật điểm số thất bại: " + error, Toast.LENGTH_SHORT).show();
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

    public interface OnScoreSavedListener {
        void onScoreSaved();
    }
}