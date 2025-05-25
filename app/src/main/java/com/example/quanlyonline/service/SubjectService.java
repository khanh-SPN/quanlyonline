package com.example.quanlyonline.service;

import com.example.quanlyonline.model.Subject;
import com.example.quanlyonline.repository.SubjectRepository;

import java.util.List;

public class SubjectService {
    private SubjectRepository subjectRepository;

    public SubjectService() {
        subjectRepository = new SubjectRepository();
    }

    public void getAllSubjects(SubjectRepository.OnSubjectsLoadedListener listener) {
        subjectRepository.getAllSubjects(listener);
    }

    public void addSubject(Subject subject, SubjectRepository.OnOperationListener listener) {
        if (subject.getName() == null || subject.getName().isEmpty()) {
            listener.onFailure("Tên môn học không được để trống");
            return;
        }
        if (subject.getClassId() == null || subject.getClassId().isEmpty()) {
            listener.onFailure("ID lớp không được để trống");
            return;
        }
        subjectRepository.addSubject(subject, listener);
    }

    public void updateSubject(Subject subject, SubjectRepository.OnOperationListener listener) {
        if (subject.getName() == null || subject.getName().isEmpty()) {
            listener.onFailure("Tên môn học không được để trống");
            return;
        }
        if (subject.getClassId() == null || subject.getClassId().isEmpty()) {
            listener.onFailure("ID lớp không được để trống");
            return;
        }
        subjectRepository.updateSubject(subject, listener);
    }

    public void deleteSubject(String subjectId, String classId, SubjectRepository.OnOperationListener listener) {
        subjectRepository.deleteSubject(subjectId, classId, listener);
    }
}