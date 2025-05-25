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
import com.example.quanlyonline.model.Class;

import java.util.List;

public class ClassAdapter extends RecyclerView.Adapter<ClassAdapter.ClassViewHolder> {
    private List<Class> classes;
    private OnClassClickListener clickListener;
    private OnClassDeleteListener deleteListener;

    public ClassAdapter(List<Class> classes, OnClassClickListener clickListener, OnClassDeleteListener deleteListener) {
        this.classes = classes;
        this.clickListener = clickListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ClassViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_class_admin, parent, false);
        return new ClassViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClassViewHolder holder, int position) {
        Class classObj = classes.get(position);
        holder.nameText.setText(classObj.getName());
        holder.teacherText.setText("GV: " + classObj.getTeacherId());
        holder.studentCountText.setText("Số HS: " + classObj.getStudentCount());
        holder.itemView.setAnimation(AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.fade_in));
        holder.itemView.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.scale_animation));
            clickListener.onClassClick(classObj);
        });
        holder.deleteButton.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.scale_animation));
            deleteListener.onClassDelete(classObj);
        });
    }

    @Override
    public int getItemCount() {
        return classes.size();
    }

    public void updateClasses(List<Class> newClasses) {
        this.classes = newClasses;
        notifyDataSetChanged();
    }

    static class ClassViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, teacherText, studentCountText;
        ImageButton deleteButton;

        public ClassViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.class_name);
            teacherText = itemView.findViewById(R.id.class_teacher);
            studentCountText = itemView.findViewById(R.id.class_student_count);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }

    public interface OnClassClickListener {
        void onClassClick(Class classObj);
    }

    public interface OnClassDeleteListener {
        void onClassDelete(Class classObj);
    }
}