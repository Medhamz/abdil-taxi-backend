package com.abdil.taxi.service;

import com.abdil.taxi.model.UserToken;
import com.abdil.taxi.repository.TokenRepository;
import com.google.firebase.messaging.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {

    @Autowired
    private TokenRepository tokenRepository;

    public void saveToken(Long userId, String token, String deviceId, String userType) {
        // Supprimer l'ancien token pour ce user_id et user_type
        List<UserToken> existingTokens = tokenRepository.findByUserIdAndUserType(userId, userType);
        for (UserToken existing : existingTokens) {
            tokenRepository.delete(existing);
        }
        // Ajouter le nouveau token
        UserToken userToken = new UserToken(userId, token, deviceId, userType);
        tokenRepository.save(userToken);
        System.out.println("✅ Token enregistré pour user " + userId);
    }

    public void sendNotificationToUser(Long userId, String title, String body) {
        List<UserToken> tokens = tokenRepository.findByUserId(userId);
        if (tokens.isEmpty()) {
            System.out.println("⚠️ Aucun token trouvé pour l'utilisateur " + userId);
            return;
        }
        for (UserToken token : tokens) {
            sendNotification(token.getToken(), title, body, null, null, null);
        }
    }

    public void sendNotificationToClient(Long userId, String title, String body) {
        List<UserToken> tokens = tokenRepository.findByUserId(userId);
        if (tokens.isEmpty()) {
            System.out.println("⚠️ Aucun token trouvé pour le client " + userId);
            return;
        }
        for (UserToken token : tokens) {
            sendNotification(token.getToken(), title, body, null, null, null);
        }
    }

    public void sendChatNotification(Long userId, String title, String body, Long rideId, Long senderId, String senderName, String userType) {
        System.out.println("=== sendChatNotification ===");
        System.out.println("userId: " + userId + ", userType: " + userType);
        List<UserToken> tokens = tokenRepository.findByUserIdAndUserType(userId, userType);
        if (tokens.isEmpty()) {
            System.out.println("⚠️ Aucun token trouvé pour l'utilisateur " + userId);
            return;
        }
        for (UserToken token : tokens) {
            sendNotification(token.getToken(), title, body, rideId, senderId, senderName);
        }
    }

    public void sendNotificationToAllClients(String title, String body) {
        List<UserToken> tokens = tokenRepository.findAll().stream()
                .filter(t -> "CLIENT".equals(t.getUserType()))
                .toList();
        for (UserToken token : tokens) {
            sendNotification(token.getToken(), title, body, null, null, null);
        }
    }

    public void sendNotificationToAllDrivers(String title, String body) {
        List<UserToken> tokens = tokenRepository.findAll().stream()
                .filter(t -> "DRIVER".equals(t.getUserType()))
                .toList();
        for (UserToken token : tokens) {
            sendNotification(token.getToken(), title, body, null, null, null);
        }
    }

    public void sendNotificationToDriver(Long driverId, String title, String body) {
        System.out.println("=== sendNotificationToDriver ===");
        System.out.println("driverId: " + driverId);
        List<UserToken> tokens = tokenRepository.findByUserIdAndUserType(driverId, "DRIVER");
        System.out.println("Tokens trouvés: " + tokens.size());
        for (UserToken userToken : tokens) {
            System.out.println("Token: " + userToken.getToken().substring(0, Math.min(50, userToken.getToken().length())) + "...");
            sendNotification(userToken.getToken(), title, body, null, null, null);
        }
    }

    private void sendNotification(String token, String title, String body, Long rideId, Long senderId, String senderName) {
        try {
            System.out.println("=== sendNotification ===");
            System.out.println("Token: " + token.substring(0, Math.min(50, token.length())) + "...");
            System.out.println("Title: " + title);
            System.out.println("Body: " + body);

            Notification.Builder notificationBuilder = Notification.builder()
                    .setTitle(title)
                    .setBody(body);

            Message.Builder messageBuilder = Message.builder()
                    .setToken(token)
                    .setNotification(notificationBuilder.build());

            if (rideId != null) {
                messageBuilder.putData("rideId", String.valueOf(rideId));
            }
            if (senderId != null) {
                messageBuilder.putData("senderId", String.valueOf(senderId));
            }
            if (senderName != null) {
                messageBuilder.putData("senderName", senderName);
            }

            Message message = messageBuilder.build();
            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("✅ Notification envoyée: " + response);
        } catch (FirebaseMessagingException e) {
            System.err.println("❌ Erreur envoi notification: " + e.getMessage());
            Optional<UserToken> userToken = tokenRepository.findFirstByToken(token);
            userToken.ifPresent(t -> tokenRepository.delete(t));
        }
    }

    public void sendChatNotificationToDriver(Long driverId, String title, String body, Long rideId, Long senderId, String senderName) {
        System.out.println("🔥🔥🔥 sendChatNotificationToDriver APPELEE 🔥🔥🔥");
        System.out.println("driverId: " + driverId);
        System.out.println("title: " + title);
        System.out.println("body: " + body);
        System.out.println("rideId: " + rideId);

        List<UserToken> tokens = tokenRepository.findByUserIdAndUserType(driverId, "DRIVER");
        System.out.println("Tokens trouvés: " + tokens.size());

        if (tokens.isEmpty()) {
            System.out.println("⚠️ AUCUN TOKEN TROUVÉ pour driverId=" + driverId);
            return;
        }

        for (UserToken userToken : tokens) {
            System.out.println("Token: " + userToken.getToken().substring(0, Math.min(50, userToken.getToken().length())) + "...");
            sendNotification(userToken.getToken(), title, body, rideId, senderId, senderName);
        }
    }

    public void sendChatNotificationToClient(Long clientId, String title, String body, Long rideId, Long senderId, String senderName) {
        System.out.println("🔥🔥🔥 sendChatNotificationToClient APPELEE 🔥🔥🔥");
        System.out.println("clientId: " + clientId);
        System.out.println("title: " + title);
        System.out.println("body: " + body);

        List<UserToken> tokens = tokenRepository.findByUserIdAndUserType(clientId, "CLIENT");
        System.out.println("Tokens trouvés: " + tokens.size());

        if (tokens.isEmpty()) {
            System.out.println("⚠️ AUCUN TOKEN TROUVÉ pour clientId=" + clientId);
            return;
        }

        for (UserToken userToken : tokens) {
            sendNotification(userToken.getToken(), title, body, rideId, senderId, senderName);
        }
    }
}