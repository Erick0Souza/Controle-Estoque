package com.erick.estoque.exportacao;

import com.erick.estoque.movimentacao.MovimentacaoEstoque;
import com.erick.estoque.movimentacao.MovimentacaoRepository;
import com.erick.estoque.movimentacao.TipoMovimentacao;
import com.erick.estoque.produto.Produto;
import com.erick.estoque.produto.ProdutoRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class ExportacaoExcelService {

    private final ProdutoRepository produtoRepository;
    private final MovimentacaoRepository movimentacaoRepository;

    public ExportacaoExcelService(
            ProdutoRepository produtoRepository,
            MovimentacaoRepository movimentacaoRepository
    ) {
        this.produtoRepository =
                produtoRepository;

        this.movimentacaoRepository =
                movimentacaoRepository;
    }

    @Transactional(readOnly = true)
    public byte[] exportarEstoque(
            boolean somenteEstoqueBaixo
    ) {

        List<Produto> produtos =
                produtoRepository
                        .findAll()
                        .stream()
                        .filter(
                                produto ->
                                        !somenteEstoqueBaixo ||
                                                estaComEstoqueBaixo(
                                                        produto
                                                )
                        )
                        .sorted(
                                Comparator.comparing(
                                        Produto::getNome,
                                        Comparator.nullsLast(
                                                String.CASE_INSENSITIVE_ORDER
                                        )
                                )
                        )
                        .toList();

        try (
                Workbook workbook =
                        new XSSFWorkbook();

                ByteArrayOutputStream outputStream =
                        new ByteArrayOutputStream()
        ) {

            Sheet sheet =
                    workbook.createSheet(
                            "Estoque"
                    );

            CellStyle estiloCabecalho =
                    criarEstiloCabecalho(
                            workbook
                    );

            CellStyle estiloMoeda =
                    criarEstiloMoeda(
                            workbook
                    );

            CellStyle estiloInteiro =
                    criarEstiloInteiro(
                            workbook
                    );

            CellStyle estiloEstoqueBaixo =
                    criarEstiloEstoqueBaixo(
                            workbook
                    );

            criarCabecalhoEstoque(
                    sheet,
                    estiloCabecalho
            );

            int numeroLinha = 1;

            for (
                    Produto produto :
                    produtos
            ) {

                Row linha =
                        sheet.createRow(
                                numeroLinha++
                        );

                String categoria =
                        produto.getCategoria()
                                == null
                                ? ""
                                : produto
                                .getCategoria()
                                .getNome();

                BigDecimal preco =
                        produto.getPreco()
                                == null
                                ? BigDecimal.ZERO
                                : produto.getPreco();

                int quantidade =
                        produto.getQuantidade()
                                == null
                                ? 0
                                : produto.getQuantidade();

                int estoqueMinimo =
                        produto.getEstoqueMinimo()
                                == null
                                ? 0
                                : produto.getEstoqueMinimo();

                BigDecimal valorEstoque =
                        preco.multiply(
                                BigDecimal.valueOf(
                                        quantidade
                                )
                        );

                boolean estoqueBaixo =
                        estaComEstoqueBaixo(
                                produto
                        );

                criarCelulaTexto(
                        linha,
                        0,
                        produto.getId()
                                == null
                                ? ""
                                : String.valueOf(
                                produto.getId()
                        )
                );

                criarCelulaTexto(
                        linha,
                        1,
                        produto.getSku()
                );

                criarCelulaTexto(
                        linha,
                        2,
                        produto.getNome()
                );

                criarCelulaTexto(
                        linha,
                        3,
                        categoria
                );

                criarCelulaDecimal(
                        linha,
                        4,
                        preco,
                        estiloMoeda
                );

                criarCelulaNumero(
                        linha,
                        5,
                        quantidade,
                        estoqueBaixo
                                ? estiloEstoqueBaixo
                                : estiloInteiro
                );

                criarCelulaNumero(
                        linha,
                        6,
                        estoqueMinimo,
                        estiloInteiro
                );

                criarCelulaTexto(
                        linha,
                        7,
                        estoqueBaixo
                                ? "ESTOQUE BAIXO"
                                : "NORMAL"
                );

                criarCelulaDecimal(
                        linha,
                        8,
                        valorEstoque,
                        estiloMoeda
                );
            }

            ajustarColunas(
                    sheet,
                    9
            );

            sheet.createFreezePane(
                    0,
                    1
            );

            sheet.setAutoFilter(
                    new org.apache.poi.ss.util.CellRangeAddress(
                            0,
                            Math.max(
                                    0,
                                    numeroLinha - 1
                            ),
                            0,
                            8
                    )
            );

            workbook.write(
                    outputStream
            );

            return outputStream
                    .toByteArray();

        } catch (
                IOException exception
        ) {

            throw new IllegalStateException(
                    "Não foi possível gerar o relatório de estoque em Excel.",
                    exception
            );
        }
    }

    @Transactional(readOnly = true)
    public byte[] exportarMovimentacoes(
            LocalDateTime dataInicio,
            LocalDateTime dataFim,
            TipoMovimentacao tipo
    ) {

        List<MovimentacaoEstoque> movimentacoes =
                movimentacaoRepository
                        .findAll()
                        .stream()
                        .filter(
                                movimentacao ->
                                        dentroDoPeriodo(
                                                movimentacao,
                                                dataInicio,
                                                dataFim
                                        )
                        )
                        .filter(
                                movimentacao ->
                                        tipo == null ||
                                                movimentacao.getTipo()
                                                        == tipo
                        )
                        .sorted(
                                Comparator.comparing(
                                        MovimentacaoEstoque::getDataHora,
                                        Comparator.nullsLast(
                                                Comparator.naturalOrder()
                                        )
                                ).reversed()
                        )
                        .toList();

        try (
                Workbook workbook =
                        new XSSFWorkbook();

                ByteArrayOutputStream outputStream =
                        new ByteArrayOutputStream()
        ) {

            Sheet sheet =
                    workbook.createSheet(
                            "Movimentações"
                    );

            CellStyle estiloCabecalho =
                    criarEstiloCabecalho(
                            workbook
                    );

            CellStyle estiloInteiro =
                    criarEstiloInteiro(
                            workbook
                    );

            CellStyle estiloDataHora =
                    criarEstiloDataHora(
                            workbook
                    );

            criarCabecalhoMovimentacoes(
                    sheet,
                    estiloCabecalho
            );

            int numeroLinha = 1;

            for (
                    MovimentacaoEstoque movimentacao :
                    movimentacoes
            ) {

                Row linha =
                        sheet.createRow(
                                numeroLinha++
                        );

                Produto produto =
                        movimentacao
                                .getProduto();

                criarCelulaTexto(
                        linha,
                        0,
                        movimentacao.getId()
                                == null
                                ? ""
                                : String.valueOf(
                                movimentacao.getId()
                        )
                );

                criarCelulaTexto(
                        linha,
                        1,
                        produto == null ||
                                produto.getId() == null
                                ? ""
                                : String.valueOf(
                                produto.getId()
                        )
                );

                criarCelulaTexto(
                        linha,
                        2,
                        produto == null
                                ? ""
                                : produto.getNome()
                );

                criarCelulaTexto(
                        linha,
                        3,
                        movimentacao.getTipo()
                                == null
                                ? ""
                                : movimentacao
                                .getTipo()
                                .name()
                );

                criarCelulaNumero(
                        linha,
                        4,
                        movimentacao.getQuantidade()
                                == null
                                ? 0
                                : movimentacao.getQuantidade(),
                        estiloInteiro
                );

                criarCelulaDataHora(
                        linha,
                        5,
                        movimentacao.getDataHora(),
                        estiloDataHora
                );

                criarCelulaTexto(
                        linha,
                        6,
                        movimentacao
                                .getResponsavelNome()
                );

                criarCelulaTexto(
                        linha,
                        7,
                        movimentacao
                                .getResponsavelEmail()
                );

                criarCelulaTexto(
                        linha,
                        8,
                        movimentacao
                                .getResponsavelPerfil()
                                == null
                                ? ""
                                : movimentacao
                                .getResponsavelPerfil()
                                .name()
                );

                criarCelulaTexto(
                        linha,
                        9,
                        movimentacao
                                .getObservacao()
                );
            }

            ajustarColunas(
                    sheet,
                    10
            );

            sheet.createFreezePane(
                    0,
                    1
            );

            sheet.setAutoFilter(
                    new org.apache.poi.ss.util.CellRangeAddress(
                            0,
                            Math.max(
                                    0,
                                    numeroLinha - 1
                            ),
                            0,
                            9
                    )
            );

            workbook.write(
                    outputStream
            );

            return outputStream
                    .toByteArray();

        } catch (
                IOException exception
        ) {

            throw new IllegalStateException(
                    "Não foi possível gerar o relatório de movimentações em Excel.",
                    exception
            );
        }
    }

    private void criarCabecalhoEstoque(
            Sheet sheet,
            CellStyle estiloCabecalho
    ) {

        String[] colunas = {
                "ID",
                "SKU",
                "Produto",
                "Categoria",
                "Preço Unitário",
                "Quantidade",
                "Estoque Mínimo",
                "Status",
                "Valor em Estoque"
        };

        criarCabecalho(
                sheet,
                colunas,
                estiloCabecalho
        );
    }

    private void criarCabecalhoMovimentacoes(
            Sheet sheet,
            CellStyle estiloCabecalho
    ) {

        String[] colunas = {
                "ID",
                "Produto ID",
                "Produto",
                "Tipo",
                "Quantidade",
                "Data/Hora",
                "Responsável",
                "E-mail",
                "Perfil",
                "Observação"
        };

        criarCabecalho(
                sheet,
                colunas,
                estiloCabecalho
        );
    }

    private void criarCabecalho(
            Sheet sheet,
            String[] colunas,
            CellStyle estiloCabecalho
    ) {

        Row linha =
                sheet.createRow(
                        0
                );

        for (
                int coluna = 0;
                coluna < colunas.length;
                coluna++
        ) {

            Cell celula =
                    linha.createCell(
                            coluna
                    );

            celula.setCellValue(
                    colunas[coluna]
            );

            celula.setCellStyle(
                    estiloCabecalho
            );
        }
    }

    private CellStyle criarEstiloCabecalho(
            Workbook workbook
    ) {

        CellStyle estilo =
                workbook.createCellStyle();

        Font fonte =
                workbook.createFont();

        fonte.setBold(
                true
        );

        estilo.setFont(
                fonte
        );

        estilo.setAlignment(
                HorizontalAlignment.CENTER
        );

        estilo.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        estilo.setBorderBottom(
                BorderStyle.THIN
        );

        estilo.setBorderTop(
                BorderStyle.THIN
        );

        estilo.setBorderLeft(
                BorderStyle.THIN
        );

        estilo.setBorderRight(
                BorderStyle.THIN
        );

        return estilo;
    }

    private CellStyle criarEstiloMoeda(
            Workbook workbook
    ) {

        CellStyle estilo =
                workbook.createCellStyle();

        DataFormat dataFormat =
                workbook.createDataFormat();

        estilo.setDataFormat(
                dataFormat.getFormat(
                        "R$ #,##0.00"
                )
        );

        return estilo;
    }

    private CellStyle criarEstiloInteiro(
            Workbook workbook
    ) {

        CellStyle estilo =
                workbook.createCellStyle();

        DataFormat dataFormat =
                workbook.createDataFormat();

        estilo.setDataFormat(
                dataFormat.getFormat(
                        "0"
                )
        );

        return estilo;
    }

    private CellStyle criarEstiloDataHora(
            Workbook workbook
    ) {

        CellStyle estilo =
                workbook.createCellStyle();

        DataFormat dataFormat =
                workbook.createDataFormat();

        estilo.setDataFormat(
                dataFormat.getFormat(
                        "dd/mm/yyyy hh:mm:ss"
                )
        );

        return estilo;
    }

    private CellStyle criarEstiloEstoqueBaixo(
            Workbook workbook
    ) {

        CellStyle estilo =
                criarEstiloInteiro(
                        workbook
                );

        Font fonte =
                workbook.createFont();

        fonte.setBold(
                true
        );

        estilo.setFont(
                fonte
        );

        return estilo;
    }

    private void criarCelulaTexto(
            Row linha,
            int coluna,
            String valor
    ) {

        Cell celula =
                linha.createCell(
                        coluna
                );

        celula.setCellValue(
                valor == null
                        ? ""
                        : valor
        );
    }

    private void criarCelulaNumero(
            Row linha,
            int coluna,
            int valor,
            CellStyle estilo
    ) {

        Cell celula =
                linha.createCell(
                        coluna
                );

        celula.setCellValue(
                valor
        );

        if (
                estilo != null
        ) {
            celula.setCellStyle(
                    estilo
            );
        }
    }

    private void criarCelulaDecimal(
            Row linha,
            int coluna,
            BigDecimal valor,
            CellStyle estilo
    ) {

        Cell celula =
                linha.createCell(
                        coluna
                );

        celula.setCellValue(
                valor == null
                        ? 0
                        : valor.doubleValue()
        );

        if (
                estilo != null
        ) {
            celula.setCellStyle(
                    estilo
            );
        }
    }

    private void criarCelulaDataHora(
            Row linha,
            int coluna,
            LocalDateTime valor,
            CellStyle estilo
    ) {

        Cell celula =
                linha.createCell(
                        coluna
                );

        if (
                valor != null
        ) {

            celula.setCellValue(
                    valor
            );

            celula.setCellStyle(
                    estilo
            );
        }
    }

    private void ajustarColunas(
            Sheet sheet,
            int quantidadeColunas
    ) {

        for (
                int coluna = 0;
                coluna < quantidadeColunas;
                coluna++
        ) {

            sheet.autoSizeColumn(
                    coluna
            );

            int larguraAtual =
                    sheet.getColumnWidth(
                            coluna
                    );

            int larguraComEspaco =
                    larguraAtual + 1000;

            int larguraMaxima =
                    15000;

            sheet.setColumnWidth(
                    coluna,
                    Math.min(
                            larguraComEspaco,
                            larguraMaxima
                    )
            );
        }
    }

    private boolean estaComEstoqueBaixo(
            Produto produto
    ) {

        int quantidade =
                produto.getQuantidade()
                        == null
                        ? 0
                        : produto.getQuantidade();

        int estoqueMinimo =
                produto.getEstoqueMinimo()
                        == null
                        ? 0
                        : produto.getEstoqueMinimo();

        return quantidade <=
                estoqueMinimo;
    }

    private boolean dentroDoPeriodo(
            MovimentacaoEstoque movimentacao,
            LocalDateTime dataInicio,
            LocalDateTime dataFim
    ) {

        LocalDateTime dataHora =
                movimentacao
                        .getDataHora();

        if (
                dataHora == null
        ) {
            return false;
        }

        if (
                dataInicio != null &&
                        dataHora.isBefore(
                                dataInicio
                        )
        ) {
            return false;
        }

        if (
                dataFim != null &&
                        dataHora.isAfter(
                                dataFim
                        )
        ) {
            return false;
        }

        return true;
    }
}