package com.erick.estoque.exception;

import java.time.LocalDateTime;

public record ApiError(
        int status,
        String mensagem,
        LocalDateTime timestamp
) {
}