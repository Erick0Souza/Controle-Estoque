package com.erick.estoque.relatorio;

import com.erick.estoque.movimentacao.TipoMovimentacao;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/admin/relatorios")
@SecurityRequirement(name = "bearerAuth")
public class RelatorioController {

    private final RelatorioService relatorioService;

    public RelatorioController(
            RelatorioService relatorioService
    ) {
        this.relatorioService =
                relatorioService;
    }

    @GetMapping("/estoque")
    public RelatorioEstoqueResponse relatorioEstoque(
            @RequestParam(
                    defaultValue = "false"
            )
            boolean somenteEstoqueBaixo
    ) {

        return relatorioService
                .gerarRelatorioEstoque(
                        somenteEstoqueBaixo
                );
    }

    @GetMapping("/movimentacoes")
    public RelatorioMovimentacaoResponse relatorioMovimentacoes(

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

        return relatorioService
                .gerarRelatorioMovimentacoes(
                        dataInicio,
                        dataFim,
                        tipo
                );
    }
}