package com.erick.estoque.relatorio;

import com.erick.estoque.movimentacao.TipoMovimentacao;
import com.erick.estoque.security.PerfilUsuario;

import java.time.LocalDateTime;
import java.util.List;

public record RelatorioMovimentacaoResponse(

        long totalMovimentacoes,

        long totalEntradas,

        long totalSaidas,

        long unidadesEntrada,

        long unidadesSaida,

        List<MovimentacaoRelatorioItem> movimentacoes

) {

    public record MovimentacaoRelatorioItem(

            Long id,

            Long produtoId,

            String produtoNome,

            TipoMovimentacao tipo,

            Integer quantidade,

            LocalDateTime dataHora,

            String responsavelNome,

            String responsavelEmail,

            PerfilUsuario responsavelPerfil,

            String observacao

    ) {
    }
}