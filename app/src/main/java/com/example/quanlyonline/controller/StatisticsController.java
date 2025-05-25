package com.example.quanlyonline.controller;

import com.example.quanlyonline.model.User;
import com.example.quanlyonline.repository.StatisticsRepository;
import com.example.quanlyonline.service.StatisticsService;

import java.util.List;

public class StatisticsController {
    private StatisticsService statisticsService;

    public StatisticsController() {
        statisticsService = new StatisticsService();
    }

    public void getStatisticsByClass(String classId, OnStatisticsResultListener listener) {
        statisticsService.getStatisticsByClass(classId, new StatisticsRepository.OnStatisticsLoadedListener() {
            @Override
            public void onSuccess(List<User> students) {
                listener.onSuccess(students);
            }

            @Override
            public void onFailure(String error) {
                listener.onFailure(error);
            }
        });
    }

    public interface OnStatisticsResultListener {
        void onSuccess(List<User> students);
        void onFailure(String error);
    }
}