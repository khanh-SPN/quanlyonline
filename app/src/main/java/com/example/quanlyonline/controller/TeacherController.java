package com.example.quanlyonline.controller;

import com.example.quanlyonline.model.User;
import com.example.quanlyonline.repository.TeacherRepository;
import com.example.quanlyonline.service.TeacherService;

import java.util.List;

public class TeacherController {
    private TeacherService teacherService;

    public TeacherController() {
        teacherService = new TeacherService();
    }

    public void getAllTeachers(OnTeachersResultListener listener) {
        teacherService.getAllTeachers(new TeacherRepository.OnTeachersLoadedListener() {
            @Override
            public void onSuccess(List<User> teachers) {
                listener.onSuccess(teachers);
            }

            @Override
            public void onFailure(String error) {
                listener.onFailure(error);
            }
        });
    }

    public void addTeacher(User teacher, OnOperationResultListener listener) {
        teacherService.addTeacher(teacher, new TeacherRepository.OnOperationListener() {
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

    public void updateTeacher(User teacher, OnOperationResultListener listener) {
        teacherService.updateTeacher(teacher, new TeacherRepository.OnOperationListener() {
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

    public void deleteTeacher(String teacherId, OnOperationResultListener listener) {
        teacherService.deleteTeacher(teacherId, new TeacherRepository.OnOperationListener() {
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

    public interface OnTeachersResultListener {
        void onSuccess(List<User> teachers);
        void onFailure(String error);
    }

    public interface OnOperationResultListener {
        void onSuccess();
        void onFailure(String error);
    }
}