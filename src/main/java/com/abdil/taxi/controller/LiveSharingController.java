package com.abdil.taxi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/live")
@CrossOrigin(origins = "*")
public class LiveSharingController {

    private Map<String, LiveShare> activeShares = new ConcurrentHashMap<>();

    @PostMapping("/create/{rideId}")
    public ResponseEntity<Map<String, String>> createShare(@PathVariable Long rideId) {
        String shareCode = generateShareCode();
        activeShares.put(shareCode, new LiveShare(shareCode, rideId, System.currentTimeMillis()));

        Map<String, String> response = new HashMap<>();
        response.put("shareCode", shareCode);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/update/{shareCode}")
    public ResponseEntity<Void> updateLocation(@PathVariable String shareCode,
                                               @RequestParam double lat,
                                               @RequestParam double lng) {
        LiveShare share = activeShares.get(shareCode);
        if (share != null) {
            share.lastLat = lat;
            share.lastLng = lng;
            share.lastUpdate = System.currentTimeMillis();
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/viewers/{shareCode}")
    public ResponseEntity<Map<String, Object>> getViewers(@PathVariable String shareCode) {
        Map<String, Object> response = new HashMap<>();
        response.put("viewerCount", 0); // À implémenter avec WebSocket
        return ResponseEntity.ok(response);
    }

    private String generateShareCode() {
        return UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    static class LiveShare {
        String shareCode;
        Long rideId;
        double lastLat, lastLng;
        long lastUpdate;
        int viewerCount = 0;

        LiveShare(String code, Long rideId, long time) {
            this.shareCode = code;
            this.rideId = rideId;
            this.lastUpdate = time;
        }
    }

    @DeleteMapping("/stop/{shareCode}")
    public ResponseEntity<Void> stopLiveShare(@PathVariable String shareCode) {
        LiveShare removed = activeShares.remove(shareCode);
        if (removed != null) {
            System.out.println("🛑 Live share arrêté: " + shareCode);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/position/{shareCode}")
    public ResponseEntity<Map<String, Object>> getPosition(@PathVariable String shareCode) {
        LiveShare share = activeShares.get(shareCode);

        Map<String, Object> response = new HashMap<>();

        if (share != null && (System.currentTimeMillis() - share.lastUpdate) < 60000) {
            response.put("lat", share.lastLat);
            response.put("lng", share.lastLng);
            response.put("isActive", true);
            response.put("lastUpdate", share.lastUpdate);
        } else {
            response.put("isActive", false);
            if (share != null) activeShares.remove(shareCode);
        }

        return ResponseEntity.ok(response);
    }
}