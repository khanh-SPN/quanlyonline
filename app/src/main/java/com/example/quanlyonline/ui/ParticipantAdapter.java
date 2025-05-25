package com.example.quanlyonline.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quanlyonline.R;

import java.util.List;

public class ParticipantAdapter extends RecyclerView.Adapter<ParticipantAdapter.ParticipantViewHolder> {
    private List<String> participantNames;

    public ParticipantAdapter(List<String> participantNames) {
        this.participantNames = participantNames;
    }

    @NonNull
    @Override
    public ParticipantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_participant, parent, false);
        return new ParticipantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParticipantViewHolder holder, int position) {
        String participantName = participantNames.get(position);
        holder.participantNameText.setText(participantName);
    }

    @Override
    public int getItemCount() {
        return participantNames.size();
    }

    public void updateParticipants(List<String> newParticipants) {
        this.participantNames = newParticipants;
        notifyDataSetChanged();
    }

    static class ParticipantViewHolder extends RecyclerView.ViewHolder {
        TextView participantNameText;

        public ParticipantViewHolder(@NonNull View itemView) {
            super(itemView);
            participantNameText = itemView.findViewById(R.id.participant_name_text);
        }
    }
}