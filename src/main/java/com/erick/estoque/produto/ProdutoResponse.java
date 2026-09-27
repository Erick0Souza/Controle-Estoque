package com.erick.estoque.produto;

import java.math.BigDecimal;

public record ProdutoResponse(

        Long id,
        String sku,
        String nome,
        BigDecimal preco,
        Integer quantidade,
        Integer estoqueMinimo,
        Boolean estoqueBaixo,
        Long categoriaId,
        String categoriaNome,
        String descricao,
        String imagemUrl

) {
}