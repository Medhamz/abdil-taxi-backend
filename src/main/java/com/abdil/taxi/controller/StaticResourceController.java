package com.abdil.taxi.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.io.InputStream;

@Controller
public class StaticResourceController {

    @GetMapping("/admin")
    public String adminIndex() {
        return "forward:/admin/index.html";
    }

    @GetMapping(value = "/admin/css/{filename}", produces = "text/css")
    @ResponseBody
    public ResponseEntity<byte[]> getCss(@PathVariable String filename) {
        try {
            Resource resource = new ClassPathResource("static/admin/css/" + filename);
            try (InputStream is = resource.getInputStream()) {
                byte[] content = is.readAllBytes();
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType("text/css"))
                        .body(content);
            }
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping(value = "/admin/js/{filename}", produces = "application/javascript")
    @ResponseBody
    public ResponseEntity<byte[]> getJs(@PathVariable String filename) {
        try {
            Resource resource = new ClassPathResource("static/admin/js/" + filename);
            try (InputStream is = resource.getInputStream()) {
                byte[] content = is.readAllBytes();
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType("application/javascript"))
                        .body(content);
            }
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping(value = "/admin/{filename:.+}")
    @ResponseBody
    public ResponseEntity<byte[]> getHtml(@PathVariable String filename) {
        try {
            Resource resource = new ClassPathResource("static/admin/" + filename);
            try (InputStream is = resource.getInputStream()) {
                byte[] content = is.readAllBytes();
                String contentType = "text/html";
                if (filename.endsWith(".css")) contentType = "text/css";
                if (filename.endsWith(".js")) contentType = "application/javascript";
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(content);
            }
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }
}