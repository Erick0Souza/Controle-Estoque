package com.erick.estoque.produto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProdutoRequest(

        @NotBlank(message = "O nome do produto é obrigatório")
        String nome,

        @NotNull(message = "O preço é obrigatório")
        @PositiveOrZero(message = "O preço não pode ser negativo")
        BigDecimal preco,

        @NotNull(message = "A quantidade é obrigatória")
        @PositiveOrZero(message = "A quantidade não pode ser negativa")
        Integer quantidade,

        @NotNull(message = "A categoria é obrigatória")
        Long categoriaId,

        @Size(max = 500, message = "A descrição deve ter no máximo 500 caracteres")
        String descricao

) {
}