package com.example.quanlyonline.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quanlyonline.R;
import com.example.quanlyonline.model.Student;

import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {
    private List<Student> students;
    private OnStudentClickListener clickListener;
    private OnStudentDeleteListener deleteListener;

    public StudentAdapter(List<Student> students, OnStudentClickListener clickListener, OnStudentDeleteListener deleteListener) {
        this.students = students;
        this.clickListener = clickListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student_admin, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        Student student = students.get(position);
        holder.studentIdText.setText("Mã SV: " + student.getStudentId());
        holder.nameText.setText(student.getFullName());
        holder.classText.setText("Lớp: " + student.getClassName());
        holder.dobText.setText("Ngày sinh: " + student.getDateOfBirth());
        holder.addressText.setText("Địa chỉ: " + student.getAddress());
        holder.itemView.setOnClickListener(v -> clickListener.onStudentClick(student));
        holder.deleteButton.setOnClickListener(v -> deleteListener.onStudentDelete(student));
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    public void updateStudents(List<Student> newStudents) {
        this.students = newStudents;
        notifyDataSetChanged();
    }

    static class StudentViewHolder extends RecyclerView.ViewHolder {
        TextView studentIdText, nameText, classText, dobText, addressText;
        ImageButton deleteButton;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            studentIdText = itemView.findViewById(R.id.student_id);
            nameText = itemView.findViewById(R.id.student_name);
            classText = itemView.findViewById(R.id.student_class);
            dobText = itemView.findViewById(R.id.student_dob);
            addressText = itemView.findViewById(R.id.student_address);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }

    public interface OnStudentClickListener {
        void onStudentClick(Student student);
    }

    public interface OnStudentDeleteListener {
        void onStudentDelete(Student student);
    }
}