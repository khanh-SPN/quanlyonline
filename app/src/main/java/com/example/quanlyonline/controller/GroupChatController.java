package com.example.quanlyonline.controller;

import com.example.quanlyonline.model.Message;
import com.example.quanlyonline.repository.GroupChatRepository;
import com.example.quanlyonline.service.GroupChatService;

import java.util.List;

public class GroupChatController {
    private GroupChatService groupChatService;

    public GroupChatController() {
        groupChatService = new GroupChatService();
    }

    public void getMessages(String groupChatId, OnMessagesResultListener listener) {
        groupChatService.getMessages(groupChatId, new GroupChatRepository.OnMessagesLoadedListener() {
            @Override
            public void onSuccess(List<Message> messages) {
                listener.onSuccess(messages);
            }

            @Override
            public void onFailure(String error) {
                listener.onFailure(error);
            }
        });
    }

    public void sendMessage(String groupChatId, Message message, OnOperationResultListener listener) {
        groupChatService.sendMessage(groupChatId, message, new GroupChatRepository.OnOperationListener() {
            @Override
            public void onSuccess() {
                listener.onSuccess();
            }

            @Override
            public void onFailure(String error) {
                listener.onFailure(error);
            }
        });
    }

    public void setupGroupChat(String groupChatId, String classId, OnOperationResultListener listener) {
        groupChatService.setupGroupChat(groupChatId, classId, new GroupChatRepository.OnOperationListener() {
            @Override
            public void onSuccess() {
                listener.onSuccess();
            }

            @Override
            public void onFailure(String error) {
                listener.onFailure(error);
            }
        });
    }

    public void addParticipant(String groupChatId, String userId, OnOperationResultListener listener) {
        groupChatService.addParticipant(groupChatId, userId, new GroupChatRepository.OnOperationListener() {
            @Override
            public void onSuccess() {
                listener.onSuccess();
            }

            @Override
            public void onFailure(String error) {
                listener.onFailure(error);
            }
        });
    }

    public void getStudentsByClass(String classId, OnStudentsResultListener listener) {
        groupChatService.getStudentsByClass(classId, new GroupChatRepository.OnStudentsLoadedListener() {
            @Override
            public void onSuccess(List<String> studentIds) {
                listener.onSuccess(studentIds);
            }

            @Override
            public void onFailure(String error) {
                listener.onFailure(error);
            }
        });
    }

    public void getParticipants(String groupChatId, OnParticipantsResultListener listener) {
        groupChatService.getParticipants(groupChatId, new GroupChatRepository.OnParticipantsLoadedListener() {
            @Override
            public void onSuccess(List<String> participantIds) {
                listener.onSuccess(participantIds);
            }

            @Override
            public void onFailure(String error) {
                listener.onFailure(error);
            }
        });
    }

    public interface OnMessagesResultListener {
        void onSuccess(List<Message> messages);
        void onFailure(String error);
    }

    public interface OnOperationResultListener {
        void onSuccess();
        void onFailure(String error);
    }

    public interface OnStudentsResultListener {
        void onSuccess(List<String> studentIds);
        void onFailure(String error);
    }

    public interface OnParticipantsResultListener {
        void onSuccess(List<String> participantIds);
        void onFailure(String error);
    }
}