package com.erick.estoque.recuperacaosenha;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "recuperacao_senha_tokens",
        indexes = {
                @Index(
                        name = "idx_recuperacao_senha_expira_em",
                        columnList = "expira_em"
                )
        }
)
public class RecuperacaoSenhaToken {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    @Column(
            name = "usuario_id",
            nullable = false
    )
    private Long usuarioId;

    @Column(
            name = "token_hash",
            nullable = false,
            unique = true,
            length = 64
    )
    private String tokenHash;

    @Column(
            name = "criado_em",
            nullable = false
    )
    private LocalDateTime criadoEm;

    @Column(
            name = "expira_em",
            nullable = false
    )
    private LocalDateTime expiraEm;

    public RecuperacaoSenhaToken() {
    }

    public Long getId() {
        return id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(
            Long usuarioId
    ) {
        this.usuarioId =
                usuarioId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(
            String tokenHash
    ) {
        this.tokenHash =
                tokenHash;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(
            LocalDateTime criadoEm
    ) {
        this.criadoEm =
                criadoEm;
    }

    public LocalDateTime getExpiraEm() {
        return expiraEm;
    }

    public void setExpiraEm(
            LocalDateTime expiraEm
    ) {
        this.expiraEm =
                expiraEm;
    }
}