package com.erick.estoque.recuperacaosenha;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RecuperacaoSenhaTokenRepository
        extends JpaRepository<RecuperacaoSenhaToken, Long> {

    Optional<RecuperacaoSenhaToken>
    findByTokenHash(
            String tokenHash
    );

    void deleteByUsuarioId(
            Long usuarioId
    );

    long deleteByExpiraEmBefore(
            LocalDateTime dataHora
    );
}