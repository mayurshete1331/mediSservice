package com.example.demo.util; // You can place this in any package you prefer

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.util.Base64;

public class KeyGenerator {
    public static void main(String[] args) {
        // Generate a secure random key for HS256 algorithm
        String secretKey = Base64.getEncoder().encodeToString(Keys.secretKeyFor(SignatureAlgorithm.HS256).getEncoded());
        System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        System.out.println("Generated JWT Secret Key (copy this whole string):");
        System.out.println(secretKey);
        System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------------------------");
    }
}