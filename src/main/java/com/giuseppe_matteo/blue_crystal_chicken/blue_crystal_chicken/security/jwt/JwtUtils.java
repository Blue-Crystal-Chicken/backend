package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.security.jwt;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.security.servicies.UserDetailsImpl;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JwtUtils {

    @Value("${be.app.jwtSecret}")
    private String jwtSecret;

    @Value("${be.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public String generateJwtToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        return Jwts.builder()
                .subject(userPrincipal.getUsername())        // era .setSubject()
                .issuedAt(new Date())                        // era .setIssuedAt()
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs)) // era .setExpiration()
                .signWith(getSecretKey())                    // era .signWith(key, algo) — l'algo ora è implicito
                .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser()                                 // era .parserBuilder()
                .verifyWith(getSecretKey())                  // era .setSigningKey()
                .build()
                .parseSignedClaims(token)                    // era .parseClaimsJws()
                .getPayload()                                // era .getBody()
                .getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(authToken);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }
}