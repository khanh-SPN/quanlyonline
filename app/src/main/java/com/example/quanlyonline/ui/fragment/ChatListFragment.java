package com.example.quanlyonline.ui.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quanlyonline.model.ChatMessage;
import com.example.quanlyonline.R;
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

public class ChatListFragment extends Fragment {

    private RecyclerView recyclerViewChats;
    private EditText searchBar;
    private ImageView searchIcon;
    private ChatAdapter chatAdapter;
    private List<ChatItem> chatList;
    private List<User> allUsers;
    private DatabaseReference messagesRef;
    private DatabaseReference usersRef;
    private String currentUserId;

    private static final String TAG = "ChatListFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);

        recyclerViewChats = view.findViewById(R.id.recycler_view_chats);
        searchBar = view.findViewById(R.id.search_bar);
        searchIcon = view.findViewById(R.id.search_icon);
        chatList = new ArrayList<>();
        allUsers = new ArrayList<>();

        recyclerViewChats.setLayoutManager(new LinearLayoutManager(getContext()));
        chatAdapter = new ChatAdapter(chatList);
        recyclerViewChats.setAdapter(chatAdapter);

        messagesRef = FirebaseDatabase.getInstance().getReference("one_on_one_messages");
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        currentUserId = getActivity().getIntent().getStringExtra("user_id");

        if (currentUserId == null) {
            Log.e(TAG, "Current user ID is null");
            Toast.makeText(getContext(), "Lỗi: Không tìm thấy thông tin người dùng", Toast.LENGTH_LONG).show();
            return view;
        }

        Log.d(TAG, "Current user ID: " + currentUserId);

        // Tải danh sách tất cả user để tìm kiếm
        loadAllUsers();

        // Tải danh sách chat
        loadChats();

        // Hiển thị giao diện tìm kiếm khi nhấn vào icon tìm kiếm
        searchIcon.setOnClickListener(v -> {
            if (searchBar.getVisibility() == View.VISIBLE) {
                searchBar.setVisibility(View.GONE);
                searchBar.setText("");
                Log.d(TAG, "Search bar hidden");
            } else {
                searchBar.setVisibility(View.VISIBLE);
                searchBar.requestFocus();
                Log.d(TAG, "Search icon clicked, search bar visible");
            }
        });

