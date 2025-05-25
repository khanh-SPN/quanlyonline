package com.example.quanlyonline.service;

import com.example.quanlyonline.model.Message;
import com.example.quanlyonline.repository.GroupChatRepository;

import java.util.List;

public class GroupChatService {
    private GroupChatRepository groupChatRepository;

    public GroupChatService() {
        groupChatRepository = new GroupChatRepository();
    }

    public void getMessages(String groupChatId, GroupChatRepository.OnMessagesLoadedListener listener) {
        groupChatRepository.getMessages(groupChatId, listener);
    }

    public void sendMessage(String groupChatId, Message message, GroupChatRepository.OnOperationListener listener) {
        if (message.getContent() == null || message.getContent().isEmpty()) {
            listener.onFailure("Nội dung tin nhắn không được để trống");
            return;
        }
        groupChatRepository.sendMessage(groupChatId, message, listener);
    }

    public void setupGroupChat(String groupChatId, String classId, GroupChatRepository.OnOperationListener listener) {
        groupChatRepository.setupGroupChat(groupChatId, classId, listener);
    }

    public void addParticipant(String groupChatId, String userId, GroupChatRepository.OnOperationListener listener) {
        groupChatRepository.addParticipant(groupChatId, userId, listener);
    }

    public void getStudentsByClass(String classId, GroupChatRepository.OnStudentsLoadedListener listener) {
        groupChatRepository.getStudentsByClass(classId, listener);
    }

    public void getParticipants(String groupChatId, GroupChatRepository.OnParticipantsLoadedListener listener) {
        groupChatRepository.getParticipants(groupChatId, listener);
    }
}