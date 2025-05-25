package com.example.quanlyonline.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quanlyonline.R;
import com.example.quanlyonline.model.Schedule;

import java.util.List;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder> {
    private List<Schedule> schedules;
    private OnScheduleClickListener listener;

    public ScheduleAdapter(List<Schedule> schedules, OnScheduleClickListener listener) {
        this.schedules = schedules;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_schedule, parent, false);
        return new ScheduleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleViewHolder holder, int position) {
        Schedule schedule = schedules.get(position);
        holder.subjectText.setText(schedule.getSubject());
        holder.dateText.setText(schedule.getDate());
        holder.timeText.setText(schedule.getTime());
        holder.descriptionText.setText(schedule.getDescription());
        holder.itemView.setOnClickListener(v -> listener.onScheduleClick(schedule));
    }

    @Override
    public int getItemCount() {
        return schedules.size();
    }

    public void updateSchedules(List<Schedule> newSchedules) {
        this.schedules = newSchedules;
        notifyDataSetChanged();
    }

    static class ScheduleViewHolder extends RecyclerView.ViewHolder {
        TextView subjectText, dateText, timeText, descriptionText;

        public ScheduleViewHolder(@NonNull View itemView) {
            super(itemView);
            subjectText = itemView.findViewById(R.id.schedule_subject);
            dateText = itemView.findViewById(R.id.schedule_date);
            timeText = itemView.findViewById(R.id.schedule_time);
            descriptionText = itemView.findViewById(R.id.schedule_description);
        }
    }

    public interface OnScheduleClickListener {
        void onScheduleClick(Schedule schedule);
    }
}