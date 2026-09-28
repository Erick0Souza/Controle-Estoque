package com.erick.estoque.exportacao;

import com.erick.estoque.movimentacao.MovimentacaoEstoque;
import com.erick.estoque.movimentacao.MovimentacaoRepository;
import com.erick.estoque.movimentacao.TipoMovimentacao;
import com.erick.estoque.produto.Produto;
import com.erick.estoque.produto.ProdutoRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Service
public class ExportacaoPdfService {

    private static final DateTimeFormatter FORMATADOR_DATA =
            DateTimeFormatter.ofPattern(
                    "dd/MM/yyyy HH:mm:ss"
            );

    private static final int LINHAS_POR_PAGINA_ESTOQUE =
            24;

    private static final int LINHAS_POR_PAGINA_MOVIMENTACAO =
            22;

    private final ProdutoRepository produtoRepository;
    private final MovimentacaoRepository movimentacaoRepository;

    public ExportacaoPdfService(
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
                PDDocument documento =
                        new PDDocument();

                ByteArrayOutputStream outputStream =
                        new ByteArrayOutputStream()
        ) {

            if (
                    produtos.isEmpty()
            ) {

                criarPaginaEstoque(
                        documento,
                        List.of(),
                        1,
                        1,
                        somenteEstoqueBaixo
                );

            } else {

                int totalPaginas =
                        (int) Math.ceil(
                                (double) produtos.size() /
                                        LINHAS_POR_PAGINA_ESTOQUE
                        );

                for (
                        int pagina = 0;
                        pagina < totalPaginas;
                        pagina++
                ) {

                    int inicio =
                            pagina *
                                    LINHAS_POR_PAGINA_ESTOQUE;

                    int fim =
                            Math.min(
                                    inicio +
                                            LINHAS_POR_PAGINA_ESTOQUE,
                                    produtos.size()
                            );

                    List<Produto> produtosPagina =
                            produtos.subList(
                                    inicio,
                                    fim
                            );

                    criarPaginaEstoque(
                            documento,
                            produtosPagina,
                            pagina + 1,
                            totalPaginas,
                            somenteEstoqueBaixo
                    );
                }
            }

            documento.save(
                    outputStream
            );

            return outputStream
                    .toByteArray();

        } catch (
                IOException exception
        ) {

            throw new IllegalStateException(
                    "Não foi possível gerar o relatório de estoque em PDF.",
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
                PDDocument documento =
                        new PDDocument();

                ByteArrayOutputStream outputStream =
                        new ByteArrayOutputStream()
        ) {

            if (
                    movimentacoes.isEmpty()
            ) {

                criarPaginaMovimentacoes(
                        documento,
                        List.of(),
                        1,
                        1,
                        dataInicio,
                        dataFim,
                        tipo
                );

            } else {

                int totalPaginas =
                        (int) Math.ceil(
                                (double) movimentacoes.size() /
                                        LINHAS_POR_PAGINA_MOVIMENTACAO
                        );

                for (
                        int pagina = 0;
                        pagina < totalPaginas;
                        pagina++
                ) {

                    int inicio =
                            pagina *
                                    LINHAS_POR_PAGINA_MOVIMENTACAO;

                    int fim =
                            Math.min(
                                    inicio +
                                            LINHAS_POR_PAGINA_MOVIMENTACAO,
                                    movimentacoes.size()
                            );

                    List<MovimentacaoEstoque> movimentacoesPagina =
                            movimentacoes.subList(
                                    inicio,
                                    fim
                            );

                    criarPaginaMovimentacoes(
                            documento,
                            movimentacoesPagina,
                            pagina + 1,
                            totalPaginas,
                            dataInicio,
                            dataFim,
                            tipo
                    );
                }
            }

            documento.save(
                    outputStream
            );

            return outputStream
                    .toByteArray();

        } catch (
                IOException exception
        ) {

            throw new IllegalStateException(
                    "Não foi possível gerar o relatório de movimentações em PDF.",
                    exception
            );
        }
    }

