package com.example.quanlyonline.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quanlyonline.R;
import com.example.quanlyonline.model.Score;

import java.util.List;

public class ScoreAdapter extends RecyclerView.Adapter<ScoreAdapter.ScoreViewHolder> {
    private List<ScoreEntry> scores;
    private OnScoreClickListener clickListener;
    private OnScoreDeleteListener deleteListener;

    public ScoreAdapter(List<ScoreEntry> scores, OnScoreClickListener clickListener, OnScoreDeleteListener deleteListener) {
        this.scores = scores;
        this.clickListener = clickListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ScoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_score, parent, false);
        return new ScoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScoreViewHolder holder, int position) {
        ScoreEntry entry = scores.get(position);
        holder.subjectText.setText("Môn: " + entry.subjectName);
        // Ánh xạ chính xác học kỳ từ giá trị trong database
        String semesterDisplay = entry.semester.equals("semester_1") ? "Kỳ 1" : "Kỳ 2";
        holder.semesterText.setText("Học kỳ: " + semesterDisplay);
        holder.scoreText.setText("Điểm Giữa kỳ: " + (entry.score.getScore() != 0.0 ? entry.score.getScore() : "N/A"));
        holder.finalScoreText.setText("Điểm Cuối kỳ: " + (entry.score.getFinalScore() != null ? entry.score.getFinalScore() : "N/A"));
        holder.dateText.setText("Ngày: " + entry.score.getDate());
        holder.itemView.setOnClickListener(v -> clickListener.onScoreClick(entry));
        holder.deleteButton.setOnClickListener(v -> deleteListener.onScoreDelete(entry));
    }

    @Override
    public int getItemCount() {
        return scores.size();
    }

    public void updateScores(List<ScoreEntry> newScores) {
        this.scores = newScores;
        notifyDataSetChanged();
    }

    static class ScoreViewHolder extends RecyclerView.ViewHolder {
        TextView subjectText, semesterText, scoreText, finalScoreText, dateText, typeText;
        ImageButton deleteButton;

        public ScoreViewHolder(@NonNull View itemView) {
            super(itemView);
            subjectText = itemView.findViewById(R.id.subject_text);
            semesterText = itemView.findViewById(R.id.semester_text);
            scoreText = itemView.findViewById(R.id.score_text);
            finalScoreText = itemView.findViewById(R.id.final_score_text);
            dateText = itemView.findViewById(R.id.date_text);
            typeText = itemView.findViewById(R.id.type_text);
            typeText.setVisibility(View.GONE); // Ẩn trường type vì không còn cần thiết
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }

    public static class ScoreEntry {
        public String subjectId; // Lưu ID môn học
        public String subjectName; // Lưu tên môn học để hiển thị
        public String semester;
        public Score score;

        public ScoreEntry(String subjectId, String subjectName, String semester, Score score) {
            this.subjectId = subjectId;
            this.subjectName = subjectName;
            this.semester = semester;
            this.score = score;
        }
    }

    public interface OnScoreClickListener {
        void onScoreClick(ScoreEntry entry);
    }

    public interface OnScoreDeleteListener {
        void onScoreDelete(ScoreEntry entry);
    }
}