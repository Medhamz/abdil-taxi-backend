package com.abdil.taxi.controller;

import com.abdil.taxi.service.CryptoPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/crypto")
@CrossOrigin(origins = "*")
public class CryptoPaymentController {

    @Autowired
    private CryptoPaymentService cryptoPaymentService;

    /**
     * Générer une adresse de paiement crypto
     */
    @PostMapping("/generate-address")
    public ResponseEntity<Map<String, String>> generatePaymentAddress(
            @RequestParam Long rideId,
            @RequestParam Double amount,
            @RequestParam(defaultValue = "USDT") String currency) {

        Map<String, String> response = cryptoPaymentService.generatePaymentAddress(rideId, amount, currency);
        return ResponseEntity.ok(response);
    }

    /**
     * Vérifier le statut d'une transaction crypto
     */
    @GetMapping("/check-status")
    public ResponseEntity<Map<String, Object>> checkStatus(
            @RequestParam String transactionId,
            @RequestParam Double amount) {

        Map<String, Object> response = cryptoPaymentService.checkTransactionStatus(transactionId, amount);
        return ResponseEntity.ok(response);
    }

    /**
     * Créer une facture crypto
     */
    @PostMapping("/create-invoice")
    public ResponseEntity<Map<String, Object>> createInvoice(
            @RequestParam Long rideId,
            @RequestParam Double amount,
            @RequestParam(defaultValue = "USDT") String currency,
            @RequestParam(defaultValue = "30") int expiryMinutes) {

        Map<String, Object> invoice = cryptoPaymentService.createCryptoInvoice(rideId, amount, currency, expiryMinutes);
        return ResponseEntity.ok(invoice);
    }

    /**
     * Webhook pour les notifications de paiement crypto
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody String payload) {
        System.out.println("=== Webhook Crypto reçu ===");
        System.out.println(payload);
        // I vous mettrez à jour le statut de la course
        return ResponseEntity.ok("OK");
    }
}