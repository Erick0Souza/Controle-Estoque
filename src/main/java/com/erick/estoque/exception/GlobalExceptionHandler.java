package com.erick.estoque.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> tratarResponseStatus(
            ResponseStatusException exception
    ) {

        int status = exception.getStatusCode().value();

        ApiError erro = new ApiError(
                status,
                exception.getReason(),
                LocalDateTime.now()
        );

        return ResponseEntity
                .status(status)
                .body(erro);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> tratarValidacao(
            MethodArgumentNotValidException exception
    ) {

        String mensagem = exception
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .map(erro ->
                        erro.getField()
                                + ": "
                                + erro.getDefaultMessage()
                )
                .collect(Collectors.joining(", "));

        ApiError erro = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                mensagem,
                LocalDateTime.now()
        );

        return ResponseEntity
                .badRequest()
                .body(erro);
    }
}