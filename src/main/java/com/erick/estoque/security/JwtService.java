package com.erick.estoque.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey key;

    private static final long TEMPO_EXPIRACAO =
            1000L * 60 * 60 * 8; // 8 horas

    public JwtService(
            @Value("${app.jwt.secret}") String secret
    ) {

        this.key = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );
    }

    public String gerar(String email) {

        Date agora = new Date();

        Date expiracao = new Date(
                agora.getTime() + TEMPO_EXPIRACAO
        );

        return Jwts.builder()
                .subject(email)
                .issuedAt(agora)
                .expiration(expiracao)
                .signWith(key)
                .compact();
    }

    public String extrairEmail(String token) {

        return extrairClaims(token)
                .getSubject();
    }

    public Date extrairExpiracao(String token) {

        return extrairClaims(token)
                .getExpiration();
    }

    public boolean tokenValido(String token) {

        try {

            Claims claims = extrairClaims(token);

            return claims.getSubject() != null
                    && claims.getExpiration()
                    .after(new Date());

        } catch (Exception exception) {

            return false;
        }
    }

    private Claims extrairClaims(String token) {

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}