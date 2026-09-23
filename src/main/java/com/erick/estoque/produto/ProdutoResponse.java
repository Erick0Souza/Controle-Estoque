package com.erick.estoque.produto;

import java.math.BigDecimal;

public record ProdutoResponse(

        Long id,
        String nome,
        BigDecimal preco,
        Integer quantidade,
        Long categoriaId,
        String categoriaNome,
        String descricao

) {
}
