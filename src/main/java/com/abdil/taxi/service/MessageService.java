package com.abdil.taxi.service;

import com.abdil.taxi.model.Message;
import com.abdil.taxi.model.MessageRequest;
import com.abdil.taxi.model.MessageResponse;
import com.abdil.taxi.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    public MessageResponse sendMessage(MessageRequest request) {
        Message message = new Message();
        message.setRideId(request.getRideId());
        message.setSenderId(request.getSenderId());
        message.setSenderType(request.getSenderType());
        message.setReceiverId(request.getReceiverId());
        message.setContent(request.getContent());
        message.setIsRead(false);
        message.setIsDelivered(false);
        message.setCreatedAt(LocalDateTime.now());

        // Ajouter le messageType et mediaUrl s'ils existent
        if (request.getMessageType() != null) {
            message.setMessageType(request.getMessageType());
        }
        if (request.getMediaUrl() != null) {
            message.setMediaUrl(request.getMediaUrl());
        }

        Message saved = messageRepository.save(message);
        return convertToResponse(saved);
    }

    public List<MessageResponse> getMessagesForRide(Long rideId) {
        List<Message> messages = messageRepository.findByRideIdOrderByCreatedAtAsc(rideId);
        return messages.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    public void markMessagesAsRead(Long rideId, Long userId) {
        List<Message> unreadMessages = messageRepository.findByRideIdAndReceiverIdAndIsReadFalse(rideId, userId);
        for (Message message : unreadMessages) {
            message.setIsRead(true);
            messageRepository.save(message);
        }
    }

    public long getUnreadCount(Long rideId, Long userId) {
        return messageRepository.findByRideIdAndReceiverIdAndIsReadFalse(rideId, userId).size();
    }

    private MessageResponse convertToResponse(Message message) {
        MessageResponse response = new MessageResponse();
        response.setId(message.getId());
        response.setRideId(message.getRideId());
        response.setSenderId(message.getSenderId());
        response.setSenderType(message.getSenderType());
        response.setContent(message.getContent());
        response.setMessageType(message.getMessageType());
        response.setMediaUrl(message.getMediaUrl());
        response.setIsRead(message.getIsRead());
        response.setCreatedAt(message.getCreatedAt());
        return response;
    }
}