        // Tìm kiếm user khi nhập vào search bar
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "Search query: " + s.toString());
                searchUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    private void loadAllUsers() {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allUsers.clear();
                if (!snapshot.exists()) {
                    Log.w(TAG, "No users found in database");
                    Toast.makeText(getContext(), "Không tìm thấy người dùng nào", Toast.LENGTH_LONG).show();
                    return;
                }

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String userId = userSnapshot.getKey();
                    String name = userSnapshot.child("full_name").child("value").getValue(String.class);
                    if (userId != null && !userId.equals(currentUserId) && name != null) {
                        allUsers.add(new User(userId, name));
                        Log.d(TAG, "Added user to all users list: " + name + " (ID: " + userId + ")");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading users: " + error.getMessage());
                Toast.makeText(getContext(), "Lỗi tải danh sách người dùng: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadChats() {
        messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();
                if (!snapshot.exists()) {
                    Log.w(TAG, "No chat sessions found for user: " + currentUserId);
                    chatAdapter.notifyDataSetChanged();
                    Toast.makeText(getContext(), "Bạn chưa có phiên chat nào", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                    String chatId = chatSnapshot.getKey();
                    Log.d(TAG, "Found chat session: " + chatId);
                    if (chatId != null && chatId.contains(currentUserId)) {
                        String otherUserId = chatId.replace(currentUserId + "_", "").replace("_" + currentUserId, "");
                        Log.d(TAG, "Other user in chat " + chatId + ": " + otherUserId);

                        // Tìm tin nhắn cuối cùng
                        ChatMessage lastMessage = null;
                        long lastTimestamp = 0;
                        for (DataSnapshot messageSnapshot : chatSnapshot.getChildren()) {
                            ChatMessage message = messageSnapshot.getValue(ChatMessage.class);
                            if (message != null && message.getTimestamp() != null) {
                                long messageTimestamp = message.getTimestamp().getValue();
                                if (messageTimestamp > lastTimestamp) {
                                    lastTimestamp = messageTimestamp;
                                    lastMessage = message;
                                }
                            }
                        }

                        if (lastMessage != null) {
                            final ChatMessage finalLastMessage = lastMessage;
                            final long finalTimestamp = lastTimestamp;
                            usersRef.child(otherUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    String name = snapshot.child("full_name").child("value").getValue(String.class);
                                    if (name != null) {
                                        String messageText = finalLastMessage.getMessage_text() != null ? finalLastMessage.getMessage_text().getValue() : "";
                                        ChatItem chatItem = new ChatItem(chatId, otherUserId, name, messageText, finalTimestamp);
                                        chatList.add(chatItem);
                                        chatAdapter.notifyDataSetChanged();
                                        Log.d(TAG, "Added chat session with user " + name + " (ID: " + otherUserId + ")");
                                    } else {
                                        Log.w(TAG, "User " + otherUserId + " does not have a name");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e(TAG, "Error loading user " + otherUserId + ": " + error.getMessage());
                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading chat sessions: " + error.getMessage());
                Toast.makeText(getContext(), "Lỗi tải danh sách chat: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void searchUsers(String query) {
        List<User> filteredList = new ArrayList<>();
        for (User user : allUsers) {
            if (user != null && user.getName() != null && user.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(user);
                Log.d(TAG, "Found matching user for query " + query + ": " + user.getName());
            }
        }

        if (filteredList.isEmpty() && !query.isEmpty()) {
            Log.d(TAG, "No users found for query: " + query);
            Toast.makeText(getContext(), "Không tìm thấy người dùng nào khớp với \"" + query + "\"", Toast.LENGTH_SHORT).show();
        }

        // Hiển thị danh sách tìm kiếm ngay lập tức
        displaySearchResults(filteredList);
    }

    private void displaySearchResults(List<User> filteredUsers) {
        chatList.clear();
        for (User user : filteredUsers) {
            ChatItem chatItem = new ChatItem(null, user.getUserId(), user.getName(), "", 0);
            chatList.add(chatItem);
        }
        chatAdapter.notifyDataSetChanged();
    }

    // Model ChatItem để lưu thông tin phiên chat
    private static class ChatItem {
        private String chatId;
        private String otherUserId;
        private String otherUserName;
        private String lastMessage;
        private long lastTimestamp;

        public ChatItem(String chatId, String otherUserId, String otherUserName, String lastMessage, long lastTimestamp) {
            this.chatId = chatId;
            this.otherUserId = otherUserId;
            this.otherUserName = otherUserName;
            this.lastMessage = lastMessage;
            this.lastTimestamp = lastTimestamp;
        }

        public String getChatId() {
            return chatId;
        }

        public String getOtherUserId() {
            return otherUserId;
        }

        public String getOtherUserName() {
            return otherUserName;
        }

        public String getLastMessage() {
            return lastMessage;
        }

        public long getLastTimestamp() {
            return lastTimestamp;
        }
    }

    // Model User đơn giản
    private static class User {
        private String userId;
        private String name;

        public User(String userId, String name) {
            this.userId = userId;
            this.name = name;
        }

        public String getUserId() {
            return userId;
        }

        public String getName() {
            return name;
        }
    }

    private class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
        private List<ChatItem> chats;

        public ChatAdapter(List<ChatItem> chats) {
            this.chats = chats;
        }

        @NonNull
        @Override
        public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
            return new ChatViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
            ChatItem chat = chats.get(position);
            holder.tvName.setText(chat.getOtherUserName());
            holder.tvLastMessage.setText(chat.getLastMessage());

            long timestamp = chat.getLastTimestamp();
            if (timestamp != 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                holder.tvTimestamp.setText(sdf.format(new Date(timestamp)));
            } else {
                holder.tvTimestamp.setText("");
            }

            holder.itemView.setOnClickListener(v -> {
                ChatOneOnOneFragment chatFragment = new ChatOneOnOneFragment();
                Bundle args = new Bundle();
                args.putString("receiverId", chat.getOtherUserId());
                args.putString("receiverName", chat.getOtherUserName());
                chatFragment.setArguments(args);

                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, chatFragment);
                transaction.addToBackStack(null);
                transaction.commit();
                Log.d(TAG, "Navigated to ChatOneOnOneFragment with receiver: " + chat.getOtherUserId());

                // Đóng drawer nếu đang mở
                DrawerLayout drawerLayout = getActivity().findViewById(R.id.drawer_layout);
                if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                    Log.d(TAG, "Drawer closed");
                }

                // Ẩn thanh tìm kiếm sau khi chọn
                searchBar.setVisibility(View.GONE);
                searchBar.setText("");
            });
        }

        @Override
        public int getItemCount() {
            return chats.size();
        }

        public class ChatViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvLastMessage, tvTimestamp;

            public ChatViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tv_name);
                tvLastMessage = itemView.findViewById(R.id.tv_last_message);
                tvTimestamp = itemView.findViewById(R.id.tv_timestamp);
            }
        }
    }
}