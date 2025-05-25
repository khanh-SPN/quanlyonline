package com.example.quanlyonline.controller;

import com.example.quanlyonline.model.Class;
import com.example.quanlyonline.repository.ClassRepository;
import com.example.quanlyonline.service.ClassService;

import java.util.List;

public class ClassController {
    private ClassService classService;

    public ClassController() {
        classService = new ClassService();
    }

    public void getAllClasses(OnClassesResultListener listener) {
        classService.getAllClasses(new ClassRepository.OnClassesLoadedListener() {
            @Override
            public void onSuccess(List<Class> classes) {
                listener.onSuccess(classes);
            }

            @Override
            public void onFailure(String error) {
                listener.onFailure(error);
            }
        });
    }

    public void addClass(Class classObj, OnOperationResultListener listener) {
        classService.addClass(classObj, new ClassRepository.OnOperationListener() {
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

    public void updateClass(Class classObj, OnOperationResultListener listener) {
        classService.updateClass(classObj, new ClassRepository.OnOperationListener() {
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

    public void deleteClass(String classId, OnOperationResultListener listener) {
        classService.deleteClass(classId, new ClassRepository.OnOperationListener() {
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

    public interface OnClassesResultListener {
        void onSuccess(List<Class> classes);
        void onFailure(String error);
    }

    public interface OnOperationResultListener {
        void onSuccess();
        void onFailure(String error);
    }
}