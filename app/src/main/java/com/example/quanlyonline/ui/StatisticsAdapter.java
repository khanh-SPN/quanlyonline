package com.example.quanlyonline.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quanlyonline.R;
import com.example.quanlyonline.model.User;

import java.util.List;

public class StatisticsAdapter extends RecyclerView.Adapter<StatisticsAdapter.StatisticsViewHolder> {
    private List<User> students;

    public StatisticsAdapter(List<User> students) {
        this.students = students;
    }

    @NonNull
    @Override
    public StatisticsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_statistics, parent, false);
        return new StatisticsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StatisticsViewHolder holder, int position) {
        User student = students.get(position);
        holder.nameText.setText(student.getFullName());
        holder.averageScoreText.setText("ĐTB: " + String.format("%.2f", student.getAverageScore()));
        holder.itemView.setAnimation(AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.fade_in));
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    public void updateStudents(List<User> newStudents) {
        this.students = newStudents;
        notifyDataSetChanged();
    }

    static class StatisticsViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, averageScoreText;

        public StatisticsViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.student_name);
            averageScoreText = itemView.findViewById(R.id.average_score);
        }
    }
}