    private void criarPaginaEstoque(
            PDDocument documento,
            List<Produto> produtos,
            int paginaAtual,
            int totalPaginas,
            boolean somenteEstoqueBaixo
    ) throws IOException {

        PDPage pagina =
                new PDPage(
                        new PDRectangle(
                                PDRectangle.A4.getHeight(),
                                PDRectangle.A4.getWidth()
                        )
                );

        documento.addPage(
                pagina
        );

        PDType1Font fonteNormal =
                new PDType1Font(
                        Standard14Fonts.FontName.HELVETICA
                );

        PDType1Font fonteNegrito =
                new PDType1Font(
                        Standard14Fonts.FontName.HELVETICA_BOLD
                );

        try (
                PDPageContentStream contentStream =
                        new PDPageContentStream(
                                documento,
                                pagina
                        )
        ) {

            escreverTexto(
                    contentStream,
                    fonteNegrito,
                    18,
                    40,
                    555,
                    "Relatório de Estoque"
            );

            escreverTexto(
                    contentStream,
                    fonteNormal,
                    9,
                    40,
                    535,
                    "Filtro: " +
                            (
                                    somenteEstoqueBaixo
                                            ? "Somente produtos com estoque baixo"
                                            : "Todos os produtos"
                            )
            );

            escreverTexto(
                    contentStream,
                    fonteNormal,
                    9,
                    690,
                    535,
                    "Página " +
                            paginaAtual +
                            " de " +
                            totalPaginas
            );

            float y =
                    505;

            escreverTexto(
                    contentStream,
                    fonteNegrito,
                    8,
                    40,
                    y,
                    "SKU"
            );

            escreverTexto(
                    contentStream,
                    fonteNegrito,
                    8,
                    110,
                    y,
                    "Produto"
            );

            escreverTexto(
                    contentStream,
                    fonteNegrito,
                    8,
                    280,
                    y,
                    "Categoria"
            );

            escreverTexto(
                    contentStream,
                    fonteNegrito,
                    8,
                    390,
                    y,
                    "Preço"
            );

            escreverTexto(
                    contentStream,
                    fonteNegrito,
                    8,
                    460,
                    y,
                    "Qtd."
            );

            escreverTexto(
                    contentStream,
                    fonteNegrito,
                    8,
                    505,
                    y,
                    "Mín."
            );

            escreverTexto(
                    contentStream,
                    fonteNegrito,
                    8,
                    550,
                    y,
                    "Status"
            );

            escreverTexto(
                    contentStream,
                    fonteNegrito,
                    8,
                    650,
                    y,
                    "Valor estoque"
            );

            y -= 20;

            if (
                    produtos.isEmpty()
            ) {

                escreverTexto(
                        contentStream,
                        fonteNormal,
                        10,
                        40,
                        y,
                        "Nenhum produto encontrado para os filtros informados."
                );

                return;
            }

            for (
                    Produto produto :
                    produtos
            ) {

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

                escreverTexto(
                        contentStream,
                        fonteNormal,
                        8,
                        40,
                        y,
                        limitarTexto(
                                produto.getSku(),
                                12
                        )
                );

                escreverTexto(
                        contentStream,
                        fonteNormal,
                        8,
                        110,
                        y,
                        limitarTexto(
                                produto.getNome(),
                                28
                        )
                );

                escreverTexto(
                        contentStream,
                        fonteNormal,
                        8,
                        280,
                        y,
                        limitarTexto(
                                categoria,
                                17
                        )
                );

                escreverTexto(
                        contentStream,
                        fonteNormal,
                        8,
                        390,
                        y,
                        formatarMoeda(
                                preco
                        )
                );

                escreverTexto(
                        contentStream,
                        fonteNormal,
                        8,
                        460,
                        y,
                        String.valueOf(
                                quantidade
                        )
                );

                escreverTexto(
                        contentStream,
                        fonteNormal,
                        8,
                        505,
                        y,
                        String.valueOf(
                                estoqueMinimo
                        )
                );

                escreverTexto(
                        contentStream,
                        estaComEstoqueBaixo(
                                produto
                        )
                                ? fonteNegrito
                                : fonteNormal,
                        8,
                        550,
                        y,
                        estaComEstoqueBaixo(
                                produto
                        )
                                ? "ESTOQUE BAIXO"
                                : "NORMAL"
                );

                escreverTexto(
                        contentStream,
                        fonteNormal,
                        8,
                        650,
                        y,
                        formatarMoeda(
                                valorEstoque
                        )
                );

                y -= 18;
            }
        }
    }

