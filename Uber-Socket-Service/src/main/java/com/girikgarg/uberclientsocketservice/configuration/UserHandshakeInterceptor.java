package com.girikgarg.uberclientsocketservice.configuration;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * Intercepts WebSocket handshake to extract userId from request parameters
 * and store it in session attributes for user-specific message routing.
 */
public class UserHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) throws Exception {
        
        // Extract userId from query parameters (e.g., ws://localhost:8080/ws?userId=user1)
        String query = request.getURI().getQuery();
        if (query != null && query.contains("userId=")) {
            String userId = query.split("userId=")[1].split("&")[0];
            attributes.put("userId", userId);
            System.out.println("WebSocket handshake: userId = " + userId);
        }
        
        return true;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception) {
        // No action needed after handshake
    }
}


