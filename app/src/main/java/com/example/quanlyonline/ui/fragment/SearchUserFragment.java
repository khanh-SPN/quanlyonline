package com.example.quanlyonline.ui.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.quanlyonline.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class SearchUserFragment extends Fragment {

    private RecyclerView recyclerViewUsers;
    private UserAdapter userAdapter;
    private List<User> userList;
    private DatabaseReference usersRef;
    private DatabaseReference classesRef;
    private String currentUserId;
    private String teacherClassId;
    private String query;

    private static final String TAG = "SearchUserFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_user, container, false);

        recyclerViewUsers = view.findViewById(R.id.recycler_view_users);
        userList = new ArrayList<>();

        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(getContext()));
        userAdapter = new UserAdapter(userList);
        recyclerViewUsers.setAdapter(userAdapter);

        usersRef = FirebaseDatabase.getInstance().getReference("users");
        classesRef = FirebaseDatabase.getInstance().getReference("classes");
        currentUserId = getActivity().getIntent().getStringExtra("user_id");

        if (currentUserId == null) {
            Log.e(TAG, "Current user ID is null");
            Toast.makeText(getContext(), "Lỗi: Không tìm thấy thông tin người dùng", Toast.LENGTH_LONG).show();
            return view;
        }

        Log.d(TAG, "Current user ID: " + currentUserId);

        // Lấy query từ arguments (nếu có)
        if (getArguments() != null) {
            query = getArguments().getString("query", "");
            Log.d(TAG, "Search query from arguments: " + query);
        }

        // Tìm lớp mà giáo viên phụ trách
        findTeacherClass();

        return view;
    }

    private void findTeacherClass() {
        classesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "Current user ID in findTeacherClass: " + currentUserId);
                Log.d(TAG, "Classes snapshot: " + snapshot.toString());
                boolean foundClass = false;
                for (DataSnapshot classSnapshot : snapshot.getChildren()) {
                    String teacherId = classSnapshot.child("teacher_id").child("value").getValue(String.class);
                    Log.d(TAG, "Checking class: " + classSnapshot.getKey() + ", teacher_id: " + teacherId);
                    if (teacherId == null) {
                        Log.e(TAG, "teacher_id is null for class: " + classSnapshot.getKey());
                        continue;
                    }
                    if (currentUserId.equals(teacherId)) {
                        teacherClassId = classSnapshot.getKey();
                        Log.d(TAG, "Found class for teacher: " + teacherClassId);
                        foundClass = true;
                        loadClassUsers();
                        break;
                    }
                }
                if (!foundClass) {
                    Log.e(TAG, "No class found for teacher with userId: " + currentUserId);
                    Toast.makeText(getContext(), "Lỗi: Không tìm thấy lớp của giáo viên", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading classes: " + error.getMessage());
                Toast.makeText(getContext(), "Lỗi tải thông tin lớp: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadClassUsers() {
        if (teacherClassId == null) {
            Log.e(TAG, "Teacher class ID is null, cannot load class users");
            Toast.makeText(getContext(), "Lỗi: Không xác định được lớp của giáo viên", Toast.LENGTH_LONG).show();
            return;
        }

        Log.d(TAG, "Loading users for class: " + teacherClassId);
        classesRef.child(teacherClassId).child("students").child("value").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                Log.d(TAG, "Students snapshot exists: " + snapshot.exists());
                Log.d(TAG, "Students snapshot: " + snapshot.toString());
                if (!snapshot.exists()) {
                    Log.e(TAG, "No students found for class: " + teacherClassId);
                    Toast.makeText(getContext(), "Không tìm thấy học sinh trong lớp " + teacherClassId, Toast.LENGTH_LONG).show();
                    return;
                }

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String userId = userSnapshot.getKey();
                    Boolean value = userSnapshot.getValue(Boolean.class);
                    Log.d(TAG, "Found student in class: " + userId + ", value: " + value);
                    if (value != null && value) {
                        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                String name = userSnapshot.child("full_name").child("value").getValue(String.class);
                                if (name != null) {
                                    // Chỉ thêm học sinh nếu khớp với query (nếu có)
                                    if (query == null || query.isEmpty() || name.toLowerCase().contains(query.toLowerCase())) {
                                        userList.add(new User(userId, name));
                                        userAdapter.notifyDataSetChanged();
                                        Log.d(TAG, "Added student to search list: " + name + " (ID: " + userId + ")");
                                    }
                                } else {
                                    Log.w(TAG, "User " + userId + " does not have a name");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e(TAG, "Error loading user " + userId + ": " + error.getMessage());
                                Toast.makeText(getContext(), "Lỗi tải thông tin học sinh " + userId + ": " + error.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }

                if (userList.isEmpty()) {
                    Log.d(TAG, "No users matched the query: " + (query != null ? query : "none"));
                    Toast.makeText(getContext(), "Không tìm thấy học sinh nào", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading class users: " + error.getMessage());
                Toast.makeText(getContext(), "Lỗi tải danh sách học sinh: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
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

    // Adapter cho RecyclerView
    private class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
        private List<User> users;

        public UserAdapter(List<User> users) {
            this.users = users;
        }

        @NonNull
        @Override
        public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_user, parent, false);
            return new UserViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
            User user = users.get(position);
            holder.tvUserName.setText(user.getName());
            Log.d(TAG, "Binding user to search list: " + user.getName());

            holder.itemView.setOnClickListener(v -> {
                ChatOneOnOneFragment chatFragment = new ChatOneOnOneFragment();
                Bundle args = new Bundle();
                args.putString("receiverId", user.getUserId());
                args.putString("receiverName", user.getName());
                chatFragment.setArguments(args);

                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, chatFragment);
                transaction.addToBackStack(null);
                transaction.commit();
                Log.d(TAG, "Navigated to ChatOneOnOneFragment with receiver: " + user.getUserId());

                // Đóng drawer nếu đang mở
                DrawerLayout drawerLayout = getActivity().findViewById(R.id.drawer_layout);
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                    Log.d(TAG, "Drawer closed");
                }
            });
        }

        @Override
        public int getItemCount() {
            return users.size();
        }

        public class UserViewHolder extends RecyclerView.ViewHolder {
            TextView tvUserName;

            public UserViewHolder(@NonNull View itemView) {
                super(itemView);
                tvUserName = itemView.findViewById(R.id.tv_user_name);
            }
        }
    }
}