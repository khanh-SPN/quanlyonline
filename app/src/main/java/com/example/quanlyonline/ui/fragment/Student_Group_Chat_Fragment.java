package com.example.quanlyonline.ui.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quanlyonline.R;
import com.example.quanlyonline.controller.GroupChatController;
import com.example.quanlyonline.controller.NotificationController;
import com.example.quanlyonline.model.Message;
import com.example.quanlyonline.model.Notification;
import com.example.quanlyonline.repository.GroupChatRepository;
import com.example.quanlyonline.ui.Student_Message_Adapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Student_Group_Chat_Fragment extends Fragment {

    private static final String TAG = "StudentGroupChat";

    private RecyclerView recyclerView;
    private Student_Message_Adapter adapter;
    private GroupChatController groupChatController;
    private GroupChatRepository groupChatRepository;
    private NotificationController notificationController;
    private EditText messageInput;
    private ImageButton sendButton;
    private ProgressBar progressBar;
    private TextView loadingText;
    private List<Message> messageList;
    private List<String> participantIds;
    private Map<String, String> userIdToNameMap;
    private String groupChatId;
    private String currentUserId;
    private String classId;
    private String userRole;
    private boolean isInitialLoad = true;
    private DatabaseReference usersRef;
    private DatabaseReference messagesRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: Starting to create view");
        String role = requireActivity().getIntent().getStringExtra("role");
        if (role == null || (!role.equals("teacher") && !role.equals("student"))) {
            Toast.makeText(requireContext(), "Bạn không có quyền truy cập chức năng này", Toast.LENGTH_SHORT).show();
            getParentFragmentManager().popBackStack();
            return null;
        }

        View view = inflater.inflate(R.layout.fragment_student_group_chat_fragment, container, false);

        recyclerView = view.findViewById(R.id.chat_recycler_view);
        messageInput = view.findViewById(R.id.message_input);
        sendButton = view.findViewById(R.id.send_button);
        progressBar = view.findViewById(R.id.progress_bar);
        loadingText = view.findViewById(R.id.loading_text);
        groupChatController = new GroupChatController();
        groupChatRepository = new GroupChatRepository();
        notificationController = new NotificationController();
        messageList = new ArrayList<>();
        participantIds = new ArrayList<>();
        userIdToNameMap = new HashMap<>();
        usersRef = FirebaseDatabase.getInstance("https://quanlyonline-1c06a-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("users");
        messagesRef = FirebaseDatabase.getInstance("https://quanlyonline-1c06a-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("messages");

        // Lấy currentUserId và role từ Intent
        currentUserId = requireActivity().getIntent().getStringExtra("user_id");
        userRole = requireActivity().getIntent().getStringExtra("role");
        if (currentUserId == null || userRole == null) {
            Toast.makeText(requireContext(), "Không tìm thấy thông tin người dùng. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            getParentFragmentManager().popBackStack();
            return view;
        }
        Log.d(TAG, "onCreateView: Current user ID: " + currentUserId + ", Role: " + userRole);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new Student_Message_Adapter(messageList, currentUserId, userIdToNameMap);
        recyclerView.setAdapter(adapter);
        Log.d(TAG, "onCreateView: RecyclerView and adapter initialized");

        // Kiểm tra kích thước của RecyclerView
        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Log.d(TAG, "RecyclerView size: width=" + recyclerView.getWidth() + ", height=" + recyclerView.getHeight());
                recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        // Ẩn nút thêm thành viên nếu là học sinh
        if (userRole.equals("student")) {
            View addMemberButton = view.findViewById(R.id.add_member_button);
            if (addMemberButton != null) {
                addMemberButton.setVisibility(View.GONE);
                Log.d(TAG, "onCreateView: Hid add member button for student");
            }
        }

        // Lấy classId và role của người dùng từ Firebase
        usersRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    classId = snapshot.child("class_id").child("value").getValue(String.class);
                    userRole = snapshot.child("role").child("value").getValue(String.class);
                    if (classId == null || userRole == null) {
                        Toast.makeText(requireContext(), "Không tìm thấy thông tin lớp hoặc vai trò", Toast.LENGTH_SHORT).show();
                        getParentFragmentManager().popBackStack();
                        return;
                    }
                    Log.d(TAG, "onDataChange: Class ID: " + classId + ", Role: " + userRole);

                    // Tạo groupChatId động dựa trên classId
                    groupChatId = "group_chat_" + classId;
                    Log.d(TAG, "onDataChange: Group chat ID: " + groupChatId);

                    // Thiết lập nhóm chat
                    groupChatController.setupGroupChat(groupChatId, classId, new GroupChatController.OnOperationResultListener() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "setupGroupChat: Group chat setup successful");
                            loadParticipants();
                            loadMessages();
                        }

                        @Override
                        public void onFailure(String error) {
                            Log.e(TAG, "setupGroupChat: Failed to setup group chat: " + error);
                            Toast.makeText(requireContext(), "Thiết lập nhóm chat thất bại: " + error, Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                            loadingText.setVisibility(View.GONE);
                        }
                    });
                } else {
                    Log.e(TAG, "onDataChange: User data not found for user ID: " + currentUserId);
                    Toast.makeText(requireContext(), "Không tìm thấy thông tin người dùng", Toast.LENGTH_LONG).show();
                    getParentFragmentManager().popBackStack();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "onCancelled: Failed to fetch user data: " + error.getMessage());
                Toast.makeText(requireContext(), "Lỗi khi lấy thông tin người dùng: " + error.getMessage(), Toast.LENGTH_LONG).show();
                getParentFragmentManager().popBackStack();
            }
        });

        sendButton.setOnClickListener(v -> {
            String content = messageInput.getText().toString().trim();
            if (content.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập nội dung tin nhắn", Toast.LENGTH_SHORT).show();
                return;
            }

            Message message = new Message();
            message.setSenderId(currentUserId);
            message.setContent(content);
            message.setTimestamp(System.currentTimeMillis());
            Log.d(TAG, "Sending message: " + content + " by user: " + currentUserId);

            groupChatController.sendMessage(groupChatId, message, new GroupChatController.OnOperationResultListener() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "sendMessage: Message sent successfully");
                    messageInput.setText("");
                    recyclerView.scrollToPosition(messageList.size() - 1);
                    sendChatNotificationToParticipants(message);
                }

                @Override
                public void onFailure(String error) {
                    Log.e(TAG, "sendMessage: Failed to send message: " + error);
                    Toast.makeText(requireContext(), "Gửi tin nhắn thất bại: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        });

        return view;
    }

    private void loadMessages() {
        progressBar.setVisibility(View.VISIBLE);
        loadingText.setVisibility(View.VISIBLE);
        Log.d(TAG, "loadMessages: Starting to load messages for group: " + groupChatId);

        groupChatController.getMessages(groupChatId, new GroupChatController.OnMessagesResultListener() {
            @Override
            public void onSuccess(List<Message> messages) {
                Log.d(TAG, "loadMessages: Successfully loaded " + messages.size() + " messages");
                int previousSize = messageList.size();
                messageList.clear();
                messageList.addAll(messages);
                Collections.sort(messageList, (m1, m2) -> Long.compare(m1.getTimestamp(), m2.getTimestamp()));
                Log.d(TAG, "loadMessages: Messages sorted, new size: " + messageList.size());

                // Load tên người dùng
                loadUserNames();

                // Hiển thị thông báo nếu có tin nhắn mới (sau lần load đầu tiên)
                if (!isInitialLoad && messageList.size() > previousSize) {
                    Message newMessage = messageList.get(messageList.size() - 1);
                    if (!newMessage.getSenderId().equals(currentUserId)) {
                        Log.d(TAG, "loadMessages: New message received from " + newMessage.getSenderId());
                        sendChatNotificationToParticipants(newMessage);
                    }
                }
                isInitialLoad = false;
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "loadMessages: Failed to load messages: " + error);
                Toast.makeText(requireContext(), "Tải tin nhắn thất bại: " + error, Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                loadingText.setVisibility(View.GONE);
            }
        });
    }

    private void loadUserNames() {
        Log.d(TAG, "loadUserNames: Starting to load user names");
        // Lấy danh sách userId duy nhất từ tin nhắn và participants
        Set<String> uniqueUserIds = new HashSet<>();
        for (Message message : messageList) {
            uniqueUserIds.add(message.getSenderId());
            Log.d(TAG, "loadUserNames: Added user ID from message: " + message.getSenderId());
        }
        uniqueUserIds.addAll(participantIds);
        Log.d(TAG, "loadUserNames: Total unique user IDs: " + uniqueUserIds.size());

        int totalNamesToLoad = uniqueUserIds.size();
        if (totalNamesToLoad == 0) {
            Log.d(TAG, "loadUserNames: No user names to load, updating adapter");
            adapter.updateMessages(messageList);
            recyclerView.scrollToPosition(messageList.size() - 1);
            progressBar.setVisibility(View.GONE);
            loadingText.setVisibility(View.GONE);
            return;
        }

        int[] loadedNames = {0}; // Biến đếm số lượng tên đã load
        for (String userId : uniqueUserIds) {
            if (!userIdToNameMap.containsKey(userId)) {
                Log.d(TAG, "loadUserNames: Loading name for user ID: " + userId);
                groupChatRepository.getUserName(userId, new GroupChatRepository.OnUserNameLoadedListener() {
                    @Override
                    public void onSuccess(String userName) {
                        userIdToNameMap.put(userId, userName);
                        loadedNames[0]++;
                        Log.d(TAG, "loadUserNames: Loaded name for user ID: " + userId + ", name: " + userName + ", loaded: " + loadedNames[0] + "/" + totalNamesToLoad);
                        if (loadedNames[0] == totalNamesToLoad) {
                            Log.d(TAG, "loadUserNames: All names loaded, updating adapter with " + messageList.size() + " messages");
                            adapter.updateMessages(new ArrayList<>(messageList));
                            recyclerView.scrollToPosition(messageList.size() - 1);
                            progressBar.setVisibility(View.GONE);
                            loadingText.setVisibility(View.GONE);
                        }
                    }
                });
            } else {
                loadedNames[0]++;
                Log.d(TAG, "loadUserNames: Name already loaded for user ID: " + userId + ", loaded: " + loadedNames[0] + "/" + totalNamesToLoad);
                if (loadedNames[0] == totalNamesToLoad) {
                    Log.d(TAG, "loadUserNames: All names loaded (cached), updating adapter with " + messageList.size() + " messages");
                    adapter.updateMessages(new ArrayList<>(messageList));
                    recyclerView.scrollToPosition(messageList.size() - 1);
                    progressBar.setVisibility(View.GONE);
                    loadingText.setVisibility(View.GONE);
                }
            }
        }
    }

    private void loadParticipants() {
        Log.d(TAG, "loadParticipants: Starting to load participants for group: " + groupChatId);
        groupChatController.getParticipants(groupChatId, new GroupChatController.OnParticipantsResultListener() {
            @Override
            public void onSuccess(List<String> participants) {
                participantIds.clear();
                participantIds.addAll(participants);
                Log.d(TAG, "loadParticipants: Loaded " + participantIds.size() + " participants");
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "loadParticipants: Failed to load participants: " + error);
                Toast.makeText(requireContext(), "Không thể tải danh sách thành viên: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendChatNotificationToParticipants(Message message) {
        Log.d(TAG, "sendChatNotificationToParticipants: Sending notifications for new message");
        String senderName = userIdToNameMap.getOrDefault(message.getSenderId(), message.getSenderId());
        String notificationTitle = "Tin nhắn mới từ " + senderName;
        String notificationContent = message.getContent();

        for (String participantId : participantIds) {
            // Không gửi thông báo cho chính người gửi
            if (participantId.equals(currentUserId)) {
                continue;
            }

            Notification notification = new Notification();
            notification.setSenderId(currentUserId);
            notification.setReceiverId(participantId);
            notification.setTitle(notificationTitle);
            notification.setContent(notificationContent);
            notification.setTimestamp(System.currentTimeMillis());
            notification.setRead(false);

            Log.d(TAG, "sendChatNotificationToParticipants: Sending notification to user: " + participantId);
            notificationController.sendNotification(participantId, notification, new NotificationController.OnOperationResultListener() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "sendChatNotificationToParticipants: Notification sent to user: " + participantId);
                }

                @Override
                public void onFailure(String error) {
                    Log.e(TAG, "sendChatNotificationToParticipants: Failed to send notification to user: " + participantId + ", error: " + error);
                    if (isAdded() && getContext() != null) {
                        Toast.makeText(getContext(), "Không thể gửi thông báo: " + error, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}