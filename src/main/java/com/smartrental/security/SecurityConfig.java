package com.smartrental.security;

import com.smartrental.model.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security Configuration for the Smart Rental Management System.
 * Configures JWT-based stateless authentication, CORS, and authorization rules.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;

    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:4200}")
    private String allowedOrigins;

    // ===========================================
    // Password Encoder
    // ===========================================

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Strength 12 provides good security with reasonable performance
        return new BCryptPasswordEncoder(12);
    }

    // ===========================================
    // Authentication Provider
    // ===========================================

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        provider.setHideUserNotFoundExceptions(true); // Prevent email enumeration
        return provider;
    }

    // ===========================================
    // Authentication Manager
    // ===========================================

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // ===========================================
    // CORS Configuration
    // ===========================================

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        configuration.setAllowedOriginPatterns(origins);

        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"
        ));

        configuration.setAllowedHeaders(List.of("*"));

        configuration.setAllowCredentials(true);

        configuration.setExposedHeaders(List.of(
                "Authorization",
                "Content-Disposition",
                "X-Total-Count",
                "X-Page-Count",
                "X-Current-Page"
        ));

        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // ===========================================
    // Security Filter Chain
    // ===========================================

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF for stateless JWT API
                .csrf(AbstractHttpConfigurer::disable)

                // Enable CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Security headers
                .headers(headers -> headers
                        .xssProtection(xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                        .frameOptions(frame -> frame.deny())
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000))
                )

                // Stateless session management (JWT)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Exception handling
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write(
                                    "{\"error\":\"Unauthorized\",\"message\":\"Authentication required\",\"status\":401}"
                            );
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            response.getWriter().write(
                                    "{\"error\":\"Forbidden\",\"message\":\"Access denied\",\"status\":403}"
                            );
                        })
                )

                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - no authentication required
                        // NOTE: context-path is /api, so patterns are relative to it
                        .requestMatchers("/auth/login", "/auth/register", "/auth/refresh",
                                "/auth/forgot-password", "/auth/reset-password",
                                "/auth/verify-email", "/auth/resend-verification",
                                "/auth/health").permitAll()
                        .requestMatchers("/public/**").permitAll()
                        .requestMatchers("/health").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()

                        // Swagger/OpenAPI endpoints
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/api-docs/**").permitAll()

                        // Webhook (external services)
                        .requestMatchers("/webhook/**").permitAll()

                        // Static resources
                        .requestMatchers("/favicon.ico", "/error").permitAll()

                        // Actuator endpoints (restrict in production)
                        .requestMatchers("/actuator/**").hasRole("ADMIN")

                        // Role-based endpoint access
                        .requestMatchers("/v1/owner/**").hasAnyRole("OWNER", "ADMIN")
                        .requestMatchers("/v1/tenant/**").hasRole("TENANT")
                        .requestMatchers("/v1/dashboard/tenant").hasAnyRole("TENANT", "OWNER", "ADMIN")
                        .requestMatchers("/v1/properties/**", "/v1/dashboard/**", "/v1/tenants/**").hasAnyRole("OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/v1/leases/**").hasAnyRole("OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/v1/leases/**").hasAnyRole("OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/v1/leases/**").hasAnyRole("OWNER", "ADMIN")
                        .requestMatchers("/v1/payments/tenant/**").hasAnyRole("TENANT", "OWNER", "ADMIN")
                        .requestMatchers("/v1/payments/**").hasAnyRole("OWNER", "ADMIN")
                        .requestMatchers("/v1/maintenance/**").authenticated()

                        .anyRequest().authenticated()
                )

                // Add JWT filter before UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // HTTP Basic disabled (we use JWT)
                .httpBasic(AbstractHttpConfigurer::disable)

                // Form login disabled (we use JWT)
                .formLogin(AbstractHttpConfigurer::disable)

                // Logout configuration
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .permitAll()
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"message\":\"Logged out successfully\"}");
                        })
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                );

        return http.build();
    }
}

