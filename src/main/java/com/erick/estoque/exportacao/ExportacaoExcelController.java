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
public class ExportacaoExcelController {

    private static final String CONTENT_TYPE_EXCEL =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final ExportacaoExcelService exportacaoExcelService;

    public ExportacaoExcelController(
            ExportacaoExcelService exportacaoExcelService
    ) {
        this.exportacaoExcelService =
                exportacaoExcelService;
    }

    @GetMapping(
            value = "/estoque/xlsx",
            produces = CONTENT_TYPE_EXCEL
    )
    public ResponseEntity<byte[]> exportarEstoqueExcel(

            @RequestParam(
                    defaultValue = "false"
            )
            boolean somenteEstoqueBaixo

    ) {

        byte[] arquivo =
                exportacaoExcelService
                        .exportarEstoque(
                                somenteEstoqueBaixo
                        );

        HttpHeaders headers =
                new HttpHeaders();

        headers.setContentType(
                MediaType.parseMediaType(
                        CONTENT_TYPE_EXCEL
                )
        );

        headers.setContentDisposition(
                ContentDisposition
                        .attachment()
                        .filename(
                                "relatorio-estoque.xlsx"
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
            value = "/movimentacoes/xlsx",
            produces = CONTENT_TYPE_EXCEL
    )
    public ResponseEntity<byte[]> exportarMovimentacoesExcel(

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
                exportacaoExcelService
                        .exportarMovimentacoes(
                                dataInicio,
                                dataFim,
                                tipo
                        );

        HttpHeaders headers =
                new HttpHeaders();

        headers.setContentType(
                MediaType.parseMediaType(
                        CONTENT_TYPE_EXCEL
                )
        );

        headers.setContentDisposition(
                ContentDisposition
                        .attachment()
                        .filename(
                                "relatorio-movimentacoes.xlsx"
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