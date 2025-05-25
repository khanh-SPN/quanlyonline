package com.example.quanlyonline.service;

import com.example.quanlyonline.model.Student;
import com.example.quanlyonline.repository.StudentRepository;

import java.util.List;

public class StudentService {
    private StudentRepository studentRepository;

    public StudentService() {
        studentRepository = new StudentRepository();
    }

    public void getStudentsByClass(String classId, StudentRepository.OnStudentsLoadedListener listener) {
        studentRepository.getStudentsByClass(classId, listener);
    }

    public void addStudent(String classId, Student student, StudentRepository.OnOperationListener listener) {
        if (student.getFullName() == null || student.getFullName().isEmpty()) {
            listener.onFailure("Tên học sinh không được để trống");
            return;
        }
        studentRepository.addStudent(classId, student, listener);
    }

    public void updateStudent(Student student, StudentRepository.OnOperationListener listener) {
        if (student.getFullName() == null || student.getFullName().isEmpty()) {
            listener.onFailure("Tên học sinh không được để trống");
            return;
        }
        studentRepository.updateStudent(student, listener);
    }

    public void deleteStudent(String classId, String studentId, StudentRepository.OnOperationListener listener) {
        studentRepository.deleteStudent(classId, studentId, listener);
    }
}