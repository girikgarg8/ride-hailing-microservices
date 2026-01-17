package com.girikgarg.ubersocketservice.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration for real-time bidirectional communication between clients and server.
 * Uses STOMP (Simple Text Oriented Messaging Protocol) over WebSocket.
 */
@Configuration
@EnableWebSocketMessageBroker // Enables WebSocket message handling, backed by a message broker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Configures the message broker for handling messages.
     * 
     * - enableSimpleBroker("/topic"): Creates an in-memory message broker that routes messages
     *   to destinations prefixed with "/topic". Clients subscribe to "/topic/..." to receive broadcasts.
     *   
     * - setApplicationDestinationPrefixes("/app"): Messages sent by clients to destinations prefixed
     *   with "/app" will be routed to @MessageMapping annotated methods in controllers.
     *   
     * Example flow: Client sends to "/app/location" → routed to @MessageMapping("/location") method
     *               Server broadcasts to "/topic/drivers" → all subscribers receive the message
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Enable simple in-memory broker for broadcasting messages to subscribed clients
        // Add "/queue" for user-specific private messages
        registry.enableSimpleBroker("/topic", "/queue");
        
        // Set prefix for messages bound for @MessageMapping methods (client → server)
        registry.setApplicationDestinationPrefixes("/app");
        
        // Set user destination prefix (default is "/user")
        registry.setUserDestinationPrefix("/user");
    }

    /**
     * Registers STOMP endpoints that clients will connect to.
     * 
     * - addEndpoint("/ws"): Creates WebSocket endpoint at "ws://host/ws"
     * - addInterceptors(): Adds custom handshake interceptor to extract userId from query params
     * - setAllowedOriginPatterns("*"): Allows CORS from any origin (including file:// protocol)
     * - withSockJS(): Enables SockJS fallback options for browsers that don't support WebSocket.
     *   SockJS provides HTTP-based fallbacks (polling, streaming) for compatibility.
     *   
     * Clients connect to: ws://localhost:PORT/ws?userId=user1 (or http://localhost:PORT/ws?userId=user1 with SockJS)
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register WebSocket endpoint with custom handshake interceptor for user identification
        // Allow all origins - API Gateway will handle the main CORS policy
        registry.addEndpoint("/ws")
                .addInterceptors(new UserHandshakeInterceptor())  // Extract userId during handshake
                .setAllowedOriginPatterns("*")  // Required for WebSocket CORS, Gateway handles HTTP CORS
                .withSockJS();
    }

    /**
     * Configures the channel interceptor to set user principal based on session attributes.
     * This is required for convertAndSendToUser() to work properly.
     *
     * The interceptor extracts userId from session attributes (set during handshake)
     * and creates a Principal object that Spring can use for user-specific routing.
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new UserChannelInterceptor());
    }
}
