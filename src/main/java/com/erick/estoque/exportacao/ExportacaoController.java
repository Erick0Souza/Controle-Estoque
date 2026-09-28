package com.erick.estoque.exportacao;

import com.erick.estoque.movimentacao.TipoMovimentacao;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/admin/exportacoes")
@SecurityRequirement(name = "bearerAuth")
public class ExportacaoController {

    private final ExportacaoCsvService exportacaoCsvService;

    public ExportacaoController(
            ExportacaoCsvService exportacaoCsvService
    ) {
        this.exportacaoCsvService =
                exportacaoCsvService;
    }

    @GetMapping("/estoque/csv")
    public ResponseEntity<byte[]> exportarEstoqueCsv(

            @RequestParam(
                    defaultValue = "false"
            )
            boolean somenteEstoqueBaixo

    ) {

        byte[] arquivo =
                exportacaoCsvService
                        .gerarCsvEstoque(
                                somenteEstoqueBaixo
                        );

        return criarRespostaCsv(
                arquivo,
                "relatorio-estoque.csv"
        );
    }

    @GetMapping("/movimentacoes/csv")
    public ResponseEntity<byte[]> exportarMovimentacoesCsv(

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
                exportacaoCsvService
                        .gerarCsvMovimentacoes(
                                dataInicio,
                                dataFim,
                                tipo
                        );

        return criarRespostaCsv(
                arquivo,
                "relatorio-movimentacoes.csv"
        );
    }

    private ResponseEntity<byte[]> criarRespostaCsv(
            byte[] arquivo,
            String nomeArquivo
    ) {

        HttpHeaders headers =
                new HttpHeaders();

        headers.setContentType(
                MediaType.parseMediaType(
                        "text/csv;charset=UTF-8"
                )
        );

        headers.setContentDisposition(
                ContentDisposition
                        .attachment()
                        .filename(
                                nomeArquivo,
                                StandardCharsets.UTF_8
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