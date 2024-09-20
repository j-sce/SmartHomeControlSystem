package com.smart.home.userservice.security;

import com.smart.home.userservice.model.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Set;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class JwtTokenService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private Long accessTokenValidity;

    public JwtTokenService(String secretKey, Long accessTokenValidity) {
        this.secretKey = secretKey;
        this.accessTokenValidity = accessTokenValidity;
    }


    public String generateToken(String username, Set<Role> authorities) {
        return Jwts.builder().subject(username)
                .claim("roles", authorities)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + accessTokenValidity))
                .signWith(getSecretKey())
                .compact();
    }

    public String extractUsernameFromToken(String token) {
        if (isTokenExpired(token)) {
            return null;
        }
        return getClaims(token, Claims::getSubject);
    }

    public <T> T getClaims(String token, Function<Claims, T> resolver) {
        return resolver.apply(Jwts.parser()
                .verifyWith(getSecretKey())
                .clockSkewSeconds(2)
                .build()
                .parseSignedClaims(token)
                .getPayload());
    }

    public boolean isTokenExpired(String token) {
        Date expiration = getClaims(token, Claims::getExpiration);
        return expiration.before(new Date());
    }

    public boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }


    private SecretKey getSecretKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