    private void criarPaginaMovimentacoes(
            PDDocument documento,
            List<MovimentacaoEstoque> movimentacoes,
            int paginaAtual,
            int totalPaginas,
            LocalDateTime dataInicio,
            LocalDateTime dataFim,
            TipoMovimentacao tipo
    ) throws IOException {

        PDPage pagina =
                new PDPage(
                        new PDRectangle(
                                PDRectangle.A4.getHeight(),
                                PDRectangle.A4.getWidth()
                        )
                );

        documento.addPage(
                pagina
        );

        PDType1Font fonteNormal =
                new PDType1Font(
                        Standard14Fonts.FontName.HELVETICA
                );

        PDType1Font fonteNegrito =
                new PDType1Font(
                        Standard14Fonts.FontName.HELVETICA_BOLD
                );

        try (
                PDPageContentStream contentStream =
                        new PDPageContentStream(
                                documento,
                                pagina
                        )
        ) {

            escreverTexto(
                    contentStream,
                    fonteNegrito,
                    18,
                    40,
                    555,
                    "Relatório de Movimentações"
            );

            String filtro =
                    "Tipo: " +
                            (
                                    tipo == null
                                            ? "Todos"
                                            : tipo.name()
                            ) +
                            " | Início: " +
                            formatarDataFiltro(
                                    dataInicio
                            ) +
                            " | Fim: " +
                            formatarDataFiltro(
                                    dataFim
                            );

            escreverTexto(
                    contentStream,
                    fonteNormal,
                    9,
                    40,
                    535,
                    filtro
            );

            escreverTexto(
                    contentStream,
                    fonteNormal,
                    9,
                    690,
                    535,
                    "Página " +
                            paginaAtual +
                            " de " +
                            totalPaginas
            );

            float y =
                    505;

            escreverTexto(
                    contentStream,
                    fonteNegrito,
                    8,
                    40,
                    y,
                    "Produto"
            );

            escreverTexto(
                    contentStream,
                    fonteNegrito,
                    8,
                    190,
                    y,
                    "Tipo"
            );

            escreverTexto(
                    contentStream,
                    fonteNegrito,
                    8,
                    250,
                    y,
                    "Qtd."
            );

            escreverTexto(
                    contentStream,
                    fonteNegrito,
                    8,
                    295,
                    y,
                    "Data/Hora"
            );

            escreverTexto(
                    contentStream,
                    fonteNegrito,
                    8,
                    400,
                    y,
                    "Responsável"
            );

            escreverTexto(
                    contentStream,
                    fonteNegrito,
                    8,
                    520,
                    y,
                    "Perfil"
            );

            escreverTexto(
                    contentStream,
                    fonteNegrito,
                    8,
                    585,
                    y,
                    "Observação"
            );

            y -= 20;

            if (
                    movimentacoes.isEmpty()
            ) {

                escreverTexto(
                        contentStream,
                        fonteNormal,
                        10,
                        40,
                        y,
                        "Nenhuma movimentação encontrada para os filtros informados."
                );

                return;
            }

            for (
                    MovimentacaoEstoque movimentacao :
                    movimentacoes
            ) {

                Produto produto =
                        movimentacao
                                .getProduto();

                escreverTexto(
                        contentStream,
                        fonteNormal,
                        8,
                        40,
                        y,
                        limitarTexto(
                                produto == null
                                        ? ""
                                        : produto.getNome(),
                                24
                        )
                );

                escreverTexto(
                        contentStream,
                        fonteNormal,
                        8,
                        190,
                        y,
                        movimentacao.getTipo()
                                == null
                                ? ""
                                : movimentacao
                                .getTipo()
                                .name()
                );

                escreverTexto(
                        contentStream,
                        fonteNormal,
                        8,
                        250,
                        y,
                        movimentacao.getQuantidade()
                                == null
                                ? "0"
                                : String.valueOf(
                                movimentacao
                                        .getQuantidade()
                        )
                );

                escreverTexto(
                        contentStream,
                        fonteNormal,
                        8,
                        295,
                        y,
                        formatarData(
                                movimentacao
                                        .getDataHora()
                        )
                );

                escreverTexto(
                        contentStream,
                        fonteNormal,
                        8,
                        400,
                        y,
                        limitarTexto(
                                movimentacao
                                        .getResponsavelNome(),
                                18
                        )
                );

                escreverTexto(
                        contentStream,
                        fonteNormal,
                        8,
                        520,
                        y,
                        movimentacao
                                .getResponsavelPerfil()
                                == null
                                ? ""
                                : movimentacao
                                .getResponsavelPerfil()
                                .name()
                );

                escreverTexto(
                        contentStream,
                        fonteNormal,
                        8,
                        585,
                        y,
                        limitarTexto(
                                movimentacao
                                        .getObservacao(),
                                35
                        )
                );

                y -= 18;
            }
        }
    }

    private void escreverTexto(
            PDPageContentStream contentStream,
            PDType1Font fonte,
            float tamanho,
            float x,
            float y,
            String texto
    ) throws IOException {

        contentStream.beginText();

        contentStream.setFont(
                fonte,
                tamanho
        );

        contentStream.newLineAtOffset(
                x,
                y
        );

        contentStream.showText(
                sanitizarTexto(
                        texto
                )
        );

        contentStream.endText();
    }

    private String sanitizarTexto(
            String texto
    ) {

        if (
                texto == null
        ) {
            return "";
        }

        return texto
                .replace(
                        "\n",
                        " "
                )
                .replace(
                        "\r",
                        " "
                )
                .replace(
                        "\t",
                        " "
                );
    }

    private String limitarTexto(
            String texto,
            int limite
    ) {

        if (
                texto == null
        ) {
            return "";
        }

        String valor =
                sanitizarTexto(
                        texto
                );

        if (
                valor.length() <= limite
        ) {
            return valor;
        }

        return valor.substring(
                0,
                Math.max(
                        0,
                        limite - 3
                )
        ) + "...";
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

    private String formatarDataFiltro(
            LocalDateTime dataHora
    ) {

        if (
                dataHora == null
        ) {
            return "Não informado";
        }

        return formatarData(
                dataHora
        );
    }

    private String formatarMoeda(
            BigDecimal valor
    ) {

        if (
                valor == null
        ) {
            return "R$ 0,00";
        }

        return "R$ " +
                valor
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        )
                        .toPlainString()
                        .replace(
                                ".",
                                ","
                        );
    }
}