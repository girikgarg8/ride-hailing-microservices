package com.girikgarg.uberclientsocketservice.configuration;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;

import java.security.Principal;

/**
 * Intercepts STOMP messages to set user principal based on userId in session attributes.
 * This enables Spring's convertAndSendToUser() to route messages to specific users.
 */
public class UserChannelInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // Extract userId from session attributes (set during handshake)
            Object userIdObj = accessor.getSessionAttributes().get("userId");
            
            if (userIdObj != null) {
                String userId = userIdObj.toString();
                
                // Create a custom Principal with the userId
                Principal principal = () -> userId;
                accessor.setUser(principal);
                
                System.out.println("STOMP CONNECT: Set user principal = " + userId);
            }
        }
        
        return message;
    }
}

