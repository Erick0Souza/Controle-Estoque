package com.erick.estoque.exportacao;

import com.erick.estoque.movimentacao.TipoMovimentacao;
import com.erick.estoque.relatorio.RelatorioEstoqueResponse;
import com.erick.estoque.relatorio.RelatorioMovimentacaoResponse;
import com.erick.estoque.relatorio.RelatorioService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class ExportacaoCsvService {

    private static final DateTimeFormatter FORMATADOR_DATA =
            DateTimeFormatter.ofPattern(
                    "dd/MM/yyyy HH:mm:ss"
            );

    private final RelatorioService relatorioService;

    public ExportacaoCsvService(
            RelatorioService relatorioService
    ) {
        this.relatorioService =
                relatorioService;
    }

    public byte[] gerarCsvEstoque(
            boolean somenteEstoqueBaixo
    ) {

        RelatorioEstoqueResponse relatorio =
                relatorioService
                        .gerarRelatorioEstoque(
                                somenteEstoqueBaixo
                        );

        StringBuilder csv =
                new StringBuilder();

        adicionarBomUtf8(
                csv
        );

        csv.append(
                "RELATORIO DE ESTOQUE\n"
        );

        csv.append(
                "Total de produtos;"
        ).append(
                relatorio.totalProdutos()
        ).append(
                "\n"
        );

        csv.append(
                "Total de unidades;"
        ).append(
                relatorio.totalUnidades()
        ).append(
                "\n"
        );

        csv.append(
                "Produtos com estoque baixo;"
        ).append(
                relatorio.produtosEstoqueBaixo()
        ).append(
                "\n"
        );

        csv.append(
                "Valor total do estoque;"
        ).append(
                formatarDecimal(
                        relatorio.valorTotalEstoque()
                )
        ).append(
                "\n\n"
        );

        csv.append(
                "ID;SKU;Produto;Categoria;Preco;Quantidade;Estoque minimo;Status;Valor em estoque\n"
        );

        for (
                RelatorioEstoqueResponse.ProdutoRelatorioResponse produto
                : relatorio.produtos()
        ) {

            csv.append(
                    produto.id()
            ).append(
                    ";"
            );

            csv.append(
                    escapar(
                            produto.sku()
                    )
            ).append(
                    ";"
            );

            csv.append(
                    escapar(
                            produto.nome()
                    )
            ).append(
                    ";"
            );

            csv.append(
                    escapar(
                            produto.categoria()
                    )
            ).append(
                    ";"
            );

            csv.append(
                    formatarDecimal(
                            produto.preco()
                    )
            ).append(
                    ";"
            );

            csv.append(
                    produto.quantidade()
            ).append(
                    ";"
            );

            csv.append(
                    produto.estoqueMinimo()
            ).append(
                    ";"
            );

            csv.append(
                    produto.estoqueBaixo()
                            ? "ESTOQUE BAIXO"
                            : "OK"
            ).append(
                    ";"
            );

            csv.append(
                    formatarDecimal(
                            produto.valorEmEstoque()
                    )
            ).append(
                    "\n"
            );
        }

        return csv
                .toString()
                .getBytes(
                        StandardCharsets.UTF_8
                );
    }

    public byte[] gerarCsvMovimentacoes(
            LocalDateTime dataInicio,
            LocalDateTime dataFim,
            TipoMovimentacao tipo
    ) {

        RelatorioMovimentacaoResponse relatorio =
                relatorioService
                        .gerarRelatorioMovimentacoes(
                                dataInicio,
                                dataFim,
                                tipo
                        );

        StringBuilder csv =
                new StringBuilder();

        adicionarBomUtf8(
                csv
        );

        csv.append(
                "RELATORIO DE MOVIMENTACOES\n"
        );

        csv.append(
                "Total de movimentacoes;"
        ).append(
                relatorio.totalMovimentacoes()
        ).append(
                "\n"
        );

        csv.append(
                "Total de entradas;"
        ).append(
                relatorio.totalEntradas()
        ).append(
                "\n"
        );

        csv.append(
                "Total de saidas;"
        ).append(
                relatorio.totalSaidas()
        ).append(
                "\n"
        );

        csv.append(
                "Unidades de entrada;"
        ).append(
                relatorio.unidadesEntrada()
        ).append(
                "\n"
        );

        csv.append(
                "Unidades de saida;"
        ).append(
                relatorio.unidadesSaida()
        ).append(
                "\n\n"
        );

        csv.append(
                "ID;Produto ID;Produto;Tipo;Quantidade;Data e hora;Responsavel;Email;Perfil;Observacao\n"
        );

        for (
                RelatorioMovimentacaoResponse.MovimentacaoRelatorioItem movimentacao
                : relatorio.movimentacoes()
        ) {

            csv.append(
                    movimentacao.id()
            ).append(
                    ";"
            );

            csv.append(
                    movimentacao.produtoId()
            ).append(
                    ";"
            );

            csv.append(
                    escapar(
                            movimentacao.produtoNome()
                    )
            ).append(
                    ";"
            );

            csv.append(
                    movimentacao.tipo()
            ).append(
                    ";"
            );

            csv.append(
                    movimentacao.quantidade()
            ).append(
                    ";"
            );

            csv.append(
                    formatarData(
                            movimentacao.dataHora()
                    )
            ).append(
                    ";"
            );

            csv.append(
                    escapar(
                            movimentacao.responsavelNome()
                    )
            ).append(
                    ";"
            );

            csv.append(
                    escapar(
                            movimentacao.responsavelEmail()
                    )
            ).append(
                    ";"
            );

            csv.append(
                    movimentacao.responsavelPerfil()
                            == null
                            ? ""
                            : movimentacao
                            .responsavelPerfil()
                            .name()
            ).append(
                    ";"
            );

            csv.append(
                    escapar(
                            movimentacao.observacao()
                    )
            ).append(
                    "\n"
            );
        }

        return csv
                .toString()
                .getBytes(
                        StandardCharsets.UTF_8
                );
    }

    private void adicionarBomUtf8(
            StringBuilder csv
    ) {

        csv.append(
                '\uFEFF'
        );
    }

    private String escapar(
            String valor
    ) {

        if (
                valor == null
        ) {
            return "";
        }

        String tratado =
                valor.replace(
                        "\"",
                        "\"\""
                );

        if (
                tratado.contains(";") ||
                        tratado.contains("\n") ||
                        tratado.contains("\r") ||
                        tratado.contains("\"")
        ) {

            return "\"" +
                    tratado +
                    "\"";
        }

        return tratado;
    }

    private String formatarDecimal(
            BigDecimal valor
    ) {

        if (
                valor == null
        ) {
            return "0,00";
        }

        return valor
                .setScale(
                        2,
                        java.math.RoundingMode.HALF_UP
                )
                .toPlainString()
                .replace(
                        ".",
                        ","
                );
    }

    private String formatarData(
            LocalDateTime dataHora
    ) {

        if (
                dataHora == null
        ) {
            return "";
        }

        return dataHora.format(
                FORMATADOR_DATA
        );
    }
}