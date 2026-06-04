package com.abdil.taxi.repository;

import com.abdil.taxi.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByRideIdOrderByCreatedAtAsc(Long rideId);
    List<Message> findByRideIdAndReceiverIdAndIsReadFalse(Long rideId, Long receiverId);
}