package com.abdil.taxi.controller;

import com.abdil.taxi.model.Dispute;
import com.abdil.taxi.model.Ride;
import com.abdil.taxi.repository.DisputeRepository;
import com.abdil.taxi.repository.RideRepository;
import com.abdil.taxi.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/disputes")
@CrossOrigin(origins = "*")
public class DisputeController {

    @Autowired
    private DisputeRepository disputeRepository;

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private NotificationService notificationService;

    // ✅ POST /api/disputes (sans /create pour correspondre au client)
    @PostMapping
    public ResponseEntity<Map<String, Object>> createDispute(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            Long rideId = ((Number) request.get("rideId")).longValue();
            Long clientId = ((Number) request.get("clientId")).longValue();
            String reason = (String) request.get("reason");
            String description = (String) request.get("description");

            Ride ride = rideRepository.findById(rideId).orElse(null);
            if (ride == null) {
                response.put("success", false);
                response.put("message", "Course non trouvée");
                return ResponseEntity.badRequest().body(response);
            }

            Dispute dispute = new Dispute();
            dispute.setRideId(rideId);
            dispute.setClientId(clientId);
            dispute.setDriverId(ride.getDriverId());
            dispute.setReason(reason);
            dispute.setDescription(description);
            dispute.setStatus("PENDING");
            dispute.setCreatedAt(LocalDateTime.now());

            Dispute saved = disputeRepository.save(dispute);

            // Notifier l'admin
            System.out.println("📢 Nouveau litige créé: ID=" + saved.getId() + " pour course " + rideId);

            response.put("success", true);
            response.put("message", "Litige enregistré avec succès");
            response.put("disputeId", saved.getId());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ✅ GET /api/disputes/client/{clientId}
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<Dispute>> getClientDisputes(@PathVariable Long clientId) {
        return ResponseEntity.ok(disputeRepository.findByClientId(clientId));
    }

    // ✅ GET /api/disputes/driver/{driverId}
    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<Dispute>> getDriverDisputes(@PathVariable Long driverId) {
        return ResponseEntity.ok(disputeRepository.findByDriverId(driverId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Dispute>> getAllDisputes() {
        return ResponseEntity.ok(disputeRepository.findAll());
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Dispute>> getPendingDisputes() {
        return ResponseEntity.ok(disputeRepository.findByStatus("PENDING"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getDisputeDetails(@PathVariable Long id) {
        return disputeRepository.findById(id)
                .map(dispute -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("id", dispute.getId());
                    result.put("rideId", dispute.getRideId());
                    result.put("clientId", dispute.getClientId());
                    result.put("driverId", dispute.getDriverId());
                    result.put("reason", dispute.getReason());
                    result.put("description", dispute.getDescription());
                    result.put("status", dispute.getStatus());
                    result.put("resolution", dispute.getResolution());
                    result.put("refundAmount", dispute.getRefundAmount());
                    result.put("createdAt", dispute.getCreatedAt());
                    result.put("resolvedAt", dispute.getResolvedAt());
                    return ResponseEntity.ok(result);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/resolve/{id}")
    public ResponseEntity<Map<String, Object>> resolveDispute(@PathVariable Long id,
                                                              @RequestParam String resolution,
                                                              @RequestParam Double refundAmount) {
        Map<String, Object> response = new HashMap<>();

        Dispute dispute = disputeRepository.findById(id).orElse(null);
        if (dispute == null) {
            response.put("success", false);
            response.put("message", "Litige non trouvé");
            return ResponseEntity.notFound().build();
        }

        dispute.setStatus("RESOLVED");
        dispute.setResolution(resolution);
        dispute.setRefundAmount(refundAmount);
        dispute.setResolvedAt(LocalDateTime.now());

        Dispute saved = disputeRepository.save(dispute);

        // Notifier le client
        try {
            notificationService.sendNotificationToClient(
                    dispute.getClientId(),
                    "✅ Litige résolu",
                    "Votre litige a été résolu. " + resolution
            );
        } catch (Exception e) {
            System.err.println("Erreur notification: " + e.getMessage());
        }

        response.put("success", true);
        response.put("message", "Litige résolu avec succès");
        response.put("dispute", saved);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/reject/{id}")
    public ResponseEntity<Map<String, Object>> rejectDispute(@PathVariable Long id, @RequestParam String reason) {
        Map<String, Object> response = new HashMap<>();

        Dispute dispute = disputeRepository.findById(id).orElse(null);
        if (dispute == null) {
            response.put("success", false);
            response.put("message", "Litige non trouvé");
            return ResponseEntity.notFound().build();
        }

        dispute.setStatus("REJECTED");
        dispute.setResolution(reason);
        dispute.setResolvedAt(LocalDateTime.now());

        Dispute saved = disputeRepository.save(dispute);

        response.put("success", true);
        response.put("message", "Litige rejeté");
        response.put("dispute", saved);
        return ResponseEntity.ok(response);
    }
}