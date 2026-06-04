package com.abdil.taxi.controller;

import com.abdil.taxi.model.PaymentRequest;
import com.abdil.taxi.model.PaymentResponse;
import com.abdil.taxi.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/initiate")
    public ResponseEntity<PaymentResponse> initiatePayment(@RequestBody PaymentRequest request) {
        System.out.println("=== INITIATION PAIEMENT ===");
        System.out.println("Montant: " + request.getAmount());
        System.out.println("Méthode: " + request.getPaymentMethod());
        System.out.println("Téléphone: " + request.getPhoneNumber());
        System.out.println("Client: " + request.getCustomerName());

        PaymentResponse response = paymentService.initiatePayment(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify/{transactionId}")
    public ResponseEntity<PaymentResponse> verifyPayment(@PathVariable String transactionId) {
        PaymentResponse response = paymentService.verifyPayment(transactionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/methods")
    public ResponseEntity<String[]> getPaymentMethods() {
        String[] methods = {"CASH", "ORANGE_MONEY", "AIRTEL_MONEY", "MYNITA", "AMANATA", "CARD"};
        return ResponseEntity.ok(methods);
    }

    // ✅ Webhook pour les notifications MeSomb
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody String payload) {
        System.out.println("=== WEBHOOK RECU ===");
        System.out.println(payload);
        // Ici vous mettrez à jour le statut du paiement dans la base de données
        return ResponseEntity.ok("OK");
    }
}