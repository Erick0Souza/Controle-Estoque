package com.erick.estoque.auditoria;

import com.erick.estoque.security.PerfilUsuario;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "auditorias")
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "data_hora",
            nullable = false
    )
    private LocalDateTime dataHora;

    @Column(
            name = "usuario_nome",
            length = 50
    )
    private String usuarioNome;

    @Column(
            name = "usuario_email",
            length = 150
    )
    private String usuarioEmail;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "usuario_perfil",
            length = 20
    )
    private PerfilUsuario usuarioPerfil;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 50
    )
    private TipoAcaoAuditoria acao;

    @Column(
            nullable = false,
            length = 50
    )
    private String entidade;

    @Column(name = "entidade_id")
    private Long entidadeId;

    @Column(
            length = 1000
    )
    private String descricao;

    public Auditoria() {
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(
            LocalDateTime dataHora
    ) {
        this.dataHora = dataHora;
    }

    public String getUsuarioNome() {
        return usuarioNome;
    }

    public void setUsuarioNome(
            String usuarioNome
    ) {
        this.usuarioNome = usuarioNome;
    }

    public String getUsuarioEmail() {
        return usuarioEmail;
    }

    public void setUsuarioEmail(
            String usuarioEmail
    ) {
        this.usuarioEmail = usuarioEmail;
    }

    public PerfilUsuario getUsuarioPerfil() {
        return usuarioPerfil;
    }

    public void setUsuarioPerfil(
            PerfilUsuario usuarioPerfil
    ) {
        this.usuarioPerfil = usuarioPerfil;
    }

    public TipoAcaoAuditoria getAcao() {
        return acao;
    }

    public void setAcao(
            TipoAcaoAuditoria acao
    ) {
        this.acao = acao;
    }

    public String getEntidade() {
        return entidade;
    }

    public void setEntidade(
            String entidade
    ) {
        this.entidade = entidade;
    }

    public Long getEntidadeId() {
        return entidadeId;
    }

    public void setEntidadeId(
            Long entidadeId
    ) {
        this.entidadeId = entidadeId;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(
            String descricao
    ) {
        this.descricao = descricao;
    }
}