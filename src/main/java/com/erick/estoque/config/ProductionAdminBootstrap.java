package com.erick.estoque.config;

import com.erick.estoque.security.PerfilUsuario;
import com.erick.estoque.security.UserEntity;
import com.erick.estoque.security.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Configuration
@Profile("prod")
public class ProductionAdminBootstrap {

    @Bean
    CommandLineRunner criarAdministradorInicial(
            UserRepository usuarios,
            PasswordEncoder passwordEncoder,
            @Value("${APP_BOOTSTRAP_ADMIN_EMAIL:}") String email,
            @Value("${APP_BOOTSTRAP_ADMIN_PASSWORD:}") String senha
    ) {
        return args -> {
            if (email.isBlank() && senha.isBlank()) {
                return;
            }

            if (email.isBlank() || senha.isBlank()) {
                throw new IllegalStateException("Administrador inicial requer email e senha.");
            }

            int tamanhoSenha = senha.getBytes(StandardCharsets.UTF_8).length;
            if (tamanhoSenha < 12 || tamanhoSenha > 72) {
                throw new IllegalStateException("Senha inicial deve ter entre 12 e 72 bytes.");
            }

            if (usuarios.countByPerfil(PerfilUsuario.ADMIN) > 0) {
                return;
            }

            String emailNormalizado = email.trim().toLowerCase(Locale.ROOT);
            if (usuarios.existsByEmail(emailNormalizado)) {
                throw new IllegalStateException("Email do administrador inicial ja existe.");
            }

            UserEntity administrador = new UserEntity();
            administrador.setNomeUsuario("Administrador");
            administrador.setEmail(emailNormalizado);
            administrador.setSenha(passwordEncoder.encode(senha));
            administrador.setPerfil(PerfilUsuario.ADMIN);
            usuarios.save(administrador);

            System.out.println("Administrador inicial criado para producao.");
        };
    }
}
