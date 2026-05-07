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
            sendNotification(token.getToken(), title, body);
        }
    }

    public void sendNotificationToClient(Long userId, String title, String body) {
        List<UserToken> tokens = tokenRepository.findByUserId(userId);
        if (tokens.isEmpty()) {
            System.out.println("⚠️ Aucun token trouvé pour le client " + userId);
            return;
        }
        for (UserToken token : tokens) {
            sendNotification(token.getToken(), title, body);
        }
    }

    public void sendNotificationToAllClients(String title, String body) {
        List<UserToken> tokens = tokenRepository.findAll().stream()
                .filter(t -> "CLIENT".equals(t.getUserType()))
                .toList();
        for (UserToken token : tokens) {
            sendNotification(token.getToken(), title, body);
        }
    }

    public void sendNotificationToAllDrivers(String title, String body) {
        List<UserToken> tokens = tokenRepository.findAll().stream()
                .filter(t -> "DRIVER".equals(t.getUserType()))
                .toList();
        for (UserToken token : tokens) {
            sendNotification(token.getToken(), title, body);
        }
    }

    private void sendNotification(String token, String title, String body) {
        try {
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("✅ Notification envoyée: " + response);
        } catch (FirebaseMessagingException e) {
            System.err.println("❌ Erreur envoi notification: " + e.getMessage());
            Optional<UserToken> userToken = tokenRepository.findFirstByToken(token);
            userToken.ifPresent(t -> tokenRepository.delete(t));
        }
    }
}