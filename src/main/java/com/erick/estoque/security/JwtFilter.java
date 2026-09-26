package com.erick.estoque.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtFilter(
            JwtService jwtService,
            UserRepository userRepository
    ) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
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
                request.getHeader("Authorization");

        if (
                authorization == null
                        || !authorization.startsWith("Bearer ")
        ) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }

        String token =
                authorization.substring(7);

        if (!jwtService.tokenValido(token)) {

            respostaNaoAutorizada(
                    response,
                    "Token inválido ou expirado"
            );

            return;
        }

        if (
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        == null
        ) {

            String email =
                    jwtService.extrairEmail(
                            token
                    );

            var usuarioOptional =
                    userRepository.findByEmail(
                            email
                    );

            if (usuarioOptional.isEmpty()) {

                respostaNaoAutorizada(
                        response,
                        "Usuário do token não encontrado"
                );

                return;
            }

            var usuario =
                    usuarioOptional.get();

            var authentication =
                    new UsernamePasswordAuthenticationToken(
                            usuario.getEmail(),
                            null,
                            Collections.emptyList()
                    );

            authentication.setDetails(
                    new WebAuthenticationDetailsSource()
                            .buildDetails(request)
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
            HttpServletResponse response,
            String mensagem
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
                          "mensagem": "%s"
                        }
                        """.formatted(mensagem)
                );
    }
}