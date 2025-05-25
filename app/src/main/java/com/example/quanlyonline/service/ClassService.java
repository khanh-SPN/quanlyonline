package com.example.quanlyonline.service;

import com.example.quanlyonline.model.Class;
import com.example.quanlyonline.repository.ClassRepository;

import java.util.List;

public class ClassService {
    private ClassRepository classRepository;

    public ClassService() {
        classRepository = new ClassRepository();
    }

    public void getAllClasses(ClassRepository.OnClassesLoadedListener listener) {
        classRepository.getAllClasses(listener);
    }

    public void addClass(Class classObj, ClassRepository.OnOperationListener listener) {
        if (classObj.getName() == null || classObj.getName().isEmpty()) {
            listener.onFailure("Tên lớp không được để trống");
            return;
        }
        if (classObj.getTeacherId() == null || classObj.getTeacherId().isEmpty()) {
            listener.onFailure("Phải chọn giáo viên quản lý lớp");
            return;
        }
        classRepository.addClass(classObj, listener);
    }

    public void updateClass(Class classObj, ClassRepository.OnOperationListener listener) {
        if (classObj.getName() == null || classObj.getName().isEmpty()) {
            listener.onFailure("Tên lớp không được để trống");
            return;
        }
        if (classObj.getTeacherId() == null || classObj.getTeacherId().isEmpty()) {
            listener.onFailure("Phải chọn giáo viên quản lý lớp");
            return;
        }
        classRepository.updateClass(classObj, listener);
    }

    public void deleteClass(String classId, ClassRepository.OnOperationListener listener) {
        classRepository.deleteClass(classId, listener);
    }
}