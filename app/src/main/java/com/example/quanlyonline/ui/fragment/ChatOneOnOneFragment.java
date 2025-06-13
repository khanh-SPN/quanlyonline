package com.example.quanlyonline.ui.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quanlyonline.model.ChatMessage;
import com.example.quanlyonline.R;
import com.example.quanlyonline.model.ChatSession;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ChatOneOnOneFragment extends Fragment {

    private TextView tvReceiverName;
    private ImageButton btnBack;
    private RecyclerView recyclerViewMessages;
    private EditText etMessageInput;
    private ImageButton btnSend;
    private List<ChatMessage> messageList;
    private MessageAdapter messageAdapter;
    private DatabaseReference messagesRef;
    private DatabaseReference chatSessionsRef;
    private String currentUserId;
    private String receiverId;
    private String receiverName;
    private String chatId;

    private static final String TAG = "ChatOneOnOneFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_one_on_one, container, false);

        tvReceiverName = view.findViewById(R.id.tv_receiver_name);
        btnBack = view.findViewById(R.id.btn_back);
        recyclerViewMessages = view.findViewById(R.id.recycler_view_messages);
        etMessageInput = view.findViewById(R.id.et_message_input);
        btnSend = view.findViewById(R.id.btn_send);
        messageList = new ArrayList<>();

        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(getContext()));
        messageAdapter = new MessageAdapter(messageList);
        recyclerViewMessages.setAdapter(messageAdapter);

        // Lấy thông tin từ arguments
        if (getArguments() != null) {
            receiverId = getArguments().getString("receiverId");
            receiverName = getArguments().getString("receiverName");
            Log.d(TAG, "Receiver ID: " + receiverId + ", Receiver Name: " + receiverName);
        } else {
            Log.e(TAG, "Arguments are null, cannot retrieve receiver information");
            Toast.makeText(getContext(), "Lỗi: Không tìm thấy thông tin người nhận", Toast.LENGTH_LONG).show();
            return view;
        }

        currentUserId = getActivity().getIntent().getStringExtra("user_id");
        if (currentUserId == null) {
            Log.e(TAG, "Current user ID is null");
            Toast.makeText(getContext(), "Lỗi: Không tìm thấy thông tin người dùng", Toast.LENGTH_LONG).show();
            return view;
        }

        Log.d(TAG, "Current user ID: " + currentUserId);
        tvReceiverName.setText(receiverName != null ? receiverName : "Unknown");

        // Tạo chatId (userId1_userId2, sắp xếp theo thứ tự tăng dần)
        List<String> userIds = new ArrayList<>();
        userIds.add(currentUserId);
        userIds.add(receiverId);
        Collections.sort(userIds);
        chatId = userIds.get(0) + "_" + userIds.get(1);
        Log.d(TAG, "Chat ID: " + chatId);

        messagesRef = FirebaseDatabase.getInstance().getReference("one_on_one_messages").child(chatId);
        chatSessionsRef = FirebaseDatabase.getInstance().getReference("chat_sessions").child(chatId);

        // Tạo phiên chat nếu chưa tồn tại
        initializeChatSession();

        // Load tin nhắn
        loadMessages();

        // Gửi tin nhắn
        btnSend.setOnClickListener(v -> sendMessage());

        // Quay lại ChatListFragment khi nhấn nút Back
        btnBack.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
            Log.d(TAG, "Back button clicked, navigated back to ChatListFragment");
        });

        return view;
    }

    private void initializeChatSession() {
        chatSessionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    HashMap<String, Boolean> participants = new HashMap<>();
                    participants.put(currentUserId, true);
                    participants.put(receiverId, true);

                    ChatSession chatSession = new ChatSession(chatId, participants, "", System.currentTimeMillis());
                    chatSessionsRef.setValue(chatSession, (error, ref) -> {
                        if (error != null) {
                            Log.e(TAG, "Error initializing chat session: " + error.getMessage());
                            Toast.makeText(getContext(), "Lỗi khởi tạo phiên chat: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        } else {
                            Log.d(TAG, "Chat session initialized successfully: " + chatId);
                        }
                    });
                } else {
                    Log.d(TAG, "Chat session already exists: " + chatId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error checking chat session existence: " + error.getMessage());
                Toast.makeText(getContext(), "Lỗi kiểm tra phiên chat: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadMessages() {
        messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                if (!snapshot.exists()) {
                    Log.w(TAG, "No messages found for chat: " + chatId);
                    messageAdapter.notifyDataSetChanged();
                    Toast.makeText(getContext(), "Chưa có tin nhắn nào", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                    ChatMessage message = messageSnapshot.getValue(ChatMessage.class);
                    if (message != null) {
                        message.setMessageId(messageSnapshot.getKey());
                        messageList.add(message);
                        Log.d(TAG, "Loaded message: " + (message.getMessage_text() != null ? message.getMessage_text().getValue() : "null"));
                    } else {
                        Log.w(TAG, "Failed to parse message: " + messageSnapshot.getKey());
                    }
                }
                messageAdapter.notifyDataSetChanged();
                recyclerViewMessages.scrollToPosition(messageList.size() - 1);
                Log.d(TAG, "Messages loaded, total: " + messageList.size());

                // Cập nhật last_message và last_timestamp trong chat_sessions
                if (!messageList.isEmpty()) {
                    ChatMessage lastMessage = messageList.get(messageList.size() - 1);
                    if (lastMessage.getMessage_text() != null) {
                        chatSessionsRef.child("last_message").child("value").setValue(lastMessage.getMessage_text().getValue(), (error, ref) -> {
                            if (error != null) {
                                Log.e(TAG, "Error updating last_message: " + error.getMessage());
                            } else {
                                Log.d(TAG, "Updated last_message: " + lastMessage.getMessage_text().getValue());
                            }
                        });
                    }
                    if (lastMessage.getTimestamp() != null) {
                        chatSessionsRef.child("last_timestamp").child("value").setValue(lastMessage.getTimestamp().getValue(), (error, ref) -> {
                            if (error != null) {
                                Log.e(TAG, "Error updating last_timestamp: " + error.getMessage());
                            } else {
                                Log.d(TAG, "Updated last_timestamp: " + lastMessage.getTimestamp().getValue());
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading messages: " + error.getMessage());
                Toast.makeText(getContext(), "Lỗi tải tin nhắn: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void sendMessage() {
        String messageText = etMessageInput.getText().toString().trim();
        if (messageText.isEmpty()) {
            Log.w(TAG, "Message text is empty, not sending");
            Toast.makeText(getContext(), "Vui lòng nhập tin nhắn", Toast.LENGTH_SHORT).show();
            return;
        }

        long timestamp = System.currentTimeMillis();
        ChatMessage message = new ChatMessage(currentUserId, messageText, timestamp);
        String messageId = messagesRef.push().getKey();
        message.setMessageId(messageId);

        messagesRef.child(messageId).setValue(message, (error, ref) -> {
            if (error != null) {
                Log.e(TAG, "Error sending message: " + error.getMessage());
                Toast.makeText(getContext(), "Lỗi gửi tin nhắn: " + error.getMessage(), Toast.LENGTH_LONG).show();
            } else {
                Log.d(TAG, "Message sent successfully: " + messageText);
                etMessageInput.setText("");
            }
        });
    }

    private class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
        private List<ChatMessage> messages;

        public MessageAdapter(List<ChatMessage> messages) {
            this.messages = messages;
        }

        @NonNull
        @Override
        public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_one_on_one, parent, false);
            return new MessageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
            ChatMessage message = messages.get(position);
            // Kiểm tra null để tránh crash
            if (message.getMessage_text() != null) {
                holder.tvMessage.setText(message.getMessage_text().getValue());
            } else {
                holder.tvMessage.setText("");
                Log.w(TAG, "Message text is null at position: " + position);
            }

            if (message.getTimestamp() != null) {
                holder.tvTimestamp.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(message.getTimestamp().getValue())));
            } else {
                holder.tvTimestamp.setText("");
                Log.w(TAG, "Timestamp is null at position: " + position);
            }

            if (message.getSender_id() != null && message.getSender_id().getValue().equals(currentUserId)) {
                holder.tvMessage.setBackgroundResource(R.drawable.message_sent_background);
                holder.tvMessage.setTextColor(getResources().getColor(android.R.color.white));
                holder.itemView.setPadding(850, 8, 16, 8);
                Log.d(TAG, "Binding sent message at position " + position + ": " + (message.getMessage_text() != null ? message.getMessage_text().getValue() : "null"));
            } else {
                holder.tvMessage.setBackgroundResource(R.drawable.message_received_background);
                holder.tvMessage.setTextColor(getResources().getColor(android.R.color.black));
                holder.itemView.setPadding(16, 8, 850, 8);
                Log.d(TAG, "Binding received message at position " + position + ": " + (message.getMessage_text() != null ? message.getMessage_text().getValue() : "null"));
            }
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        public class MessageViewHolder extends RecyclerView.ViewHolder {
            TextView tvMessage;
            TextView tvTimestamp;

            public MessageViewHolder(@NonNull View itemView) {
                super(itemView);
                tvMessage = itemView.findViewById(R.id.tv_message);
                tvTimestamp = itemView.findViewById(R.id.tv_timestamp);
            }
        }
    }
}