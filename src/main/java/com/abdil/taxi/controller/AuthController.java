package com.abdil.taxi.controller;

import com.abdil.taxi.model.*;
import com.abdil.taxi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        try {
            System.out.println("=== INSCRIPTION RECUE ===");
            System.out.println("Nom: " + request.getFullName());
            System.out.println("Email: " + request.getEmail());
            System.out.println("Téléphone: " + request.getPhone());

            // Vérifier si l'email existe déjà
            if (userRepository.existsByEmail(request.getEmail())) {
                System.out.println("❌ Email déjà utilisé: " + request.getEmail());
                return ResponseEntity.badRequest().body(new AuthResponse(null, "Cet email est déjà utilisé", false, null));
            }

            // Vérifier si le téléphone existe déjà
            if (userRepository.existsByPhone(request.getPhone())) {
                System.out.println("❌ Téléphone déjà utilisé: " + request.getPhone());
                return ResponseEntity.badRequest().body(new AuthResponse(null, "Ce numéro de téléphone est déjà utilisé", false, null));
            }

            // Créer le nouvel utilisateur
            User user = new User();
            user.setFullName(request.getFullName());
            user.setEmail(request.getEmail());
            user.setPhone(request.getPhone());
            user.setPassword(request.getPassword()); // Pas de hash pour le moment
            user.setRole("CLIENT");

            User savedUser = userRepository.save(user);
            System.out.println("✅ Utilisateur créé avec ID: " + savedUser.getId());

            return ResponseEntity.ok(new AuthResponse(null, "Inscription réussie", true, savedUser));

        } catch (Exception e) {
            System.out.println("❌ Exception: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse(null, "Erreur: " + e.getMessage(), false, null));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        try {
            System.out.println("=== TENTATIVE DE CONNEXION ===");
            System.out.println("Email: " + request.getEmail());

            User user = userRepository.findByEmail(request.getEmail()).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(new AuthResponse(null, "Email ou mot de passe incorrect", false, null));
            }

            if (!user.getPassword().equals(request.getPassword())) {
                return ResponseEntity.badRequest().body(new AuthResponse(null, "Email ou mot de passe incorrect", false, null));
            }

            System.out.println("✅ Connexion réussie pour: " + user.getEmail());

            return ResponseEntity.ok(new AuthResponse(null, "Connexion réussie", true, user));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse(null, "Erreur: " + e.getMessage(), false, null));
        }
    }
}