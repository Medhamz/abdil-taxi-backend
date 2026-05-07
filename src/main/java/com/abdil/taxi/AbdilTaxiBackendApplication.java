package com.abdil.taxi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class AbdilTaxiBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(AbdilTaxiBackendApplication.class, args);
        System.out.println("");
        System.out.println("╔════════════════════════════════════════════╗");
        System.out.println("║   🚖 Application ABDIL TAXI démarrée      ║");
        System.out.println("║   📍 API disponible sur port 8080          ║");
        System.out.println("║   ✅ http://localhost:8080                 ║");
        System.out.println("╚════════════════════════════════════════════╝");
        System.out.println("");
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }
}