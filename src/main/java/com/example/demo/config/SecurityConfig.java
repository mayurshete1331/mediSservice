// src/main/java/com/example/demo/config/SecurityConfig.java

package com.example.demo.config;

import com.example.demo.service.CustomUserDetailsService; // Import your custom UserDetailsService
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider; // NEW: Import for DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; // Needed for JWT filter placement (later)
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Autowire your custom UserDetailsService to be used by the DaoAuthenticationProvider
    // This is necessary for Spring Security to know how to load user details
    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    // If you have a custom JwtAuthenticationFilter, you'd typically inject it here
    // This filter would be responsible for validating JWTs on subsequent requests after login.
    // For example:
    // @Autowired
    // private JwtAuthenticationFilter jwtAuthenticationFilter;


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF for stateless APIs (JWT). Consider alternatives for production.
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Enable CORS and configure its source
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Set session management to stateless for JWT
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll() // Allow unauthenticated access to authentication endpoints (e.g., login, register)
                        // Define other access rules based on roles from your application
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/doctor/**").hasRole("DOCTOR")
                        .requestMatchers("/api/patient/**").hasRole("PATIENT")
                        .requestMatchers("/api/staff/**").hasRole("STAFF")
                        .anyRequest().authenticated() // All other requests require authentication
                );
        // After authenticating with JWT, you will typically add your JWT filter here
        // This filter would process the JWT token present in subsequent requests.
        // .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // NEW: Define the DaoAuthenticationProvider bean
    // This bean explicitly tells Spring Security how to authenticate users.
    // It connects your CustomUserDetailsService (which fetches user details)
    // with your PasswordEncoder (which verifies passwords).
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService); // Set your custom UserDetailsService
        authProvider.setPasswordEncoder(passwordEncoder()); // Set the PasswordEncoder bean
        return authProvider;
    }


    // Define the CORS configuration source bean for cross-origin requests
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Set the allowed origins for your frontend application(s)
        // In development, this is usually http://localhost:4200 for Angular
        // For production, list your actual deployed frontend domain(s)
        configuration.setAllowedOrigins(List.of("http://localhost:4200")); // e.g., List.of("http://localhost:4200", "https://your-production-app.com")

        // Set the allowed HTTP methods for cross-origin requests
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Set the allowed headers for cross-origin requests. "*" allows all headers.
        configuration.setAllowedHeaders(List.of("*"));

        // Allow sending of credentials (e.g., cookies, Authorization headers for JWT)
        configuration.setAllowCredentials(true);

        // Set the maximum age (in seconds) for the CORS pre-flight result.
        // This caches the pre-flight response, reducing overhead for subsequent requests.
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Register this CORS configuration to apply to all paths (/**)
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // Password Encoder Bean: Essential for encoding and verifying user passwords
    // BCryptPasswordEncoder is a strong and commonly used password encoder.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Authentication Manager Bean: Used in the login process to authenticate users
    // This bean implicitly uses the configured AuthenticationProviders (like DaoAuthenticationProvider)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
