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
import java.nio.file.Files;

@Controller
public class StaticResourceController {

    @GetMapping("/admin")
    public String adminIndex() {
        return "forward:/admin/index.html";
    }

    @GetMapping(value = "/admin/css/{filename}", produces = "text/css")
    @ResponseBody
    public ResponseEntity<byte[]> getCss(@PathVariable String filename) throws IOException {
        Resource resource = new ClassPathResource("static/admin/css/" + filename);
        byte[] content = Files.readAllBytes(resource.getFile().toPath());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/css"))
                .body(content);
    }

    @GetMapping(value = "/admin/js/{filename}", produces = "application/javascript")
    @ResponseBody
    public ResponseEntity<byte[]> getJs(@PathVariable String filename) throws IOException {
        Resource resource = new ClassPathResource("static/admin/js/" + filename);
        byte[] content = Files.readAllBytes(resource.getFile().toPath());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/javascript"))
                .body(content);
    }

    @GetMapping(value = "/admin/{filename}")
    @ResponseBody
    public ResponseEntity<byte[]> getHtml(@PathVariable String filename) throws IOException {
        Resource resource = new ClassPathResource("static/admin/" + filename);
        byte[] content = Files.readAllBytes(resource.getFile().toPath());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/html"))
                .body(content);
    }
}