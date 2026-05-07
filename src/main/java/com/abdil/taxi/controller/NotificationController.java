package com.abdil.taxi.controller;

import com.abdil.taxi.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/register")
    public ResponseEntity<String> registerToken(
            @RequestParam Long userId,
            @RequestParam String token,
            @RequestParam String deviceId,
            @RequestParam String userType) {
        notificationService.saveToken(userId, token, deviceId, userType);
        return ResponseEntity.ok("Token enregistré");
    }

    @PostMapping("/send/to-user")
    public ResponseEntity<String> sendToUser(
            @RequestParam Long userId,
            @RequestParam String title,
            @RequestParam String body) {
        notificationService.sendNotificationToUser(userId, title, body);
        return ResponseEntity.ok("Notification envoyée");
    }

    @PostMapping("/send/to-drivers")
    public ResponseEntity<String> sendToDrivers(
            @RequestParam String title,
            @RequestParam String body) {
        notificationService.sendNotificationToAllDrivers(title, body);
        return ResponseEntity.ok("Notification envoyée aux chauffeurs");
    }
}