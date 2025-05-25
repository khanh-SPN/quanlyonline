package com.example.quanlyonline.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quanlyonline.R;
import com.example.quanlyonline.controller.ScheduleController;
import com.example.quanlyonline.model.Schedule;
import com.example.quanlyonline.ui.ScheduleAdapter;
import com.example.quanlyonline.ui.AddScheduleDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ScheduleListFragment extends Fragment implements ScheduleAdapter.OnScheduleClickListener {

    private RecyclerView recyclerView;
    private ScheduleAdapter adapter;
    private ScheduleController scheduleController;
    private List<Schedule> scheduleList;
    private FloatingActionButton fabAdd;
    private String className = "class_10A1"; // Giả sử lớp mặc định

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule_list, container, false);

        recyclerView = view.findViewById(R.id.schedule_recycler_view);
        fabAdd = view.findViewById(R.id.fab_add_schedule);
        scheduleController = new ScheduleController();
        scheduleList = new ArrayList<>();

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ScheduleAdapter(scheduleList, this);
        recyclerView.setAdapter(adapter);

        loadSchedules();

        fabAdd.setOnClickListener(v -> showAddScheduleModal(null));

        return view;
    }

    private void loadSchedules() {
        scheduleController.getSchedulesByClass(className, new ScheduleController.OnSchedulesResultListener() {
            @Override
            public void onSuccess(List<Schedule> schedules) {
                scheduleList.clear();
                scheduleList.addAll(schedules);
                adapter.updateSchedules(scheduleList);
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onScheduleClick(Schedule schedule) {
        showAddScheduleModal(schedule);
    }

    private void showAddScheduleModal(Schedule schedule) {
        AddScheduleDialog dialog = new AddScheduleDialog(scheduleController, className, schedule, () -> loadSchedules());
        dialog.show(getParentFragmentManager(), "AddScheduleDialog");
    }
}