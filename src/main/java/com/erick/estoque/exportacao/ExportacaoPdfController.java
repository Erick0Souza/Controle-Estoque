package com.erick.estoque.exportacao;

import com.erick.estoque.movimentacao.TipoMovimentacao;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/admin/exportacoes")
@SecurityRequirement(name = "bearerAuth")
public class ExportacaoPdfController {

    private static final String CONTENT_TYPE_PDF =
            "application/pdf";

    private final ExportacaoPdfService exportacaoPdfService;

    public ExportacaoPdfController(
            ExportacaoPdfService exportacaoPdfService
    ) {
        this.exportacaoPdfService =
                exportacaoPdfService;
    }

    @GetMapping(
            value = "/estoque/pdf",
            produces = CONTENT_TYPE_PDF
    )
    public ResponseEntity<byte[]> exportarEstoquePdf(

            @RequestParam(
                    defaultValue = "false"
            )
            boolean somenteEstoqueBaixo

    ) {

        byte[] arquivo =
                exportacaoPdfService
                        .exportarEstoque(
                                somenteEstoqueBaixo
                        );

        HttpHeaders headers =
                new HttpHeaders();

        headers.setContentType(
                MediaType.APPLICATION_PDF
        );

        headers.setContentDisposition(
                ContentDisposition
                        .attachment()
                        .filename(
                                "relatorio-estoque.pdf"
                        )
                        .build()
        );

        headers.setContentLength(
                arquivo.length
        );

        return new ResponseEntity<>(
                arquivo,
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping(
            value = "/movimentacoes/pdf",
            produces = CONTENT_TYPE_PDF
    )
    public ResponseEntity<byte[]> exportarMovimentacoesPdf(

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE_TIME
            )
            LocalDateTime dataInicio,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE_TIME
            )
            LocalDateTime dataFim,

            @RequestParam(required = false)
            TipoMovimentacao tipo

    ) {

        if (
                dataInicio != null &&
                        dataFim != null &&
                        dataInicio.isAfter(
                                dataFim
                        )
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A data inicial não pode ser posterior à data final"
            );
        }

        byte[] arquivo =
                exportacaoPdfService
                        .exportarMovimentacoes(
                                dataInicio,
                                dataFim,
                                tipo
                        );

        HttpHeaders headers =
                new HttpHeaders();

        headers.setContentType(
                MediaType.APPLICATION_PDF
        );

        headers.setContentDisposition(
                ContentDisposition
                        .attachment()
                        .filename(
                                "relatorio-movimentacoes.pdf"
                        )
                        .build()
        );

        headers.setContentLength(
                arquivo.length
        );

        return new ResponseEntity<>(
                arquivo,
                headers,
                HttpStatus.OK
        );
    }
}