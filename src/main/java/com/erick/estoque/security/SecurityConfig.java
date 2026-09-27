package com.erick.estoque.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {

        return configuration
                .getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            JwtFilter jwtFilter
    ) throws Exception {

        return http

                .csrf(
                        csrf ->
                                csrf.disable()
                )

                .sessionManagement(
                        session ->
                                session.sessionCreationPolicy(
                                        SessionCreationPolicy.STATELESS
                                )
                )

                .authorizeHttpRequests(
                        auth -> auth

                                .requestMatchers(
                                        "/auth/**"
                                )
                                .permitAll()

                                .requestMatchers(
                                        "/swagger-ui/**",
                                        "/swagger-ui.html",
                                        "/v3/api-docs/**"
                                )
                                .permitAll()

                                .requestMatchers(
                                        "/",
                                        "/index.html",
                                        "/app.js",
                                        "/style.css",
                                        "/css/**",
                                        "/js/**"
                                )
                                .permitAll()

                                .requestMatchers(
                                        "/uploads/**"
                                )
                                .permitAll()

                                .requestMatchers(
                                        "/error"
                                )
                                .permitAll()

                                .requestMatchers(
                                        "/admin/**"
                                )
                                .hasRole(
                                        "ADMIN"
                                )

                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/produtos/**"
                                )
                                .hasAnyRole(
                                        "ADMIN",
                                        "OPERADOR",
                                        "CONSULTA"
                                )

                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/produtos/*/imagem"
                                )
                                .hasAnyRole(
                                        "ADMIN",
                                        "OPERADOR"
                                )

                                .requestMatchers(
                                        HttpMethod.DELETE,
                                        "/produtos/*/imagem"
                                )
                                .hasAnyRole(
                                        "ADMIN",
                                        "OPERADOR"
                                )

                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/produtos"
                                )
                                .hasAnyRole(
                                        "ADMIN",
                                        "OPERADOR"
                                )

                                .requestMatchers(
                                        HttpMethod.PUT,
                                        "/produtos/**"
                                )
                                .hasAnyRole(
                                        "ADMIN",
                                        "OPERADOR"
                                )

                                .requestMatchers(
                                        HttpMethod.DELETE,
                                        "/produtos/**"
                                )
                                .hasRole(
                                        "ADMIN"
                                )

                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/movimentacoes/**"
                                )
                                .hasAnyRole(
                                        "ADMIN",
                                        "OPERADOR",
                                        "CONSULTA"
                                )

                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/movimentacoes/**"
                                )
                                .hasAnyRole(
                                        "ADMIN",
                                        "OPERADOR"
                                )

                                .requestMatchers(
                                        HttpMethod.PUT,
                                        "/movimentacoes/**"
                                )
                                .hasAnyRole(
                                        "ADMIN",
                                        "OPERADOR"
                                )

                                .requestMatchers(
                                        HttpMethod.DELETE,
                                        "/movimentacoes/**"
                                )
                                .hasRole(
                                        "ADMIN"
                                )

                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/categorias/**"
                                )
                                .hasAnyRole(
                                        "ADMIN",
                                        "OPERADOR",
                                        "CONSULTA"
                                )

                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/categorias/**"
                                )
                                .hasRole(
                                        "ADMIN"
                                )

                                .requestMatchers(
                                        HttpMethod.PUT,
                                        "/categorias/**"
                                )
                                .hasRole(
                                        "ADMIN"
                                )

                                .requestMatchers(
                                        HttpMethod.DELETE,
                                        "/categorias/**"
                                )
                                .hasRole(
                                        "ADMIN"
                                )

                                .anyRequest()
                                .authenticated()
                )

                .exceptionHandling(
                        exception -> exception

                                .authenticationEntryPoint(
                                        (
                                                request,
                                                response,
                                                authException
                                        ) -> {

                                            response.setStatus(
                                                    HttpServletResponse.SC_UNAUTHORIZED
                                            );

                                            response.setContentType(
                                                    "application/json;charset=UTF-8"
                                            );

                                            response
                                                    .getWriter()
                                                    .write(
                                                            """
                                                            {
                                                              "status": 401,
                                                              "mensagem": "Autenticação necessária"
                                                            }
                                                            """
                                                    );
                                        }
                                )

                                .accessDeniedHandler(
                                        (
                                                request,
                                                response,
                                                accessDeniedException
                                        ) -> {

                                            response.setStatus(
                                                    HttpServletResponse.SC_FORBIDDEN
                                            );

                                            response.setContentType(
                                                    "application/json;charset=UTF-8"
                                            );

                                            response
                                                    .getWriter()
                                                    .write(
                                                            """
                                                            {
                                                              "status": 403,
                                                              "mensagem": "Você não possui permissão para realizar esta ação"
                                                            }
                                                            """
                                                    );
                                        }
                                )
                )

                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                .build();
    }
}