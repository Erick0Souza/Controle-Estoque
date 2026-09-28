package com.erick.estoque.relatorio;

import java.math.BigDecimal;
import java.util.List;

public record RelatorioEstoqueResponse(

        long totalProdutos,

        long totalUnidades,

        long produtosEstoqueBaixo,

        BigDecimal valorTotalEstoque,

        List<ProdutoRelatorioResponse> produtos

) {

    public record ProdutoRelatorioResponse(

            Long id,

            String sku,

            String nome,

            String categoria,

            BigDecimal preco,

            Integer quantidade,

            Integer estoqueMinimo,

            boolean estoqueBaixo,

            BigDecimal valorEmEstoque

    ) {
    }
}