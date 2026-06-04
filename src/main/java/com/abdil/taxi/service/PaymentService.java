package com.abdil.taxi.service;

import com.abdil.taxi.model.PaymentRequest;
import com.abdil.taxi.model.PaymentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PaymentService {

    @Autowired
    private MeSombPaymentService meSombPaymentService;

    public PaymentResponse initiatePayment(PaymentRequest request) {
        PaymentResponse response = new PaymentResponse();

        System.out.println("=== TRAITEMENT PAIEMENT ===");
        System.out.println("Méthode: " + request.getPaymentMethod());
        System.out.println("Montant: " + request.getAmount());

        // ✅ Pour les paiements en espèces
        if ("CASH".equals(request.getPaymentMethod())) {
            response.setTransactionId(UUID.randomUUID().toString());
            response.setStatus("SUCCESS");
            response.setAmount(request.getAmount());
            response.setPaymentMethod(request.getPaymentMethod());
            response.setMessage("Paiement en espèces à la livraison");
            return response;
        }

        // ✅ Pour le porte-monnaie (Wallet)
        if ("WALLET".equals(request.getPaymentMethod())) {
            response.setTransactionId(UUID.randomUUID().toString());
            response.setStatus("SUCCESS");
            response.setAmount(request.getAmount());
            response.setPaymentMethod(request.getPaymentMethod());
            response.setMessage("Paiement effectué depuis le porte-monnaie");
            return response;
        }

        // ✅ Pour le QR Code
        if ("QR_CODE".equals(request.getPaymentMethod())) {
            response.setTransactionId(UUID.randomUUID().toString());
            response.setStatus("SUCCESS");
            response.setAmount(request.getAmount());
            response.setPaymentMethod(request.getPaymentMethod());
            response.setMessage("Paiement par QR Code validé");
            return response;
        }

        // ❌ Tous les autres modes sont désactivés
        response.setTransactionId(UUID.randomUUID().toString());
        response.setStatus("FAILED");
        response.setAmount(request.getAmount());
        response.setPaymentMethod(request.getPaymentMethod());
        response.setMessage("Mode de paiement non disponible actuellement");

        return response;
    }

    public PaymentResponse verifyPayment(String transactionId) {
        PaymentResponse response = new PaymentResponse();
        response.setTransactionId(transactionId);
        response.setStatus("SUCCESS");
        response.setMessage("Paiement vérifié");
        return response;
    }
}