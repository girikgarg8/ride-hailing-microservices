package com.girikgarg.uberauthservice.utils;

import com.girikgarg.uberauthservice.config.JwtConfigurationProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@Slf4j
@RequiredArgsConstructor
public class JWTUtil {
    
    private final JwtConfigurationProperties jwtConfig;
    
    public String createToken(Map<String, Object> payload, String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtConfig.getExpiry() * 1000L);
        return Jwts.builder()
                .claims(payload)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(expiryDate)
                .subject(email)
                .signWith(getSignKey())
                .compact();
    }

    public String createToken(String email) {
        return createToken(new HashMap<>(), email);
    }

    public Claims extractAllPayloads(String token) {
        return Jwts
                .parser()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllPayloads(token);
        return claimsResolver.apply(claims);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * This method checks if the token expiry was before the current time stamp or not ?
     * @param token JWT token
     * @return true if token is expired else false
     */
    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Key getSignKey() {
        return Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public Boolean validateToken(String token, String email) {
        final String userEmailFetchedFromToken = extractEmail(token);
        return (userEmailFetchedFromToken.equals(email)) && !isTokenExpired(token);
    }

    public Object extractPayload(String token, String payloadKey) {
        Claims claim = extractAllPayloads(token);
        return (Object) claim.get(payloadKey);
    }
}
