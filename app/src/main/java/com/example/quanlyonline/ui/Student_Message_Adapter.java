package com.example.quanlyonline.ui;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quanlyonline.R;
import com.example.quanlyonline.model.Message;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Student_Message_Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "StudentMessageAdapter";
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private List<Message> messages;
    private String currentUserId;
    private Map<String, String> userIdToNameMap;

    public Student_Message_Adapter(List<Message> messages, String currentUserId, Map<String, String> userIdToNameMap) {
        this.messages = messages;
        this.currentUserId = currentUserId;
        this.userIdToNameMap = userIdToNameMap;
        Log.d(TAG, "Constructor: Adapter initialized with " + messages.size() + " messages");
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        return message.getSenderId().equals(currentUserId) ? VIEW_TYPE_SENT : VIEW_TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: Creating view holder, viewType: " + viewType);
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.student_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.student_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        String senderName = userIdToNameMap.getOrDefault(message.getSenderId(), message.getSenderId());
        String timestamp = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date(message.getTimestamp()));

        Log.d(TAG, "onBindViewHolder: Binding message at position " + position + ": Sender: " + senderName + ", Content: " + message.getContent());

        if (holder.getItemViewType() == VIEW_TYPE_SENT) {
            SentMessageViewHolder sentHolder = (SentMessageViewHolder) holder;
            sentHolder.senderNameText.setText(senderName);
            sentHolder.messageText.setText(message.getContent());
            sentHolder.timestampText.setText(timestamp);
        } else {
            ReceivedMessageViewHolder receivedHolder = (ReceivedMessageViewHolder) holder;
            receivedHolder.senderNameText.setText(senderName);
            receivedHolder.messageText.setText(message.getContent());
            receivedHolder.timestampText.setText(timestamp);
        }
    }

    @Override
    public int getItemCount() {
        int count = messages.size();
        Log.d(TAG, "getItemCount: Returning " + count + " items");
        return count;
    }

    public void updateMessages(List<Message> newMessages) {
        Log.d(TAG, "updateMessages: Updating with " + newMessages.size() + " messages");
        this.messages = newMessages;
        notifyDataSetChanged();
        Log.d(TAG, "updateMessages: Notified data set changed, new size: " + messages.size());
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView senderNameText, messageText, timestampText;

        public SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            senderNameText = itemView.findViewById(R.id.sender_name_text);
            messageText = itemView.findViewById(R.id.message_text);
            timestampText = itemView.findViewById(R.id.timestamp_text);
            Log.d(TAG, "SentMessageViewHolder: Initialized view holder");
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView senderNameText, messageText, timestampText;

        public ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            senderNameText = itemView.findViewById(R.id.sender_name_text);
            messageText = itemView.findViewById(R.id.message_text);
            timestampText = itemView.findViewById(R.id.timestamp_text);
            Log.d(TAG, "ReceivedMessageViewHolder: Initialized view holder");
        }
    }
}