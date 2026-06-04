package com.abdil.taxi.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/audio")
@CrossOrigin(origins = "*")
public class AudioController {

    @Value("${audio.upload.dir:./uploads/audio}")
    private String uploadDir;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadAudio(@RequestParam("file") MultipartFile file) {
        System.out.println("=== UPLOAD AUDIO ===");
        System.out.println("Nom du fichier: " + file.getOriginalFilename());
        System.out.println("Taille: " + file.getSize() + " bytes");

        try {
            // Créer le dossier s'il n'existe pas
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                boolean created = directory.mkdirs();
                System.out.println("Dossier créé: " + created + " - " + directory.getAbsolutePath());
            }

            // Générer un nom unique pour le fichier
            String fileName = UUID.randomUUID().toString() + ".3gp";
            Path filePath = Paths.get(uploadDir, fileName);
            System.out.println("Chemin complet: " + filePath.toAbsolutePath());

            // Sauvegarder le fichier
            Files.write(filePath, file.getBytes());
            System.out.println("✅ Fichier sauvegardé avec succès");

            // URL pour accéder au fichier
            String fileUrl = "/api/audio/download/" + fileName;

            Map<String, String> response = new HashMap<>();
            response.put("url", fileUrl);
            response.put("fileName", fileName);

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            System.err.println("❌ Erreur sauvegarde: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> downloadAudio(@PathVariable String fileName) {
        System.out.println("=== DOWNLOAD AUDIO ===");
        System.out.println("Fichier demandé: " + fileName);

        try {
            Path filePath = Paths.get(uploadDir, fileName);
            System.out.println("Chemin: " + filePath.toAbsolutePath());

            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                System.out.println("❌ Fichier non trouvé!");
                return ResponseEntity.notFound().build();
            }

            System.out.println("✅ Fichier trouvé, envoi en cours...");
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("audio/3gpp"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                    .body(resource);

        } catch (IOException e) {
            System.err.println("❌ Erreur download: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}