package com.erick.estoque.movimentacao;

import com.erick.estoque.security.PerfilUsuario;

import java.time.LocalDateTime;

public record MovimentacaoResponse(

        Long id,

        Long produtoId,

        String produtoNome,

        TipoMovimentacao tipo,

        Integer quantidade,

        Integer estoqueAtual,

        String responsavelNome,

        String responsavelEmail,

        PerfilUsuario responsavelPerfil,

        String observacao,

        LocalDateTime dataHora

) {
}