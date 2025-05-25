package com.example.quanlyonline.service;

import com.example.quanlyonline.model.User;
import com.example.quanlyonline.repository.TeacherRepository;

import java.util.List;

public class TeacherService {
    private TeacherRepository teacherRepository;

    public TeacherService() {
        teacherRepository = new TeacherRepository();
    }

    public void getAllTeachers(TeacherRepository.OnTeachersLoadedListener listener) {
        teacherRepository.getAllTeachers(listener);
    }

    public void addTeacher(User teacher, TeacherRepository.OnOperationListener listener) {
        if (teacher.getFullName() == null || teacher.getFullName().isEmpty()) {
            listener.onFailure("Tên giáo viên không được để trống");
            return;
        }
        if (teacher.getUsername() == null || teacher.getUsername().isEmpty()) {
            listener.onFailure("Tên đăng nhập không được để trống");
            return;
        }
        teacherRepository.addTeacher(teacher, listener);
    }

    public void updateTeacher(User teacher, TeacherRepository.OnOperationListener listener) {
        if (teacher.getFullName() == null || teacher.getFullName().isEmpty()) {
            listener.onFailure("Tên giáo viên không được để trống");
            return;
        }
        if (teacher.getUsername() == null || teacher.getUsername().isEmpty()) {
            listener.onFailure("Tên đăng nhập không được để trống");
            return;
        }
        teacherRepository.updateTeacher(teacher, listener);
    }

    public void deleteTeacher(String teacherId, TeacherRepository.OnOperationListener listener) {
        teacherRepository.deleteTeacher(teacherId, listener);
    }
}