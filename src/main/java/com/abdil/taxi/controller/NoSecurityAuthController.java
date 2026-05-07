package com.abdil.taxi.controller;

import com.abdil.taxi.model.*;
import com.abdil.taxi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/noauth")
@CrossOrigin(origins = "*")
public class NoSecurityAuthController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        try {
            if (userRepository.existsByEmail(request.getEmail())) {
                return ResponseEntity.badRequest().body(new AuthResponse(null, "Email déjà utilisé", false, null));
            }

            User user = new User();
            user.setFullName(request.getFullName());
            user.setEmail(request.getEmail());
            user.setPhone(request.getPhone());
            user.setPassword(request.getPassword()); // 🔴 PAS DE HASH POUR LE MOMENT
            user.setRole("CLIENT");

            User savedUser = userRepository.save(user);
            return ResponseEntity.ok(new AuthResponse(null, "Inscription réussie", true, savedUser));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse(null, "Erreur: " + e.getMessage(), false, null));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user == null || !user.getPassword().equals(request.getPassword())) {
            return ResponseEntity.badRequest().body(new AuthResponse(null, "Email ou mot de passe incorrect", false, null));
        }
        return ResponseEntity.ok(new AuthResponse(null, "Connexion réussie", true, user));
    }
}