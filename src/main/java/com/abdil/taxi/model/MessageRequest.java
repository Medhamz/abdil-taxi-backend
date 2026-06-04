package com.abdil.taxi.model;

public class MessageRequest {
    private Long rideId;
    private Long senderId;
    private String senderType;
    private Long receiverId;
    private String content;
    private String messageType = "TEXT";
    private String mediaUrl;

    public MessageRequest() {}

    public MessageRequest(Long rideId, Long senderId, String senderType, Long receiverId, String content, String messageType, String mediaUrl) {
        this.rideId = rideId;
        this.senderId = senderId;
        this.senderType = senderType;
        this.receiverId = receiverId;
        this.content = content;
        this.messageType = messageType;
        this.mediaUrl = mediaUrl;
    }

    // Getters et Setters
    public Long getRideId() { return rideId; }
    public void setRideId(Long rideId) { this.rideId = rideId; }

    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }

    public String getSenderType() { return senderType; }
    public void setSenderType(String senderType) { this.senderType = senderType; }

    public Long getReceiverId() { return receiverId; }
    public void setReceiverId(Long receiverId) { this.receiverId = receiverId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }

    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }
}