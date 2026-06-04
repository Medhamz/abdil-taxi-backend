package com.abdil.taxi.service;

import com.abdil.taxi.model.Ride;
import com.abdil.taxi.model.RideRequest;
import com.abdil.taxi.model.ScheduledRide;
import com.abdil.taxi.repository.ScheduledRideRepository;
import com.abdil.taxi.repository.RideRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@EnableScheduling
public class ScheduledRideProcessor {

    @Autowired
    private ScheduledRideRepository scheduledRideRepository;

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private TaxiService taxiService;

    @Autowired
    private NotificationService notificationService;

    // Vérifie toutes les minutes si des courses programmées doivent être activées
    @Scheduled(fixedDelay = 60000) // Toutes les minutes
    @Transactional
    public void processScheduledRides() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneMinuteAgo = now.minusMinutes(1);

        // Récupérer les courses programmées dont l'heure est passée
        List<ScheduledRide> pendingRides = scheduledRideRepository
                .findByStatusAndScheduledDateTimeBetween("PENDING", oneMinuteAgo, now);

        for (ScheduledRide scheduled : pendingRides) {
            try {
                // Convertir en course normale
                RideRequest rideRequest = new RideRequest();
                rideRequest.setUserId(scheduled.getUserId());
                rideRequest.setClientName(scheduled.getClientName());
                rideRequest.setClientPhone(scheduled.getClientPhone());
                rideRequest.setPickupAddress(scheduled.getPickupAddress());
                rideRequest.setDestinationAddress(scheduled.getDestinationAddress());
                rideRequest.setDistance(scheduled.getDistance());
                rideRequest.setRideType(scheduled.getRideType());

                // Créer la course
                Ride ride = taxiService.createRide(rideRequest);

                // Marquer la course programmée comme traitée
                scheduled.setStatus("PROCESSED");
                scheduledRideRepository.save(scheduled);

                // Envoyer une notification au client
                notificationService.sendNotificationToClient(
                        scheduled.getUserId(),
                        "✅ Course programmée activée",
                        "Votre course du " + scheduled.getScheduledDateTime() + " vient d'être mise en ligne. Un chauffeur va bientôt l'accepter."
                );

                System.out.println("✅ Course programmée activée: ID=" + ride.getId() +
                        " pour client: " + scheduled.getClientName());

            } catch (Exception e) {
                System.err.println("❌ Erreur lors du traitement de la course programmée: " + e.getMessage());
                scheduled.setStatus("FAILED");
                scheduledRideRepository.save(scheduled);
            }
        }
    }
}