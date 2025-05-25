package com.example.quanlyonline.service;

import com.example.quanlyonline.model.User;
import com.example.quanlyonline.repository.StatisticsRepository;

import java.util.List;

public class StatisticsService {
    private StatisticsRepository statisticsRepository;

    public StatisticsService() {
        statisticsRepository = new StatisticsRepository();
    }

    public void getStatisticsByClass(String classId, StatisticsRepository.OnStatisticsLoadedListener listener) {
        statisticsRepository.getStatisticsByClass(classId, listener);
    }
}