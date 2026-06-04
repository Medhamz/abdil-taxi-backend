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
@RequestMapping("/api/nfc-payment")
@CrossOrigin(origins = "*")
public class NfcPaymentController {

    @Autowired
    private WalletService walletService;

    @Autowired
    private RideRepository rideRepository;

    // Stockage temporaire des tokens NFC
    private final Map<String, NfcSession> nfcSessions = new ConcurrentHashMap<>();

    static class NfcSession {
        String clientId;
        String rideId;
        Double amount;
        long createdAt;

        NfcSession(String clientId, String rideId, Double amount) {
            this.clientId = clientId;
            this.rideId = rideId;
            this.amount = amount;
            this.createdAt = System.currentTimeMillis();
        }
    }

    @PostMapping("/initiate")
    public ResponseEntity<Map<String, String>> initiateNfcPayment(
            @RequestParam Long clientId,
            @RequestParam Long rideId,
            @RequestParam Double amount) {

        System.out.println("=== INITIATION NFC (CLIENT) ===");
        System.out.println("clientId: " + clientId);
        System.out.println("rideId: " + rideId);
        System.out.println("amount: " + amount);

        String nfcToken = UUID.randomUUID().toString();
        nfcSessions.put(nfcToken, new NfcSession(String.valueOf(clientId), String.valueOf(rideId), amount));

        Map<String, String> response = new HashMap<>();
        response.put("nfcToken", nfcToken);
        response.put("amount", String.valueOf(amount));
        response.put("rideId", String.valueOf(rideId));

        // Programmer la suppression du token après 2 minutes
        new Thread(() -> {
            try {
                Thread.sleep(2 * 60 * 1000);
                nfcSessions.remove(nfcToken);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/process")
    public ResponseEntity<Map<String, Object>> processNfcPayment(
            @RequestParam String nfcToken,
            @RequestParam Long driverId) {

        System.out.println("=== TRAITEMENT NFC (CHAUFFEUR) ===");
        System.out.println("nfcToken: " + nfcToken);
        System.out.println("driverId: " + driverId);

        Map<String, Object> response = new HashMap<>();

        NfcSession session = nfcSessions.get(nfcToken);
        if (session == null) {
            response.put("success", false);
            response.put("message", "Session NFC expirée ou invalide");
            return ResponseEntity.badRequest().body(response);
        }

        Long clientId = Long.parseLong(session.clientId);
        Long rideId = Long.parseLong(session.rideId);
        Double amount = session.amount;

        // Vérifier la course
        Ride ride = rideRepository.findById(rideId).orElse(null);
        if (ride == null) {
            response.put("success", false);
            response.put("message", "Course non trouvée");
            return ResponseEntity.badRequest().body(response);
        }

        // Vérifier le statut
        if (!"ACCEPTED".equals(ride.getStatus()) && !"STARTED".equals(ride.getStatus())) {
            response.put("success", false);
            response.put("message", "La course n'a pas encore été acceptée par le chauffeur");
            return ResponseEntity.badRequest().body(response);
        }

        if ("COMPLETED".equals(ride.getStatus())) {
            response.put("success", false);
            response.put("message", "Cette course a déjà été payée");
            return ResponseEntity.badRequest().body(response);
        }

        // Débiter le client
        boolean success = walletService.debitWallet(clientId, amount, rideId.toString());

        if (success) {
            nfcSessions.remove(nfcToken);
            ride.setStatus("COMPLETED");
            ride.setCompletedAt(LocalDateTime.now());
            rideRepository.save(ride);

            response.put("success", true);
            response.put("message", "Paiement NFC effectué avec succès !");
            response.put("rideId", rideId);
            response.put("amount", amount);
        } else {
            response.put("success", false);
            response.put("message", "Solde client insuffisant");
        }

        return ResponseEntity.ok(response);
    }
}