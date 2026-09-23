package com.erick.estoque.movimentacao;

import java.time.LocalDateTime;

public record MovimentacaoResponse(

        Long id,

        Long produtoId,

        String produtoNome,

        TipoMovimentacao tipo,

        Integer quantidade,

        Integer estoqueAtual,

        String observacao,

        LocalDateTime dataHora

) {
}