package com.example.quanlyonline.controller;

import com.example.quanlyonline.model.Student;
import com.example.quanlyonline.repository.StudentRepository;
import com.example.quanlyonline.service.StudentService;

import java.util.List;

public class StudentController {
    private StudentService studentService;

    public StudentController() {
        studentService = new StudentService();
    }

    public void getStudentsByClass(String classId, OnStudentsResultListener listener) {
        studentService.getStudentsByClass(classId, new StudentRepository.OnStudentsLoadedListener() {
            @Override
            public void onSuccess(List<Student> students) {
                listener.onSuccess(students);
            }

            @Override
            public void onFailure(String error) {
                listener.onFailure(error);
            }
        });
    }

    public void addStudent(String classId, Student student, OnOperationResultListener listener) {
        studentService.addStudent(classId, student, new StudentRepository.OnOperationListener() {
            @Override
            public void onSuccess() {
                listener.onSuccess();
            }

            @Override
            public void onFailure(String error) {
                listener.onFailure(error);
            }
        });
    }

    public void updateStudent(Student student, OnOperationResultListener listener) {
        studentService.updateStudent(student, new StudentRepository.OnOperationListener() {
            @Override
            public void onSuccess() {
                listener.onSuccess();
            }

            @Override
            public void onFailure(String error) {
                listener.onFailure(error);
            }
        });
    }

    public void deleteStudent(String classId, String studentId, OnOperationResultListener listener) {
        studentService.deleteStudent(classId, studentId, new StudentRepository.OnOperationListener() {
            @Override
            public void onSuccess() {
                listener.onSuccess();
            }

            @Override
            public void onFailure(String error) {
                listener.onFailure(error);
            }
        });
    }

    public interface OnStudentsResultListener {
        void onSuccess(List<Student> students);
        void onFailure(String error);
    }

    public interface OnOperationResultListener {
        void onSuccess();
        void onFailure(String error);
    }
}