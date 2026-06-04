package com.abdil.taxi.model;

import java.time.LocalDateTime;

public class MessageResponse {
    private Long id;
    private Long rideId;
    private Long senderId;
    private String senderType;
    private String content;
    private String messageType;
    private String mediaUrl;
    private Boolean isRead;
    private LocalDateTime createdAt;

    public MessageResponse() {}

    public MessageResponse(Long id, Long rideId, Long senderId, String senderType,
                           String content, String messageType, String mediaUrl,
                           Boolean isRead, LocalDateTime createdAt) {
        this.id = id;
        this.rideId = rideId;
        this.senderId = senderId;
        this.senderType = senderType;
        this.content = content;
        this.messageType = messageType;
        this.mediaUrl = mediaUrl;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    // Getters
    public Long getId() { return id; }
    public Long getRideId() { return rideId; }
    public Long getSenderId() { return senderId; }
    public String getSenderType() { return senderType; }
    public String getContent() { return content; }
    public String getMessageType() { return messageType; }
    public String getMediaUrl() { return mediaUrl; }
    public Boolean getIsRead() { return isRead; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setRideId(Long rideId) { this.rideId = rideId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }
    public void setSenderType(String senderType) { this.senderType = senderType; }
    public void setContent(String content) { this.content = content; }
    public void setMessageType(String messageType) { this.messageType = messageType; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}