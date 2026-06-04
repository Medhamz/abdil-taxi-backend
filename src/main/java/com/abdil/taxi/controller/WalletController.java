package com.abdil.taxi.controller;

import com.abdil.taxi.model.RechargeCoupon;
import com.abdil.taxi.model.Ride;
import com.abdil.taxi.model.WalletTransaction;
import com.abdil.taxi.repository.RideRepository;
import com.abdil.taxi.service.NotificationService;
import com.abdil.taxi.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/wallet")
@CrossOrigin(origins = "*")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private RideRepository rideRepository;

    // Map temporaire pour stocker les tokens QR Code
    private final Map<String, String> qrCodeTokens = new ConcurrentHashMap<>();
    private final Map<String, Long> tokenExpiration = new ConcurrentHashMap<>();

    @Value("${server.address:192.168.1.100}")
    private String serverAddress;

    @Value("${server.port:8080}")
    private String serverPort;

    @GetMapping("/balance/{userId}")
    public ResponseEntity<Map<String, Object>> getBalance(@PathVariable Long userId) {
        Double balance = walletService.getBalance(userId);
        Map<String, Object> response = new HashMap<>();
        response.put("balance", balance);
        response.put("currency", "XOF");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/recharge")
    public ResponseEntity<WalletTransaction> rechargeWallet(
            @RequestParam Long userId,
            @RequestParam Double amount,
            @RequestParam String paymentMethod,
            @RequestParam(required = false) String phoneNumber) {

        WalletTransaction transaction = walletService.rechargeWallet(userId, amount, paymentMethod, phoneNumber);
        return ResponseEntity.ok(transaction);
    }

    @PostMapping("/pay")
    public ResponseEntity<Map<String, Object>> payWithWallet(
            @RequestParam Long userId,
            @RequestParam Double amount,
            @RequestParam String rideId) {

        boolean success = walletService.debitWallet(userId, amount, rideId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "Paiement effectué depuis le porte-monnaie" : "Solde insuffisant");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/transactions/{userId}")
    public ResponseEntity<List<WalletTransaction>> getTransactions(@PathVariable Long userId) {
        return ResponseEntity.ok(walletService.getTransactionHistory(userId));
    }

    // ==================== QR CODE PAYMENT ====================

    @GetMapping("/qrcode/{userId}")
    public ResponseEntity<Map<String, String>> generateQRCode(
            @PathVariable Long userId,
            @RequestParam Double amount,
            @RequestParam Long rideId) {

        System.out.println("=== GÉNÉRATION QR CODE ===");
        System.out.println("userId: " + userId);
        System.out.println("amount: " + amount);
        System.out.println("rideId: " + rideId);

        String token = java.util.UUID.randomUUID().toString();
        String data = userId + "|" + amount + "|" + rideId;
        long expirationTime = System.currentTimeMillis() + 30 * 60 * 1000; // 30 minutes

        qrCodeTokens.put(token, data);
        tokenExpiration.put(token, expirationTime);

        System.out.println("✅ Token stocké: " + token);
        System.out.println("Data stockée: " + data);
        System.out.println("Expiration: " + new java.util.Date(expirationTime));
        System.out.println("Taille du cache: " + qrCodeTokens.size());

        // URL locale pour le scan (utilisée par l'application chauffeur)
        String qrData = "http://" + serverAddress + ":" + serverPort + "/api/wallet/qrcode/scan/" + token;
        String qrCodeUrl = "https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=" + qrData;

        System.out.println("QR Data URL: " + qrData);
        System.out.println("QR Code Image: " + qrCodeUrl);

        Map<String, String> response = new HashMap<>();
        response.put("qrCodeUrl", qrCodeUrl);
        response.put("token", token);
        response.put("amount", String.valueOf(amount));
        response.put("scanUrl", qrData); // Optionnel, pour débogage

        return ResponseEntity.ok(response);
    }

    @PostMapping("/qrcode/scan/{token}")
    public ResponseEntity<Map<String, Object>> scanQRCode(@PathVariable String token) {
        System.out.println("=== SCAN QR CODE ===");
        System.out.println("Token reçu: " + token);
        System.out.println("Tokens disponibles: " + qrCodeTokens.keySet());
        System.out.println("Taille du cache: " + qrCodeTokens.size());

        Map<String, Object> response = new HashMap<>();

        // Vérifier si le token existe
        String data = qrCodeTokens.get(token);
        if (data == null) {
            System.out.println("❌ Token non trouvé dans le cache");
            response.put("success", false);
            response.put("message", "QR Code invalide ou expiré. Veuillez en générer un nouveau.");
            return ResponseEntity.badRequest().body(response);
        }

        // Vérifier l'expiration
        Long expiration = tokenExpiration.get(token);
        if (expiration == null || System.currentTimeMillis() > expiration) {
            System.out.println("❌ Token expiré");
            qrCodeTokens.remove(token);
            tokenExpiration.remove(token);
            response.put("success", false);
            response.put("message", "QR Code expiré (30 minutes). Veuillez générer un nouveau QR Code.");
            return ResponseEntity.badRequest().body(response);
        }

        String[] parts = data.split("\\|");
        Long clientId = Long.parseLong(parts[0]);
        Double amount = Double.parseDouble(parts[1]);
        Long rideId = Long.parseLong(parts[2]);

        System.out.println("✅ Token valide !");
        System.out.println("Client ID: " + clientId);
        System.out.println("Montant: " + amount);
        System.out.println("Course ID: " + rideId);

        // Vérifier si la course existe et n'est pas déjà payée
        Ride ride = rideRepository.findById(rideId).orElse(null);
        if (ride == null) {
            System.out.println("❌ Course non trouvée: " + rideId);
            response.put("success", false);
            response.put("message", "Course non trouvée");
            qrCodeTokens.remove(token);
            tokenExpiration.remove(token);
            return ResponseEntity.badRequest().body(response);
        }

        if ("COMPLETED".equals(ride.getStatus())) {
            System.out.println("❌ Course déjà payée: " + rideId);
            response.put("success", false);
            response.put("message", "Cette course a déjà été payée");
            qrCodeTokens.remove(token);
            tokenExpiration.remove(token);
            return ResponseEntity.badRequest().body(response);
        }

        // Débiter le wallet du client
        boolean success = walletService.debitWallet(clientId, amount, rideId.toString());

        if (success) {
            qrCodeTokens.remove(token);
            tokenExpiration.remove(token);

            response.put("success", true);
            response.put("message", "Paiement de " + amount + " FCFA effectué");
            response.put("clientId", clientId);
            response.put("amount", amount);
            response.put("rideId", rideId);

            // Mettre à jour le statut de la course
            ride.setStatus("COMPLETED");
            rideRepository.save(ride);
            System.out.println("✅ Course #" + rideId + " marquée COMPLETED");

            try {
                notificationService.sendNotificationToUser(clientId,
                        "✅ Paiement effectué",
                        "Votre course a été payée : " + amount + " FCFA");
                System.out.println("✅ Notification envoyée au client " + clientId);
            } catch (Exception e) {
                System.err.println("Erreur envoi notification: " + e.getMessage());
            }
        } else {
            System.out.println("❌ Solde insuffisant pour le client " + clientId);
            response.put("success", false);
            response.put("message", "Solde insuffisant");
        }

        return ResponseEntity.ok(response);
    }

    // ==================== COUPON ENDPOINTS ====================

    @PostMapping("/coupons/generate")
    public ResponseEntity<List<RechargeCoupon>> generateCoupons(
            @RequestParam int count,
            @RequestParam double amount,
            @RequestParam String createdBy,
            @RequestParam(defaultValue = "30") int validityDays) {

        List<RechargeCoupon> coupons = walletService.generateCoupons(count, amount, createdBy, validityDays);
        return ResponseEntity.ok(coupons);
    }

    @PostMapping("/coupons/redeem")
    public ResponseEntity<Map<String, Object>> redeemCoupon(
            @RequestParam String code,
            @RequestParam Long userId) {

        Map<String, Object> result = walletService.useCoupon(code, userId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/coupons/all")
    public ResponseEntity<List<RechargeCoupon>> getAllCoupons() {
        return ResponseEntity.ok(walletService.getAllCoupons());
    }
}