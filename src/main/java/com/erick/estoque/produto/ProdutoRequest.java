package com.erick.estoque.produto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProdutoRequest(

        @NotBlank(message = "O SKU é obrigatório")
        @Size(
                max = 50,
                message = "O SKU deve ter no máximo 50 caracteres"
        )
        @Pattern(
                regexp = "^[A-Za-z0-9_-]+$",
                message = "O SKU deve conter apenas letras, números, hífen ou underline"
        )
        String sku,

        @NotBlank(message = "O nome do produto é obrigatório")
        @Size(
                max = 150,
                message = "O nome deve ter no máximo 150 caracteres"
        )
        String nome,

        @NotNull(message = "O preço é obrigatório")
        @PositiveOrZero(message = "O preço não pode ser negativo")
        BigDecimal preco,

        @NotNull(message = "A quantidade é obrigatória")
        @PositiveOrZero(message = "A quantidade não pode ser negativa")
        Integer quantidade,

        @NotNull(message = "O estoque mínimo é obrigatório")
        @PositiveOrZero(message = "O estoque mínimo não pode ser negativo")
        Integer estoqueMinimo,

        @NotNull(message = "A categoria é obrigatória")
        Long categoriaId,

        @Size(
                max = 500,
                message = "A descrição deve ter no máximo 500 caracteres"
        )
        String descricao

) {
}