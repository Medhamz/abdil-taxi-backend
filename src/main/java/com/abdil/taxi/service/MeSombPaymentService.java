package com.abdil.taxi.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class MeSombPaymentService {

    @Value("${mesomb.application.key}")
    private String applicationKey;

    @Value("${mesomb.access.key}")
    private String accessKey;

    @Value("${mesomb.secret.key}")
    private String secretKey;

    @Value("${mesomb.mode:sandbox}")
    private String mode;

    private final RestTemplate restTemplate = new RestTemplate();

    private String getBaseUrl() {
        return "sandbox".equals(mode)
                ? "https://sandbox.mesomb.hachther.com/api/v1.0/"
                : "https://mesomb.hachther.com/api/v1.0/";
    }

    /**
     * Initier un paiement avec MeSomb
     */
    public Map<String, Object> initiatePayment(String phoneNumber, double amount,
                                               String rideId, String customerName,
                                               String paymentMethod, String email) {
        Map<String, Object> result = new HashMap<>();

        try {
            // Convertir le montant en centimes (comme demandé par MeSomb)
            int amountInCents = (int) (amount * 100);

            // Déterminer le service MeSomb
            String service = "";
            switch (paymentMethod) {
                case "ORANGE_MONEY":
                    service = "ORANGE_MONEY";
                    break;
                case "AIRTEL_MONEY":
                    service = "AIRTEL_MONEY";
                    break;
                case "CARD":
                    service = "CARD";
                    break;
                default:
                    service = "ORANGE_MONEY";
            }

            // Construction du body selon la documentation MeSomb
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("amount", amountInCents);
            requestBody.put("currency", "XOF");
            requestBody.put("service", service);
            requestBody.put("payer", phoneNumber);
            requestBody.put("country", "NE");
            requestBody.put("trxID", "RIDE_" + rideId + "_" + System.currentTimeMillis());

            // Informations client
            Map<String, String> customer = new HashMap<>();
            customer.put("name", customerName);
            customer.put("email", email);
            customer.put("phone_number", phoneNumber);
            requestBody.put("customer", customer);

            // Headers d'authentification MeSomb
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-application-key", applicationKey);
            headers.set("x-access-key", accessKey);
            headers.set("x-secret-key", secretKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Appel API MeSomb
            String url = getBaseUrl() + "payment/initiate/";
            System.out.println("=== APPEL API MeSomb ===");
            System.out.println("URL: " + url);
            System.out.println("Service: " + service);
            System.out.println("Montant: " + amountInCents + " XOF");
            System.out.println("Téléphone: " + phoneNumber);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map body = response.getBody();
                Boolean success = (Boolean) body.get("success");

                if (success != null && success) {
                    Map data = (Map) body.get("data");
                    result.put("success", true);
                    result.put("transactionId", data.get("trxID"));
                    result.put("status", data.get("status"));
                    result.put("message", "Paiement initié avec succès");
                    result.put("paymentUrl", data.get("payment_url"));
                } else {
                    result.put("success", false);
                    result.put("message", body.get("message") != null ? body.get("message") : "Erreur lors du paiement");
                }
            } else {
                result.put("success", false);
                result.put("message", "Erreur de communication avec MeSomb");
            }

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Vérifier le statut d'un paiement
     */
    public Map<String, Object> checkPaymentStatus(String transactionId) {
        Map<String, Object> result = new HashMap<>();

        try {
            String url = getBaseUrl() + "payment/status/" + transactionId + "/";

            HttpHeaders headers = new HttpHeaders();
            headers.set("x-application-key", applicationKey);
            headers.set("x-access-key", accessKey);
            headers.set("x-secret-key", secretKey);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map body = response.getBody();
                Boolean success = (Boolean) body.get("success");

                if (success != null && success) {
                    Map data = (Map) body.get("data");
                    result.put("success", true);
                    result.put("status", data.get("status"));
                    result.put("message", "Transaction trouvée");
                } else {
                    result.put("success", false);
                    result.put("message", "Transaction non trouvée");
                }
            } else {
                result.put("success", false);
                result.put("message", "Erreur de vérification");
            }

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }

        return result;
    }
}