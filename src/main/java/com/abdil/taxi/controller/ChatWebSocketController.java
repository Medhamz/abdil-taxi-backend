package com.abdil.taxi.controller;

import com.abdil.taxi.model.Message;
import com.abdil.taxi.model.MessageRequest;
import com.abdil.taxi.model.MessageResponse;
import com.abdil.taxi.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatWebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private MessageService messageService;

    @MessageMapping("/chat.send/{rideId}")
    public void sendMessage(MessageRequest request, @DestinationVariable Long rideId) {
        // Sauvegarder en base
        MessageResponse saved = messageService.sendMessage(request);

        // Envoyer au receiver via WebSocket
        messagingTemplate.convertAndSendToUser(
                request.getReceiverId().toString(),
                "/queue/messages/" + rideId,
                saved
        );

        // Envoyer aussi au sender pour confirmation
        messagingTemplate.convertAndSendToUser(
                request.getSenderId().toString(),
                "/queue/messages/" + rideId,
                saved
        );
    }
}