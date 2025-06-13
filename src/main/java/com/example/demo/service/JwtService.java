package com.example.demo.service;

import com.example.demo.model.Role;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class JwtService {
    private final String secret = "ThisIsAReallyLongAndSecureSecretKeyForHS256AlgorithmWhichIsAtLeast32BytesLong";

    public String generateToken(String username, Set<Role> roles) {
        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles.stream().map(Enum::name).collect(Collectors.toList()))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 1 day
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }
}
