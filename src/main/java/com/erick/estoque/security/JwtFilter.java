package com.erick.estoque.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtFilter(
            JwtService jwtService,
            UserRepository userRepository
    ) {
        this.jwtService =
                jwtService;

        this.userRepository =
                userRepository;
    }

    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request
    ) {

        String caminho =
                request.getRequestURI();

        return caminho.equals("/")
                || caminho.equals("/index.html")
                || caminho.equals("/app.js")
                || caminho.equals("/style.css")
                || caminho.equals("/error")
                || caminho.startsWith("/auth/")
                || caminho.startsWith("/swagger-ui/")
                || caminho.startsWith("/v3/api-docs/")
                || caminho.startsWith("/uploads/")
                || caminho.startsWith("/css/")
                || caminho.startsWith("/js/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorization =
                request.getHeader(
                        "Authorization"
                );

        if (
                authorization == null ||
                        !authorization.startsWith(
                                "Bearer "
                        )
        ) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }

        String token =
                authorization.substring(
                        7
                ).trim();

        if (
                token.isBlank() ||
                        token.length() > 4096 ||
                        !jwtService.tokenValido(
                                token
                        )
        ) {

            respostaNaoAutorizada(
                    response
            );

            return;
        }

        if (
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        == null
        ) {

            String email;

            try {

                email =
                        jwtService.extrairEmail(
                                token
                        );

            } catch (
                    Exception exception
            ) {

                respostaNaoAutorizada(
                        response
                );

                return;
            }

            var usuarioOptional =
                    userRepository
                            .findByEmail(
                                    email
                            );

            if (
                    usuarioOptional.isEmpty()
            ) {

                respostaNaoAutorizada(
                        response
                );

                return;
            }

            UserEntity usuario =
                    usuarioOptional.get();

            String role =
                    "ROLE_" +
                            usuario
                                    .getPerfil()
                                    .name();

            SimpleGrantedAuthority autoridade =
                    new SimpleGrantedAuthority(
                            role
                    );

            var authentication =
                    new UsernamePasswordAuthenticationToken(
                            usuario.getEmail(),
                            null,
                            List.of(
                                    autoridade
                            )
                    );

            authentication.setDetails(
                    new WebAuthenticationDetailsSource()
                            .buildDetails(
                                    request
                            )
            );

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(
                            authentication
                    );
        }

        filterChain.doFilter(
                request,
                response
        );
    }

    private void respostaNaoAutorizada(
            HttpServletResponse response
    ) throws IOException {

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
                          "mensagem": "Token inválido ou expirado"
                        }
                        """
                );
    }
}