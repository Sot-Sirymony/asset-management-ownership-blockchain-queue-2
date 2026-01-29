package com.hrd.asset_holder_api.configuration;

import com.hrd.asset_holder_api.jwt.JwtAuthEntrypoint;
import com.hrd.asset_holder_api.jwt.JwtAuthFilter;
import com.hrd.asset_holder_api.service.AppUserService;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
    @Configuration
    @AllArgsConstructor
    public class SecurityConfig {
        private final AppUserService appUserService;
        private final JwtAuthEntrypoint jwtAuthEntrypoint;
        private final BCryptPasswordEncoder passwordEncoder;
        private final JwtAuthFilter jwtAuthFilter;

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)throws Exception{
            return configuration.getAuthenticationManager();
        }
        @Bean
        DaoAuthenticationProvider authenticationProvider(){
            DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
            provider.setUserDetailsService(appUserService);
            provider.setPasswordEncoder(passwordEncoder);
            return provider;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
            httpSecurity
                    .cors(Customizer.withDefaults()).csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(request -> request
                            .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui-html", "/api/v1/files/**", "/rest/auth/**").permitAll()
                            .requestMatchers("/api/v1/admin/**").hasAuthority("ADMIN")
                            .anyRequest().authenticated()
                    )
                    .sessionManagement(session -> session
                            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    )
                    .exceptionHandling(exception -> exception
                            .authenticationEntryPoint(jwtAuthEntrypoint)
                    )
                    .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
            return httpSecurity.build();
        }


    }

