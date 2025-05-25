package com.example.quanlyonline.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quanlyonline.R;
import com.example.quanlyonline.model.Subject;

import java.util.List;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder> {
    private List<Subject> subjects;
    private OnSubjectClickListener clickListener;
    private OnSubjectDeleteListener deleteListener;

    public SubjectAdapter(List<Subject> subjects, OnSubjectClickListener clickListener, OnSubjectDeleteListener deleteListener) {
        this.subjects = subjects;
        this.clickListener = clickListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subject_admin, parent, false);
        return new SubjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
        Subject subject = subjects.get(position);
        holder.nameText.setText(subject.getName());
        holder.classText.setText("Lớp: " + subject.getClassId());
        holder.itemView.setAnimation(AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.fade_in));
        holder.itemView.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.scale_animation));
            clickListener.onSubjectClick(subject);
        });
        holder.deleteButton.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.scale_animation));
            deleteListener.onSubjectDelete(subject);
        });
    }

    @Override
    public int getItemCount() {
        return subjects.size();
    }

    public void updateSubjects(List<Subject> newSubjects) {
        this.subjects = newSubjects;
        notifyDataSetChanged();
    }

    static class SubjectViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, classText;
        ImageButton deleteButton;

        public SubjectViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.subject_name);
            classText = itemView.findViewById(R.id.subject_class);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }

    public interface OnSubjectClickListener {
        void onSubjectClick(Subject subject);
    }

    public interface OnSubjectDeleteListener {
        void onSubjectDelete(Subject subject);
    }
}