package com.example.quanlyonline.controller;

import com.example.quanlyonline.model.Subject;
import com.example.quanlyonline.repository.SubjectRepository;
import com.example.quanlyonline.service.SubjectService;

import java.util.List;

public class SubjectController {
    private SubjectService subjectService;

    public SubjectController() {
        subjectService = new SubjectService();
    }

    public void getAllSubjects(OnSubjectsResultListener listener) {
        subjectService.getAllSubjects(new SubjectRepository.OnSubjectsLoadedListener() {
            @Override
            public void onSuccess(List<Subject> subjects) {
                listener.onSuccess(subjects);
            }

            @Override
            public void onFailure(String error) {
                listener.onFailure(error);
            }
        });
    }

    public void addSubject(Subject subject, OnOperationResultListener listener) {
        subjectService.addSubject(subject, new SubjectRepository.OnOperationListener() {
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

    public void updateSubject(Subject subject, OnOperationResultListener listener) {
        subjectService.updateSubject(subject, new SubjectRepository.OnOperationListener() {
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

    public void deleteSubject(String subjectId, String classId, OnOperationResultListener listener) {
        subjectService.deleteSubject(subjectId, classId, new SubjectRepository.OnOperationListener() {
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

    public interface OnSubjectsResultListener {
        void onSuccess(List<Subject> subjects);
        void onFailure(String error);
    }

    public interface OnOperationResultListener {
        void onSuccess();
        void onFailure(String error);
    }
}