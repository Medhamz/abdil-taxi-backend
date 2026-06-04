package com.abdil.taxi.controller;

import com.abdil.taxi.model.Message;
import com.abdil.taxi.repository.MessageRepository;
import com.abdil.taxi.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "*")
public class MessageController {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendMessage(@RequestBody Map<String, Object> request) {
        System.out.println("=== MESSAGE RECU ===");
        System.out.println("rideId: " + request.get("rideId"));
        System.out.println("senderId: " + request.get("senderId"));
        System.out.println("receiverId: " + request.get("receiverId"));
        System.out.println("content: " + request.get("content"));
        System.out.println("messageType: " + request.get("messageType"));
        System.out.println("mediaUrl: " + request.get("mediaUrl"));

        Message message = new Message();
        message.setRideId(((Number) request.get("rideId")).longValue());
        message.setSenderId(((Number) request.get("senderId")).longValue());
        message.setSenderType((String) request.get("senderType"));
        message.setReceiverId(((Number) request.get("receiverId")).longValue());
        message.setContent((String) request.get("content"));

        String messageType = (String) request.get("messageType");
        if (messageType != null && !messageType.isEmpty()) {
            message.setMessageType(messageType);
        }

        String mediaUrl = (String) request.get("mediaUrl");
        if (mediaUrl != null && !mediaUrl.isEmpty()) {
            message.setMediaUrl(mediaUrl);
        }

        message.setIsRead(false);
        message.setIsDelivered(false);
        message.setCreatedAt(LocalDateTime.now());

        Message saved = messageRepository.save(message);
        System.out.println("✅ Message sauvegardé ID: " + saved.getId());

        // ✅ ENVOYER LA NOTIFICATION PUSH
        try {
            String senderName = "CLIENT".equals(saved.getSenderType()) ? "Client" : "Chauffeur";
            String notificationTitle = "Nouveau message de " + senderName;
            String notificationBody = saved.getContent() != null && saved.getContent().length() > 50
                    ? saved.getContent().substring(0, 50) + "..."
                    : (saved.getContent() != null ? saved.getContent() : "Message");

            if ("CLIENT".equals(saved.getSenderType())) {
                // Le client a envoyé un message → notifier le chauffeur
                notificationService.sendChatNotificationToDriver(
                        saved.getReceiverId(),
                        notificationTitle,
                        notificationBody,
                        saved.getRideId(),
                        saved.getSenderId(),
                        senderName
                );
            } else {
                // Le chauffeur a envoyé un message → notifier le client
                notificationService.sendChatNotificationToClient(
                        saved.getReceiverId(),
                        notificationTitle,
                        notificationBody,
                        saved.getRideId(),
                        saved.getSenderId(),
                        senderName
                );
            }
            System.out.println("📱 Notification push envoyée à l'utilisateur " + saved.getReceiverId());
        } catch (Exception e) {
            System.err.println("❌ Erreur envoi notification: " + e.getMessage());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("id", saved.getId());
        response.put("rideId", saved.getRideId());
        response.put("senderId", saved.getSenderId());
        response.put("senderType", saved.getSenderType());
        response.put("receiverId", saved.getReceiverId());
        response.put("content", saved.getContent());
        response.put("messageType", saved.getMessageType());
        response.put("mediaUrl", saved.getMediaUrl());
        response.put("reaction", saved.getReaction());
        response.put("isRead", saved.getIsRead());
        response.put("createdAt", saved.getCreatedAt().toString());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/ride/{rideId}")
    public ResponseEntity<List<Map<String, Object>>> getMessages(@PathVariable Long rideId) {
        List<Message> messages = messageRepository.findByRideIdOrderByCreatedAtAsc(rideId);
        System.out.println("📋 " + messages.size() + " messages pour rideId=" + rideId);

        List<Map<String, Object>> response = messages.stream().map(message -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", message.getId());
            m.put("rideId", message.getRideId());
            m.put("senderId", message.getSenderId());
            m.put("senderType", message.getSenderType());
            m.put("receiverId", message.getReceiverId());
            m.put("content", message.getContent());
            m.put("messageType", message.getMessageType());
            m.put("mediaUrl", message.getMediaUrl());
            m.put("reaction", message.getReaction());
            m.put("isRead", message.getIsRead());
            m.put("createdAt", message.getCreatedAt().toString());
            return m;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @PutMapping("/read/{rideId}/{messageId}/{userId}")
    public ResponseEntity<Void> markAsRead(@PathVariable Long rideId,
                                           @PathVariable Long messageId,
                                           @PathVariable Long userId) {
        Message message = messageRepository.findById(messageId).orElse(null);
        if (message != null && message.getReceiverId().equals(userId)) {
            message.setIsRead(true);
            messageRepository.save(message);
            System.out.println("✅ Message " + messageId + " marqué comme lu par userId " + userId);
        }
        return ResponseEntity.ok().build();
    }

    @PutMapping("/readAll/{rideId}/{userId}")
    public ResponseEntity<Void> markAllAsRead(@PathVariable Long rideId, @PathVariable Long userId) {
        List<Message> messages = messageRepository.findByRideIdOrderByCreatedAtAsc(rideId);
        int count = 0;
        for (Message message : messages) {
            if (!message.getIsRead() && message.getReceiverId().equals(userId)) {
                message.setIsRead(true);
                messageRepository.save(message);
                count++;
            }
        }
        System.out.println("✅ " + count + " messages marqués comme lus pour rideId=" + rideId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/reaction/{messageId}/{userId}")
    public ResponseEntity<Void> addReaction(@PathVariable Long messageId,
                                            @PathVariable Long userId,
                                            @RequestParam String reaction) {
        Message message = messageRepository.findById(messageId).orElse(null);
        if (message != null) {
            message.setReaction(reaction.isEmpty() ? null : reaction);
            messageRepository.save(message);
            System.out.println("✅ Réaction '" + reaction + "' ajoutée au message " + messageId);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}