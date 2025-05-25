package com.example.quanlyonline.service;

import com.example.quanlyonline.model.Schedule;
import com.example.quanlyonline.repository.ScheduleRepository;

import java.util.List;

public class ScheduleService {
    private ScheduleRepository scheduleRepository;

    public ScheduleService() {
        scheduleRepository = new ScheduleRepository();
    }

    public void getSchedulesByClass(String className, ScheduleRepository.OnSchedulesLoadedListener listener) {
        scheduleRepository.getSchedulesByClass(className, listener);
    }

    public void addSchedule(String className, Schedule schedule, ScheduleRepository.OnOperationListener listener) {
        if (schedule.getSubject().isEmpty() || schedule.getDate().isEmpty() || schedule.getTime().isEmpty()) {
            listener.onFailure("Vui lòng nhập đầy đủ thông tin");
            return;
        }
        scheduleRepository.addSchedule(className, schedule, listener);
    }

    public void updateSchedule(String className, Schedule schedule, ScheduleRepository.OnOperationListener listener) {
        if (schedule.getSubject().isEmpty() || schedule.getDate().isEmpty() || schedule.getTime().isEmpty()) {
            listener.onFailure("Vui lòng nhập đầy đủ thông tin");
            return;
        }
        scheduleRepository.updateSchedule(className, schedule, listener);
    }
}