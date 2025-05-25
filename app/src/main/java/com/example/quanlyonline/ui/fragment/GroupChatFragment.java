package com.example.quanlyonline.ui.fragment;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quanlyonline.R;
import com.example.quanlyonline.ui.activity.TeacherMainActivity;
import com.example.quanlyonline.controller.GroupChatController;
import com.example.quanlyonline.model.Message;
import com.example.quanlyonline.repository.GroupChatRepository;
import com.example.quanlyonline.ui.AddMemberDialog;
import com.example.quanlyonline.ui.MessageAdapter;
import com.example.quanlyonline.ui.ParticipantAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GroupChatFragment extends Fragment {

    private RecyclerView recyclerView, participantsRecyclerView;
    private MessageAdapter adapter;
    private ParticipantAdapter participantsAdapter;
    private GroupChatController groupChatController;
    private GroupChatRepository groupChatRepository;
    private EditText messageInput;
    private ImageButton sendButton;
    private FloatingActionButton addMemberButton;
    private ProgressBar progressBar;
    private TextView loadingText, participantsLabel;
    private List<Message> messageList;
    private List<String> participantIds;
    private Map<String, String> userIdToNameMap;
    private String groupChatId;
    private String currentUserId;
    private String classId;
    private String userRole;
    private boolean isInitialLoad = true;
    private DatabaseReference usersRef;
    private DatabaseReference classesRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        String role = requireActivity().getIntent().getStringExtra("role");
        if (role == null || !role.equals("teacher")) {
            Toast.makeText(getContext(), "Bạn không có quyền truy cập chức năng này", Toast.LENGTH_SHORT).show();
            getParentFragmentManager().popBackStack();
            return null;
        }

        View view = inflater.inflate(R.layout.fragment_group_chat, container, false);

        recyclerView = view.findViewById(R.id.chat_recycler_view);
        participantsRecyclerView = view.findViewById(R.id.participants_recycler_view);
        messageInput = view.findViewById(R.id.message_input);
        sendButton = view.findViewById(R.id.send_button);
        addMemberButton = view.findViewById(R.id.add_member_button);
        progressBar = view.findViewById(R.id.progress_bar);
        loadingText = view.findViewById(R.id.loading_text);
        participantsLabel = view.findViewById(R.id.participants_label);
        groupChatController = new GroupChatController();
        groupChatRepository = new GroupChatRepository();
        messageList = new ArrayList<>();
        participantIds = new ArrayList<>();
        userIdToNameMap = new HashMap<>();
        usersRef = FirebaseDatabase.getInstance("https://quanlyonline-1c06a-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("users");
        classesRef = FirebaseDatabase.getInstance("https://quanlyonline-1c06a-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("classes");

        // Kiểm tra null cho participantsRecyclerView và participantsLabel
        if (participantsRecyclerView == null || participantsLabel == null) {
            Toast.makeText(getContext(), "Không tìm thấy view participants_recycler_view hoặc participants_label", Toast.LENGTH_LONG).show();
            getParentFragmentManager().popBackStack();
            return view;
        }

        // Lấy currentUserId từ Intent
        currentUserId = requireActivity().getIntent().getStringExtra("user_id");
        if (currentUserId == null) {
            Toast.makeText(getContext(), "Không tìm thấy thông tin người dùng. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            getParentFragmentManager().popBackStack();
            return view;
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MessageAdapter(messageList, currentUserId, userIdToNameMap);
        recyclerView.setAdapter(adapter);

        participantsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        participantsAdapter = new ParticipantAdapter(new ArrayList<>());
        participantsRecyclerView.setAdapter(participantsAdapter);

        // Lấy userRole từ Firebase
        usersRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    userRole = snapshot.child("role").child("value").getValue(String.class);
                    if (userRole == null || !userRole.equals("teacher")) {
                        Toast.makeText(getContext(), "Vai trò không hợp lệ cho người dùng " + currentUserId, Toast.LENGTH_LONG).show();
                        getParentFragmentManager().popBackStack();
                        return;
                    }

                    // Tìm classId dựa trên teacher_id
                    classesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot classSnapshot : snapshot.getChildren()) {
                                String teacherId = classSnapshot.child("teacher_id").child("value").getValue(String.class);
                                if (teacherId != null && teacherId.equals(currentUserId)) {
                                    classId = classSnapshot.getKey();
                                    break;
                                }
                            }
                            if (classId == null) {
                                Toast.makeText(getContext(), "Không tìm thấy lớp do giáo viên " + currentUserId + " quản lý", Toast.LENGTH_LONG).show();
                                getParentFragmentManager().popBackStack();
                                return;
                            }

                            // Tạo groupChatId động dựa trên classId
                            groupChatId = "group_chat_" + classId;

                            // Thiết lập nhóm chat
                            groupChatController.setupGroupChat(groupChatId, classId, new GroupChatController.OnOperationResultListener() {
                                @Override
                                public void onSuccess() {
                                    loadParticipants();
                                    loadMessages();
                                }

                                @Override
                                public void onFailure(String error) {
                                    Toast.makeText(getContext(), "Thiết lập nhóm chat thất bại: " + error, Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                    loadingText.setVisibility(View.GONE);
                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(getContext(), "Lỗi khi lấy thông tin lớp: " + error.getMessage(), Toast.LENGTH_LONG).show();
                            getParentFragmentManager().popBackStack();
                        }
                    });
                } else {
                    Toast.makeText(getContext(), "Không tìm thấy thông tin giáo viên cho người dùng " + currentUserId, Toast.LENGTH_LONG).show();
                    getParentFragmentManager().popBackStack();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Lỗi khi lấy thông tin giáo viên: " + error.getMessage(), Toast.LENGTH_LONG).show();
                getParentFragmentManager().popBackStack();
            }
        });

        sendButton.setOnClickListener(v -> {
            // Kiểm tra quyền gửi tin nhắn
            if (!"teacher".equals(userRole)) {
                Toast.makeText(getContext(), "Chỉ giáo viên được phép gửi tin nhắn", Toast.LENGTH_SHORT).show();
                return;
            }

            String content = messageInput.getText().toString().trim();
            if (content.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập nội dung tin nhắn", Toast.LENGTH_SHORT).show();
                return;
            }

            Message message = new Message();
            message.setSenderId(currentUserId);
            message.setContent(content);
            message.setTimestamp(System.currentTimeMillis());

            groupChatController.sendMessage(groupChatId, message, new GroupChatController.OnOperationResultListener() {
                @Override
                public void onSuccess() {
                    messageInput.setText("");
                    recyclerView.scrollToPosition(messageList.size() - 1);
                }

                @Override
                public void onFailure(String error) {
                    Toast.makeText(getContext(), "Gửi tin nhắn thất bại: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        });

        addMemberButton.setOnClickListener(v -> {
            // Load danh sách học sinh trong lớp và danh sách participants hiện tại
            groupChatController.getStudentsByClass(classId, new GroupChatController.OnStudentsResultListener() {
                @Override
                public void onSuccess(List<String> studentIds) {
                    groupChatController.getParticipants(groupChatId, new GroupChatController.OnParticipantsResultListener() {
                        @Override
                        public void onSuccess(List<String> participantIds) {
                            AddMemberDialog dialog = new AddMemberDialog(groupChatController, groupChatId, studentIds, participantIds, () -> {
                                Toast.makeText(getContext(), "Đã thêm thành viên mới", Toast.LENGTH_SHORT).show();
                                loadParticipants();
                            });
                            dialog.show(getParentFragmentManager(), "AddMemberDialog");
                        }

                        @Override
                        public void onFailure(String error) {
                            Toast.makeText(getContext(), "Không thể tải danh sách thành viên: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onFailure(String error) {
                    Toast.makeText(getContext(), "Không thể tải danh sách học sinh: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        });

        return view;
    }

    private void loadMessages() {
        progressBar.setVisibility(View.VISIBLE);
        loadingText.setVisibility(View.VISIBLE);
        groupChatController.getMessages(groupChatId, new GroupChatController.OnMessagesResultListener() {
            @Override
            public void onSuccess(List<Message> messages) {
                int previousSize = messageList.size();
                messageList.clear();
                // Sắp xếp tin nhắn theo timestamp tăng dần (tin cũ ở đầu, tin mới ở cuối)
                Collections.sort(messages, new Comparator<Message>() {
                    @Override
                    public int compare(Message m1, Message m2) {
                        return Long.compare(m1.getTimestamp(), m2.getTimestamp());
                    }
                });
                messageList.addAll(messages);

                // Load tên người dùng
                loadUserNames();

                // Hiển thị thông báo nếu có tin nhắn mới (sau lần load đầu tiên)
                if (!isInitialLoad && messageList.size() > previousSize) {
                    Message newMessage = messageList.get(messageList.size() - 1);
                    if (!newMessage.getSenderId().equals(currentUserId)) {
                        showNotification(newMessage);
                    }
                }
                isInitialLoad = false;
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(getContext(), "Tải tin nhắn thất bại: " + error, Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                loadingText.setVisibility(View.GONE);
            }
        });
    }

    private void loadUserNames() {
        // Lấy danh sách userId duy nhất từ tin nhắn và participants
        Set<String> uniqueUserIds = new HashSet<>();
        for (Message message : messageList) {
            uniqueUserIds.add(message.getSenderId());
        }
        uniqueUserIds.addAll(participantIds);

        int totalNamesToLoad = uniqueUserIds.size();
        if (totalNamesToLoad == 0) {
            adapter.updateMessages(messageList);
            recyclerView.scrollToPosition(messageList.size() - 1);
            updateParticipantsView();
            progressBar.setVisibility(View.GONE);
            loadingText.setVisibility(View.GONE);
            return;
        }

        int[] loadedNames = {0}; // Biến đếm số lượng tên đã load
        for (String userId : uniqueUserIds) {
            if (!userIdToNameMap.containsKey(userId)) {
                groupChatRepository.getUserName(userId, new GroupChatRepository.OnUserNameLoadedListener() {
                    @Override
                    public void onSuccess(String userName) {
                        userIdToNameMap.put(userId, userName);
                        loadedNames[0]++;
                        if (loadedNames[0] == totalNamesToLoad) {
                            adapter.updateMessages(messageList);
                            recyclerView.scrollToPosition(messageList.size() - 1);
                            updateParticipantsView();
                            progressBar.setVisibility(View.GONE);
                            loadingText.setVisibility(View.GONE);
                        }
                    }
                });
            } else {
                loadedNames[0]++;
                if (loadedNames[0] == totalNamesToLoad) {
                    adapter.updateMessages(messageList);
                    recyclerView.scrollToPosition(messageList.size() - 1);
                    updateParticipantsView();
                    progressBar.setVisibility(View.GONE);
                    loadingText.setVisibility(View.GONE);
                }
            }
        }
    }

    private void loadParticipants() {
        groupChatController.getParticipants(groupChatId, new GroupChatController.OnParticipantsResultListener() {
            @Override
            public void onSuccess(List<String> participants) {
                participantIds.clear();
                participantIds.addAll(participants);
                loadUserNames();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(getContext(), "Không thể tải danh sách thành viên: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateParticipantsView() {
        List<String> participantNames = new ArrayList<>();
        for (String userId : participantIds) {
            String name = userIdToNameMap.getOrDefault(userId, userId);
            participantNames.add(name);
        }
        participantsAdapter.updateParticipants(participantNames);
        participantsLabel.setText("Thành viên nhóm (" + participantIds.size() + "):");
    }

    private void showNotification(Message message) {
        try {
            NotificationManager notificationManager = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);
            String channelId = "chat_notifications";
            String channelName = "Chat Notifications";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(channel);
            }

            Intent intent = new Intent(requireContext(), TeacherMainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            String senderName = userIdToNameMap.getOrDefault(message.getSenderId(), message.getSenderId());
            NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), channelId)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle("Tin nhắn mới từ " + senderName)
                    .setContentText(message.getContent())
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        } catch (Exception e) {
            Toast.makeText(getContext(), "Không thể hiển thị thông báo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}