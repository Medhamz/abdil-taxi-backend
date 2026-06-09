package com.abdil.taxi.controller;

import com.abdil.taxi.model.License;
import com.abdil.taxi.service.LicenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/licenses")
@CrossOrigin(origins = "*")
public class LicenseController {

    @Autowired
    private LicenseService licenseService;

    @PostMapping("/generate")
    public ResponseEntity<License> generateLicense(@RequestBody Map<String, Object> request) {
        String licenseType = (String) request.get("licenseType");
        Integer durationDays = (Integer) request.get("durationDays");
        Integer price = (Integer) request.get("price");
        String appType = (String) request.get("appType");
        Long userId = request.get("userId") != null ? Long.valueOf(request.get("userId").toString()) : null;
        String userType = (String) request.get("userType");
        String createdBy = (String) request.get("createdBy");

        if (createdBy == null) createdBy = "ADMIN";

        License license = licenseService.generateLicense(
                licenseType, durationDays, price, appType, userId, userType, createdBy
        );
        return ResponseEntity.ok(license);
    }

    @GetMapping("/all")
    public ResponseEntity<List<License>> getAllLicenses() {
        return ResponseEntity.ok(licenseService.getAllLicenses());
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getLicenseStats() {
        return ResponseEntity.ok(licenseService.getLicenseStats());
    }

    @PutMapping("/revoke/{id}")
    public ResponseEntity<License> revokeLicense(@PathVariable Long id) {
        return ResponseEntity.ok(licenseService.revokeLicense(id));
    }

    @PutMapping("/activate/{id}")
    public ResponseEntity<License> activateLicense(@PathVariable Long id) {
        return ResponseEntity.ok(licenseService.activateLicense(id));
    }

    @GetMapping("/verify/{licenseKey}")
    public ResponseEntity<Map<String, Object>> verifyLicense(
            @PathVariable String licenseKey,
            @RequestParam String appType,
            @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(licenseService.verifyLicense(licenseKey, appType, userId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<License>> getUserLicenses(
            @PathVariable Long userId,
            @RequestParam String appType) {
        return ResponseEntity.ok(licenseService.getUserLicenses(userId, appType));
    }

    @GetMapping("/user/{userId}/active")
    public ResponseEntity<Map<String, Object>> getUserActiveLicense(
            @PathVariable Long userId,
            @RequestParam String appType) {
        var licenseOpt = licenseService.getUserActiveLicense(userId, appType);
        if (licenseOpt.isPresent()) {
            License license = licenseOpt.get();
            Map<String, Object> result = Map.of(
                    "valid", true,
                    "license", license,
                    "message", "Licence active"
            );
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.ok(Map.of("valid", false, "message", "Aucune licence active"));
        }
    }

    @PostMapping("/check-expired")
    public ResponseEntity<Map<String, Object>> checkExpiredLicenses() {
        int count = licenseService.updateExpiredLicenses();
        return ResponseEntity.ok(Map.of("updated", count, "message", count + " licences expirées mises à jour"));
    }
}