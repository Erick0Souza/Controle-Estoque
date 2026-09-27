package com.erick.estoque.security;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface UserRepository
        extends JpaRepository<UserEntity, Long>,
        JpaSpecificationExecutor<UserEntity> {

    Optional<UserEntity> findByEmail(
            String email
    );

    boolean existsByEmail(
            String email
    );

    boolean existsByNomeUsuarioIgnoreCase(
            String nomeUsuario
    );

    long countByPerfil(
            PerfilUsuario perfil
    );
}