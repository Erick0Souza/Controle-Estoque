package com.erick.estoque.movimentacao;

import com.erick.estoque.produto.Produto;
import com.erick.estoque.security.PerfilUsuario;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "movimentacoes_estoque")
public class MovimentacaoEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "produto_id",
            nullable = false
    )
    private Produto produto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMovimentacao tipo;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    private String observacao;

    @Column(
            name = "responsavel_nome",
            length = 50
    )
    private String responsavelNome;

    @Column(
            name = "responsavel_email",
            length = 150
    )
    private String responsavelEmail;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "responsavel_perfil",
            length = 20
    )
    private PerfilUsuario responsavelPerfil;

    public MovimentacaoEstoque() {
    }

    public Long getId() {
        return id;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(
            Produto produto
    ) {
        this.produto = produto;
    }

    public TipoMovimentacao getTipo() {
        return tipo;
    }

    public void setTipo(
            TipoMovimentacao tipo
    ) {
        this.tipo = tipo;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(
            Integer quantidade
    ) {
        this.quantidade = quantidade;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(
            LocalDateTime dataHora
    ) {
        this.dataHora = dataHora;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(
            String observacao
    ) {
        this.observacao = observacao;
    }

    public String getResponsavelNome() {
        return responsavelNome;
    }

    public void setResponsavelNome(
            String responsavelNome
    ) {
        this.responsavelNome =
                responsavelNome;
    }

    public String getResponsavelEmail() {
        return responsavelEmail;
    }

    public void setResponsavelEmail(
            String responsavelEmail
    ) {
        this.responsavelEmail =
                responsavelEmail;
    }

    public PerfilUsuario getResponsavelPerfil() {
        return responsavelPerfil;
    }

    public void setResponsavelPerfil(
            PerfilUsuario responsavelPerfil
    ) {
        this.responsavelPerfil =
                responsavelPerfil;
    }
}