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
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorization =
                request.getHeader("Authorization");

        // Não existe token
        if (authorization == null
                || !authorization.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring(7);

        // Token inválido ou expirado
        if (!jwtService.tokenValido(token)) {

            respostaNaoAutorizada(
                    response,
                    "Token inválido ou expirado"
            );

            return;
        }

        // Se ainda não existe autenticação
        if (SecurityContextHolder
                .getContext()
                .getAuthentication() == null) {

            String email =
                    jwtService.extrairEmail(token);

            System.out.println(
                    "JWT - email encontrado no token: " + email
            );

            var usuarioOptional =
                    userRepository.findByEmail(email);

            // Token válido, mas usuário não existe
            if (usuarioOptional.isEmpty()) {

                System.out.println(
                        "JWT - usuário não encontrado no banco"
                );

                respostaNaoAutorizada(
                        response,
                        "Usuário do token não encontrado"
                );

                return;
            }

            var usuario = usuarioOptional.get();

            System.out.println(
                    "JWT - usuário encontrado: "
                            + usuario.getEmail()
            );

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
                    .setAuthentication(authentication);

            System.out.println(
                    "JWT - usuário autenticado com sucesso"
            );
        }

        filterChain.doFilter(request, response);
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

        response.getWriter().write(
                """
                {
                  "status": 401,
                  "mensagem": "%s"
                }
                """.formatted(mensagem)
        );
    }
}