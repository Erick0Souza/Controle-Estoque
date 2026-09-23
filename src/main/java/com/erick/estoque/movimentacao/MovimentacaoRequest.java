package com.erick.estoque.movimentacao;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record MovimentacaoRequest(

        @NotNull(message = "O produto é obrigatório")
        Long produtoId,

        @NotNull(message = "O tipo da movimentação é obrigatório")
        TipoMovimentacao tipo,

        @NotNull(message = "A quantidade é obrigatória")
        @Positive(message = "A quantidade deve ser maior que zero")
        Integer quantidade,

        @Size(
                max = 500,
                message = "A observação deve ter no máximo 500 caracteres"
        )
        String observacao

) {
}