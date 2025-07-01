package com.example.movie_booking_backend.common.config;

import com.example.movie_booking_backend.common.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    private JwtUtils jwtUtils;

    // JWT 过滤器
    public class JwtAuthFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String role = jwtUtils.getRoleFromToken(token);
                if (role != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(null, null, Collections.singletonList(authority));
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
            filterChain.doFilter(request, response);
        }
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new JwtAuthFilter(), org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(authz -> authz
                        // 公开接口
                        .requestMatchers("/api/qq/callback").permitAll()
                        .requestMatchers("/api/bind-qq").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/movies/public/**").permitAll()
                        .requestMatchers("/api/sessions/public/**").permitAll()
                        .requestMatchers("/api/users/**").permitAll()
                        .requestMatchers("/api/alipay/**").permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v2/api-docs", "/swagger-resources/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        // 需要特定角色的接口
                        .requestMatchers("/api/user/**").hasAnyRole("ADMIN", "SUPERADMIN")
                        .requestMatchers("/api/movies/**").hasAnyRole("ADMIN", "SUPERADMIN")
                        // 其他接口需要认证
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}