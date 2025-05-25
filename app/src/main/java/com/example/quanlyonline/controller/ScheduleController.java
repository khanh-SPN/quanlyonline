package com.example.quanlyonline.controller;

import com.example.quanlyonline.model.Schedule;
import com.example.quanlyonline.repository.ScheduleRepository;
import com.example.quanlyonline.service.ScheduleService;

import java.util.List;

public class ScheduleController {
    private ScheduleService scheduleService;

    public ScheduleController() {
        scheduleService = new ScheduleService();
    }

    public void getSchedulesByClass(String className, OnSchedulesResultListener listener) {
        scheduleService.getSchedulesByClass(className, new ScheduleRepository.OnSchedulesLoadedListener() {
            @Override
            public void onSuccess(List<Schedule> schedules) {
                listener.onSuccess(schedules);
            }

            @Override
            public void onFailure(String error) {
                listener.onFailure(error);
            }
        });
    }

    public void addSchedule(String className, Schedule schedule, OnOperationResultListener listener) {
        scheduleService.addSchedule(className, schedule, new ScheduleRepository.OnOperationListener() {
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

    public void updateSchedule(String className, Schedule schedule, OnOperationResultListener listener) {
        scheduleService.updateSchedule(className, schedule, new ScheduleRepository.OnOperationListener() {
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

    public interface OnSchedulesResultListener {
        void onSuccess(List<Schedule> schedules);
        void onFailure(String error);
    }

    public interface OnOperationResultListener {
        void onSuccess();
        void onFailure(String error);
    }
}