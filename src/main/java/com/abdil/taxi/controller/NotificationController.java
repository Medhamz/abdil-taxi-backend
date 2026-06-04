package com.abdil.taxi.controller;

import com.abdil.taxi.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    // Historique des campagnes en mémoire
    private final List<Map<String, Object>> campaignHistory = new ArrayList<>();

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

    @PostMapping("/send/to-clients")
    public ResponseEntity<String> sendToClients(
            @RequestParam String title,
            @RequestParam String body) {
        notificationService.sendNotificationToAllClients(title, body);
        return ResponseEntity.ok("Notification envoyée aux clients");
    }

    // ==================== CAMPAGNES PUSH ====================

    @PostMapping("/campaign/send")
    public ResponseEntity<Map<String, Object>> sendCampaign(@RequestBody Map<String, String> campaign) {
        String title = campaign.get("title");
        String body = campaign.get("body");
        String target = campaign.get("target"); // "ALL", "CLIENTS", "DRIVERS"
        String image = campaign.getOrDefault("image", "");

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("sentAt", LocalDateTime.now().toString());

        int sentCount = 0;

        switch (target) {
            case "ALL":
                notificationService.sendNotificationToAllClients(title, body);
                notificationService.sendNotificationToAllDrivers(title, body);
                response.put("message", "Campagne envoyée à tous les utilisateurs");
                sentCount = getTotalUsers();
                break;
            case "CLIENTS":
                notificationService.sendNotificationToAllClients(title, body);
                response.put("message", "Campagne envoyée à tous les clients");
                sentCount = getTotalClients();
                break;
            case "DRIVERS":
                notificationService.sendNotificationToAllDrivers(title, body);
                response.put("message", "Campagne envoyée à tous les chauffeurs");
                sentCount = getTotalDrivers();
                break;
            default:
                response.put("success", false);
                response.put("message", "Cible invalide");
                return ResponseEntity.badRequest().body(response);
        }

        // Sauvegarder dans l'historique
        Map<String, Object> historyEntry = new HashMap<>();
        historyEntry.put("id", campaignHistory.size() + 1);
        historyEntry.put("title", title);
        historyEntry.put("body", body.length() > 100 ? body.substring(0, 100) + "..." : body);
        historyEntry.put("target", target);
        historyEntry.put("sentCount", sentCount);
        historyEntry.put("sentAt", LocalDateTime.now().toString());
        campaignHistory.add(0, historyEntry);

        System.out.println("📢 Campagne envoyée: " + title + " (" + target + ")");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/campaigns/history")
    public ResponseEntity<List<Map<String, Object>>> getCampaignHistory() {
        return ResponseEntity.ok(campaignHistory);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getNotificationStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSent", campaignHistory.size() * 10);
        stats.put("deliveryRate", "95%");
        stats.put("engagementRate", "72%");
        stats.put("totalCampaigns", campaignHistory.size());
        return ResponseEntity.ok(stats);
    }

    private int getTotalUsers() {
        return getTotalClients() + getTotalDrivers();
    }

    private int getTotalClients() {
        // À remplacer par un vrai compteur depuis UserRepository
        return 150;
    }

    private int getTotalDrivers() {
        // À remplacer par un vrai compteur depuis DriverRepository
        return 45;
    }
}