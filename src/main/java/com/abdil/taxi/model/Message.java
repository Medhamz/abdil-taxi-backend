package com.abdil.taxi.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long rideId;

    @Column(nullable = false)
    private Long senderId;

    @Column(nullable = false)
    private String senderType;

    @Column(nullable = false)
    private Long receiverId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    private String messageType = "TEXT";

    @Column(length = 500)
    private String mediaUrl;

    private Boolean isRead = false;
    private Boolean isDelivered = false;

    private LocalDateTime createdAt = LocalDateTime.now();

    private String reaction;

    public Message() {}

    // Getters
    public Long getId() { return id; }
    public Long getRideId() { return rideId; }
    public Long getSenderId() { return senderId; }
    public String getSenderType() { return senderType; }
    public Long getReceiverId() { return receiverId; }
    public String getContent() { return content; }
    public String getMessageType() { return messageType; }
    public String getMediaUrl() { return mediaUrl; }
    public Boolean getIsRead() { return isRead; }
    public Boolean getIsDelivered() { return isDelivered; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setRideId(Long rideId) { this.rideId = rideId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }
    public void setSenderType(String senderType) { this.senderType = senderType; }
    public void setReceiverId(Long receiverId) { this.receiverId = receiverId; }
    public void setContent(String content) { this.content = content; }
    public void setMessageType(String messageType) { this.messageType = messageType; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }
    public void setIsDelivered(Boolean isDelivered) { this.isDelivered = isDelivered; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getReaction() { return reaction; }
    public void setReaction(String reaction) { this.reaction = reaction; }
}