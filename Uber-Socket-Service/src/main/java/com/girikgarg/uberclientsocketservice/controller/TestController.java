package com.girikgarg.uberclientsocketservice.controller;

import com.girikgarg.uberclientsocketservice.dto.ChatRequest;
import com.girikgarg.uberclientsocketservice.dto.ChatResponse;
import com.girikgarg.uberclientsocketservice.dto.TestRequest;
import com.girikgarg.uberclientsocketservice.dto.TestResponse;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

/**
 * WebSocket controller for testing real-time communication.
 * Note: Use @Controller (not @RestController) for WebSocket endpoints.
 */
@Controller
public class TestController {

    private final SimpMessagingTemplate messagingTemplate;

    public TestController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Handles incoming WebSocket messages sent to /app/ping
     * Broadcasts response to all clients subscribed to /topic/ping
     * 
     * Test flow:
     * 1. Client connects to ws://localhost:PORT/ws
     * 2. Client subscribes to /topic/ping
     * 3. Client sends message to /app/ping with TestRequest payload
     * 4. Server responds by broadcasting TestResponse to /topic/ping
     * 5. All subscribed clients receive the response
     */
    @MessageMapping("/ping") // Clients send to /app/ping
    @SendTo("/topic/ping")   // Server broadcasts to /topic/ping
    public TestResponse pingCheck(TestRequest message) {
        return TestResponse.builder()
                .data("Received: " + message.getData())
                .build();
    }

    /**
     * Handles chat messages from clients with room support.
     * Broadcasts the message to all clients subscribed to the specific room
     * 
     * Chat flow:
     * 1. Client sends message to /app/chat/{room} with name and message
     * 2. Server broadcasts to /topic/message/{room} with name, message, and timestamp
     * 3. Only clients subscribed to that specific room receive the message
     * 
     * @param room The chat room identifier (captured from URL path)
     * @param request The chat request containing name and message
     * @return ChatResponse with name, message, and timestamp
     */
    @MessageMapping("/chat/{room}")          // Clients send to /app/chat/room1
    @SendTo("/topic/message/{room}")         // Server broadcasts to /topic/message/room1
    public ChatResponse chatMessage(@DestinationVariable String room, ChatRequest request) {
        System.out.println("Message received in room '" + room + "' from " + request.getName());
        return ChatResponse.builder()
                .name(request.getName())
                .message(request.getMessage())
                .timestamp("" + System.currentTimeMillis())
                .build();
    }

    /**
     * Handles one-to-one private messages between users.
     * Sends message to a specific user in a specific room (private messaging)
     * 
     * Private chat flow:
     * 1. Client sends to /app/private/{room}/{userId} with name and message
     * 2. Server sends ONLY to the specific userId at /user/queue/privateMessage/{room}
     * 3. Only the targeted user receives the private message
     * 
     * @param room The chat room identifier
     * @param userId The target user's ID who should receive the message
     * @param request The chat request containing sender name and message
     */
    @MessageMapping("/private/{room}/{userId}")
    public void privateChatMessage(
            @DestinationVariable String room,
            @DestinationVariable String userId,
            ChatRequest request) {
        
        ChatResponse response = ChatResponse.builder()
                .name(request.getName())
                .message(request.getMessage())
                .timestamp("" + System.currentTimeMillis())
                .build();
        
        // Send to specific user only
        // User must subscribe to /user/queue/privateMessage/{room}
        messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/privateMessage/" + room,
                response
        );
        
        System.out.println("Private message sent in room '" + room + "' from " 
                + request.getName() + " to user " + userId);
    }

    /**
     * Sends periodic messages to all subscribed clients every 2 seconds.
     * Uses SimpMessagingTemplate to broadcast messages to /topic/scheduled
     */
    @Scheduled(fixedDelay = 2000)
    public void sendPeriodicMessage() {
        String message = "Periodic message from server " + System.currentTimeMillis();
        messagingTemplate.convertAndSend("/topic/scheduled", message);
        System.out.println("Sent scheduled message: " + message);
    }
}
