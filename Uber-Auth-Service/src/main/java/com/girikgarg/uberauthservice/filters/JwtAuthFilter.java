package com.girikgarg.uberauthservice.filters;

import com.girikgarg.uberauthservice.helpers.AuthUserDetails;
import com.girikgarg.uberauthservice.utils.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {
    
    @Autowired
    private UserDetailsService userDetailsService;

    private final JWTUtil jwtUtil;

    public JwtAuthFilter(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {
        
        // Skip filter for public endpoints
        String requestPath = request.getRequestURI();
        if (requestPath.contains("/api/v1/auth/signup") || 
            requestPath.contains("/api/v1/auth/signin") || 
            requestPath.contains("/actuator/health")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        log.debug("JWT Filter processing request: {}", requestPath);
        
        String token = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals("JwtToken")) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        if (token == null) {
            log.warn("No JWT token found in request to: {}", requestPath);
            // User has not provided any jwt token hence request should not go forward
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized - No token provided");
            return;
        }

        try {
            String email = jwtUtil.extractEmail(token);
            
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                
                if (jwtUtil.validateToken(token, userDetails.getUsername())) {
                    log.info("JWT token validated successfully for user: {}", email);
                    
                    UsernamePasswordAuthenticationToken authToken = 
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    
                    // Set email and role in request attributes for controller access
                    request.setAttribute("email", email);
                    if (userDetails instanceof AuthUserDetails) {
                        request.setAttribute("role", ((AuthUserDetails) userDetails).getRole().name());
                    }
                } else {
                    log.warn("JWT token validation failed for user: {}", email);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("Unauthorized - Invalid token");
                    return;
                }
            }
        } catch (Exception e) {
            log.error("Error processing JWT token: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized - Token processing error");
            return;
        }
        
        filterChain.doFilter(request, response);
    }
}
