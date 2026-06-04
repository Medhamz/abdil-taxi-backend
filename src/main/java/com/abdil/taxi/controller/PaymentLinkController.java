package com.abdil.taxi.controller;

import com.abdil.taxi.model.Ride;
import com.abdil.taxi.repository.RideRepository;
import com.abdil.taxi.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/payment-link")
@CrossOrigin(origins = "*")
public class PaymentLinkController {

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private WalletService walletService;

    private final Map<String, String> paymentLinks = new ConcurrentHashMap<>();
    private final Map<String, Long> linkExpiration = new ConcurrentHashMap<>();

    @PostMapping("/generate")
    public ResponseEntity<Map<String, String>> generatePaymentLink(
            @RequestParam Long rideId,
            @RequestParam Long clientId,
            @RequestParam Double amount) {

        System.out.println("=== GÉNÉRATION LIEN PAIEMENT ===");
        System.out.println("rideId: " + rideId);
        System.out.println("clientId: " + clientId);
        System.out.println("amount: " + amount);

        String linkId = UUID.randomUUID().toString();
        String paymentLink = "http://192.168.11.101:8080/api/payment-link/pay/" + linkId;

        String data = clientId + "|" + amount + "|" + rideId;
        paymentLinks.put(linkId, data);
        linkExpiration.put(linkId, System.currentTimeMillis() + 30 * 60 * 1000); // 30 minutes

        Map<String, String> response = new HashMap<>();
        response.put("paymentLink", paymentLink);
        response.put("linkId", linkId);
        response.put("expiresIn", "30 minutes");

        return ResponseEntity.ok(response);
    }

    // ✅ Méthode GET pour le navigateur
    @GetMapping("/pay/{linkId}")
    public ResponseEntity<Map<String, Object>> payWithLinkGet(@PathVariable String linkId) {
        return processPayment(linkId);
    }

    // ✅ Méthode POST pour l'application chauffeur
    @PostMapping("/pay/{linkId}")
    public ResponseEntity<Map<String, Object>> payWithLinkPost(@PathVariable String linkId) {
        return processPayment(linkId);
    }

    // ✅ Méthode commune pour traiter le paiement
    private ResponseEntity<Map<String, Object>> processPayment(String linkId) {
        System.out.println("=== PAIEMENT PAR LIEN ===");
        System.out.println("linkId: " + linkId);

        Map<String, Object> response = new HashMap<>();

        String data = paymentLinks.get(linkId);
        Long expiration = linkExpiration.get(linkId);

        if (data == null || expiration == null || System.currentTimeMillis() > expiration) {
            response.put("success", false);
            response.put("message", "Lien de paiement expiré ou invalide");
            return ResponseEntity.badRequest().body(response);
        }

        String[] parts = data.split("\\|");
        Long clientId = Long.parseLong(parts[0]);
        Double amount = Double.parseDouble(parts[1]);
        Long rideId = Long.parseLong(parts[2]);

        // ✅ VÉRIFIER LE STATUT DE LA COURSE
        Ride ride = rideRepository.findById(rideId).orElse(null);
        if (ride == null) {
            response.put("success", false);
            response.put("message", "Course non trouvée");
            return ResponseEntity.badRequest().body(response);
        }

        // ✅ Vérifier que la course a été acceptée par le chauffeur
        if (!"ACCEPTED".equals(ride.getStatus()) && !"STARTED".equals(ride.getStatus())) {
            response.put("success", false);
            response.put("message", "Veuillez attendre que le chauffeur accepte la course avant de payer");
            return ResponseEntity.badRequest().body(response);
        }

        // ✅ Vérifier que la course n'est pas déjà payée
        if ("COMPLETED".equals(ride.getStatus())) {
            response.put("success", false);
            response.put("message", "Cette course a déjà été payée");
            return ResponseEntity.badRequest().body(response);
        }

        boolean success = walletService.debitWallet(clientId, amount, rideId.toString());

        if (success) {
            paymentLinks.remove(linkId);
            linkExpiration.remove(linkId);

            ride.setStatus("COMPLETED");
            ride.setCompletedAt(LocalDateTime.now());
            rideRepository.save(ride);

            response.put("success", true);
            response.put("message", "Paiement effectué avec succès");
            response.put("rideId", rideId);
            response.put("amount", amount);
        } else {
            response.put("success", false);
            response.put("message", "Solde insuffisant");
        }

        return ResponseEntity.ok(response);
    }
}