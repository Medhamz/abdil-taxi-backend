package com.abdil.taxi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/version")
@CrossOrigin(origins = "*")
public class AppVersionController {

    // ✅ Version minimale requise pour l'application client
    private static int MIN_CLIENT_VERSION_CODE = 2;
    private static String MIN_CLIENT_VERSION_NAME = "2.0";

    // ✅ Version minimale requise pour l'application chauffeur
    private static int MIN_DRIVER_VERSION_CODE = 2;
    private static String MIN_DRIVER_VERSION_NAME = "2.0";

    @GetMapping("/client")
    public ResponseEntity<Map<String, Object>> getClientVersion() {
        Map<String, Object> response = new HashMap<>();
        response.put("minVersionCode", MIN_CLIENT_VERSION_CODE);
        response.put("minVersionName", MIN_CLIENT_VERSION_NAME);
        response.put("forceUpdate", true);
        response.put("updateUrl", "https://play.google.com/store/apps/details?id=com.abdil.taxi");
        response.put("message", "📱 Une nouvelle version est disponible !\n\nVeuillez mettre à jour l'application pour continuer à utiliser Abdil Taxi.\n\nVersion actuelle: " + getCurrentClientVersion() + "\nVersion requise: " + MIN_CLIENT_VERSION_NAME);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/driver")
    public ResponseEntity<Map<String, Object>> getDriverVersion() {
        Map<String, Object> response = new HashMap<>();
        response.put("minVersionCode", MIN_DRIVER_VERSION_CODE);
        response.put("minVersionName", MIN_DRIVER_VERSION_NAME);
        response.put("forceUpdate", true);
        response.put("updateUrl", "https://play.google.com/store/apps/details?id=com.abdil.driver");
        response.put("message", "📱 Une nouvelle version est disponible !\n\nVeuillez mettre à jour l'application pour continuer à utiliser Abdil Driver.\n\nVersion actuelle: " + getCurrentDriverVersion() + "\nVersion requise: " + MIN_DRIVER_VERSION_NAME);
        return ResponseEntity.ok(response);
    }

    // ✅ ENDPOINT POUR METTRE À JOUR LES VERSIONS DEPUIS L'ADMIN
    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> updateVersions(
            @RequestParam int clientVersion,
            @RequestParam int driverVersion) {

        MIN_CLIENT_VERSION_CODE = clientVersion;
        MIN_CLIENT_VERSION_NAME = clientVersion + ".0";
        MIN_DRIVER_VERSION_CODE = driverVersion;
        MIN_DRIVER_VERSION_NAME = driverVersion + ".0";

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Versions mises à jour avec succès !");
        response.put("clientVersion", MIN_CLIENT_VERSION_CODE);
        response.put("driverVersion", MIN_DRIVER_VERSION_CODE);

        System.out.println("✅ Versions mises à jour - Client: " + MIN_CLIENT_VERSION_CODE + ", Driver: " + MIN_DRIVER_VERSION_CODE);

        return ResponseEntity.ok(response);
    }

    // ✅ ENDPOINT POUR RÉCUPÉRER LES VERSIONS ACTUELLES
    @GetMapping("/current")
    public ResponseEntity<Map<String, Object>> getCurrentVersions() {
        Map<String, Object> response = new HashMap<>();
        response.put("clientVersion", MIN_CLIENT_VERSION_CODE);
        response.put("driverVersion", MIN_DRIVER_VERSION_CODE);
        return ResponseEntity.ok(response);
    }

    private int getCurrentClientVersion() {
        try {
            // Cette méthode serait idéalement liée à la base de données
            return MIN_CLIENT_VERSION_CODE;
        } catch (Exception e) {
            return 1;
        }
    }

    private int getCurrentDriverVersion() {
        try {
            return MIN_DRIVER_VERSION_CODE;
        } catch (Exception e) {
            return 1;
        }
    }
}