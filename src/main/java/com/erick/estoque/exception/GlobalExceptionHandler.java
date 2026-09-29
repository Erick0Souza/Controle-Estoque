package com.erick.estoque.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    GlobalExceptionHandler.class
            );

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> tratarResponseStatus(
            ResponseStatusException exception
    ) {

        int status =
                exception
                        .getStatusCode()
                        .value();

        String mensagem =
                exception.getReason();

        if (
                mensagem == null ||
                        mensagem.isBlank()
        ) {

            mensagem =
                    mensagemPadraoStatus(
                            status
                    );
        }

        if (
                status >= 500
        ) {

            LOGGER.error(
                    "Erro HTTP interno tratado. Status: {}",
                    status,
                    exception
            );
        }

        ApiError erro =
                new ApiError(
                        status,
                        mensagem,
                        LocalDateTime.now()
                );

        return ResponseEntity
                .status(
                        status
                )
                .body(
                        erro
                );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> tratarValidacao(
            MethodArgumentNotValidException exception
    ) {

        String mensagem =
                exception
                        .getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(
                                erro ->
                                        erro.getField()
                                                + ": "
                                                + erro.getDefaultMessage()
                        )
                        .collect(
                                Collectors.joining(
                                        ", "
                                )
                        );

        ApiError erro =
                new ApiError(
                        HttpStatus.BAD_REQUEST.value(),
                        mensagem,
                        LocalDateTime.now()
                );

        return ResponseEntity
                .badRequest()
                .body(
                        erro
                );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> tratarCorpoRequisicaoInvalido(
            HttpMessageNotReadableException exception
    ) {

        LOGGER.warn(
                "Corpo de requisição inválido recebido."
        );

        ApiError erro =
                new ApiError(
                        HttpStatus.BAD_REQUEST.value(),
                        "O corpo da requisição é inválido.",
                        LocalDateTime.now()
                );

        return ResponseEntity
                .badRequest()
                .body(
                        erro
                );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> tratarParametroObrigatorioAusente(
            MissingServletRequestParameterException exception
    ) {

        ApiError erro =
                new ApiError(
                        HttpStatus.BAD_REQUEST.value(),
                        "Parâmetro obrigatório não informado: "
                                + exception.getParameterName(),
                        LocalDateTime.now()
                );

        return ResponseEntity
                .badRequest()
                .body(
                        erro
                );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> tratarTipoParametroInvalido(
            MethodArgumentTypeMismatchException exception
    ) {

        ApiError erro =
                new ApiError(
                        HttpStatus.BAD_REQUEST.value(),
                        "Valor inválido para o parâmetro: "
                                + exception.getName(),
                        LocalDateTime.now()
                );

        return ResponseEntity
                .badRequest()
                .body(
                        erro
                );
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiError> tratarArquivoMuitoGrande(
            MaxUploadSizeExceededException exception
    ) {

        LOGGER.warn(
                "Upload rejeitado por exceder o tamanho máximo permitido."
        );

        ApiError erro =
                new ApiError(
                        HttpStatus.PAYLOAD_TOO_LARGE.value(),
                        "O arquivo enviado excede o tamanho máximo permitido.",
                        LocalDateTime.now()
                );

        return ResponseEntity
                .status(
                        HttpStatus.PAYLOAD_TOO_LARGE
                )
                .body(
                        erro
                );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> tratarConflitoIntegridade(
            DataIntegrityViolationException exception
    ) {

        LOGGER.error(
                "Erro de integridade de dados ao processar requisição.",
                exception
        );

        ApiError erro =
                new ApiError(
                        HttpStatus.CONFLICT.value(),
                        "Não foi possível concluir a operação devido a um conflito nos dados.",
                        LocalDateTime.now()
                );

        return ResponseEntity
                .status(
                        HttpStatus.CONFLICT
                )
                .body(
                        erro
                );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> tratarErroInterno(
            Exception exception
    ) {

        LOGGER.error(
                "Erro interno não tratado.",
                exception
        );

        ApiError erro =
                new ApiError(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Ocorreu um erro interno. Tente novamente.",
                        LocalDateTime.now()
                );

        return ResponseEntity
                .status(
                        HttpStatus.INTERNAL_SERVER_ERROR
                )
                .body(
                        erro
                );
    }

    private String mensagemPadraoStatus(
            int status
    ) {

        return switch (
                status
                ) {

            case 400 ->
                    "Requisição inválida.";

            case 401 ->
                    "Autenticação necessária.";

            case 403 ->
                    "Você não possui permissão para realizar esta ação.";

            case 404 ->
                    "Recurso não encontrado.";

            case 409 ->
                    "Não foi possível concluir a operação devido a um conflito.";

            case 429 ->
                    "Muitas solicitações. Tente novamente em alguns minutos.";

            default ->
                    status >= 500
                            ? "Ocorreu um erro interno. Tente novamente."
                            : "Não foi possível concluir a solicitação.";
        };
    }
}