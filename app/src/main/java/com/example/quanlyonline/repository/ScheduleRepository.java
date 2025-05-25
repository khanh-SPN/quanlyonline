package com.example.quanlyonline.repository;

import android.util.Log;
import com.example.quanlyonline.model.Schedule;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScheduleRepository {
    private static final String TAG = "ScheduleRepository";
    private DatabaseReference schedulesRef;

    public ScheduleRepository() {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://quanlyonline-1c06a-default-rtdb.asia-southeast1.firebasedatabase.app");
        schedulesRef = database.getReference("schedules");
    }

    public void getSchedulesByClass(String classId, OnSchedulesLoadedListener listener) {
        schedulesRef.child(classId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Schedule> schedules = new ArrayList<>();
                for (DataSnapshot scheduleSnapshot : snapshot.getChildren()) {
                    Schedule schedule = new Schedule();
                    schedule.setScheduleId(scheduleSnapshot.getKey());
                    schedule.setSubject(scheduleSnapshot.child("subject").child("value").getValue(String.class));
                    schedule.setDate(scheduleSnapshot.child("date").child("value").getValue(String.class));
                    schedule.setTime(scheduleSnapshot.child("time").child("value").getValue(String.class));
                    schedule.setTeacherId(scheduleSnapshot.child("teacher_id").child("value").getValue(String.class));
                    schedule.setDescription(scheduleSnapshot.child("description").child("value").getValue(String.class));
                    schedule.setClassName(scheduleSnapshot.child("className").child("value").getValue(String.class));
                    schedule.setCreatedAt(scheduleSnapshot.child("created_at").child("value").getValue(Long.class));
                    schedule.setUpdatedAt(scheduleSnapshot.child("updated_at").child("value").getValue(Long.class));
                    schedules.add(schedule);
                }
                listener.onSuccess(schedules);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Failed to load schedules: " + error.getMessage());
                listener.onFailure(error.getMessage());
            }
        });
    }

    public void addSchedule(String classId, Schedule schedule, OnOperationListener listener) {
        String scheduleId = schedulesRef.child(classId).push().getKey();
        schedule.setScheduleId(scheduleId);

        Map<String, Object> scheduleData = new HashMap<>();
        scheduleData.put("subject", new HashMap<String, Object>() {{ put("value", schedule.getSubject()); }});
        scheduleData.put("date", new HashMap<String, Object>() {{ put("value", schedule.getDate()); }});
        scheduleData.put("time", new HashMap<String, Object>() {{ put("value", schedule.getTime()); }});
        scheduleData.put("teacher_id", new HashMap<String, Object>() {{ put("value", schedule.getTeacherId()); }});
        scheduleData.put("description", new HashMap<String, Object>() {{ put("value", schedule.getDescription()); }});
        scheduleData.put("className", new HashMap<String, Object>() {{ put("value", schedule.getClassName()); }});
        scheduleData.put("created_at", new HashMap<String, Object>() {{ put("value", schedule.getCreatedAt()); }});
        scheduleData.put("updated_at", new HashMap<String, Object>() {{ put("value", schedule.getUpdatedAt()); }});

        schedulesRef.child(classId).child(scheduleId).setValue(scheduleData)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public void updateSchedule(String classId, Schedule schedule, OnOperationListener listener) {
        Map<String, Object> scheduleData = new HashMap<>();
        scheduleData.put("subject", new HashMap<String, Object>() {{ put("value", schedule.getSubject()); }});
        scheduleData.put("date", new HashMap<String, Object>() {{ put("value", schedule.getDate()); }});
        scheduleData.put("time", new HashMap<String, Object>() {{ put("value", schedule.getTime()); }});
        scheduleData.put("teacher_id", new HashMap<String, Object>() {{ put("value", schedule.getTeacherId()); }});
        scheduleData.put("description", new HashMap<String, Object>() {{ put("value", schedule.getDescription()); }});
        scheduleData.put("className", new HashMap<String, Object>() {{ put("value", schedule.getClassName()); }});
        scheduleData.put("created_at", new HashMap<String, Object>() {{ put("value", schedule.getCreatedAt()); }});
        scheduleData.put("updated_at", new HashMap<String, Object>() {{ put("value", schedule.getUpdatedAt()); }});

        schedulesRef.child(classId).child(schedule.getScheduleId()).setValue(scheduleData)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public void deleteSchedule(String classId, String scheduleId, OnOperationListener listener) {
        schedulesRef.child(classId).child(scheduleId).removeValue()
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public interface OnSchedulesLoadedListener {
        void onSuccess(List<Schedule> schedules);
        void onFailure(String error);
    }

    public interface OnOperationListener {
        void onSuccess();
        void onFailure(String error);
    }
}