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
import com.example.quanlyonline.model.User;

import java.util.List;

public class TeacherAdapter extends RecyclerView.Adapter<TeacherAdapter.TeacherViewHolder> {
    private List<User> teachers;
    private OnTeacherClickListener clickListener;
    private OnTeacherDeleteListener deleteListener;

    public TeacherAdapter(List<User> teachers, OnTeacherClickListener clickListener, OnTeacherDeleteListener deleteListener) {
        this.teachers = teachers;
        this.clickListener = clickListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public TeacherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_teacher_admin, parent, false);
        return new TeacherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeacherViewHolder holder, int position) {
        User teacher = teachers.get(position);
        holder.nameText.setText(teacher.getFullName());
        holder.usernameText.setText("Tên đăng nhập: " + teacher.getUsername());
        holder.classManagedText.setText("Lớp quản lý: " + teacher.getClassManaged());
        holder.itemView.setAnimation(AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.fade_in));
        holder.itemView.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.scale_animation));
            clickListener.onTeacherClick(teacher);
        });
        holder.deleteButton.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.scale_animation));
            deleteListener.onTeacherDelete(teacher);
        });
    }

    @Override
    public int getItemCount() {
        return teachers.size();
    }

    public void updateTeachers(List<User> newTeachers) {
        this.teachers = newTeachers;
        notifyDataSetChanged();
    }

    static class TeacherViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, usernameText, classManagedText;
        ImageButton deleteButton;

        public TeacherViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.teacher_name);
            usernameText = itemView.findViewById(R.id.teacher_username);
            classManagedText = itemView.findViewById(R.id.teacher_class_managed);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }

    public interface OnTeacherClickListener {
        void onTeacherClick(User teacher);
    }

    public interface OnTeacherDeleteListener {
        void onTeacherDelete(User teacher);
    }
}