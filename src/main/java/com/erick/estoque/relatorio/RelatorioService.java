package com.erick.estoque.relatorio;

import com.erick.estoque.movimentacao.MovimentacaoEstoque;
import com.erick.estoque.movimentacao.MovimentacaoRepository;
import com.erick.estoque.movimentacao.TipoMovimentacao;
import com.erick.estoque.produto.Produto;
import com.erick.estoque.produto.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class RelatorioService {

    private final ProdutoRepository produtoRepository;
    private final MovimentacaoRepository movimentacaoRepository;

    public RelatorioService(
            ProdutoRepository produtoRepository,
            MovimentacaoRepository movimentacaoRepository
    ) {
        this.produtoRepository =
                produtoRepository;

        this.movimentacaoRepository =
                movimentacaoRepository;
    }

    @Transactional(readOnly = true)
    public RelatorioEstoqueResponse gerarRelatorioEstoque(
            boolean somenteEstoqueBaixo
    ) {

        List<Produto> todosProdutos =
                produtoRepository
                        .findAll();

        List<Produto> produtosFiltrados =
                todosProdutos
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
                                        String.CASE_INSENSITIVE_ORDER
                                )
                        )
                        .toList();

        long totalProdutos =
                todosProdutos.size();

        long totalUnidades =
                todosProdutos
                        .stream()
                        .mapToLong(
                                produto ->
                                        produto.getQuantidade()
                                                == null
                                                ? 0
                                                : produto.getQuantidade()
                        )
                        .sum();

        long produtosEstoqueBaixo =
                todosProdutos
                        .stream()
                        .filter(
                                this::estaComEstoqueBaixo
                        )
                        .count();

        BigDecimal valorTotalEstoque =
                todosProdutos
                        .stream()
                        .map(
                                this::calcularValorProduto
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        List<RelatorioEstoqueResponse.ProdutoRelatorioResponse>
                produtosResponse =
                produtosFiltrados
                        .stream()
                        .map(
                                this::toProdutoResponse
                        )
                        .toList();

        return new RelatorioEstoqueResponse(
                totalProdutos,
                totalUnidades,
                produtosEstoqueBaixo,
                valorTotalEstoque,
                produtosResponse
        );
    }

    @Transactional(readOnly = true)
    public RelatorioMovimentacaoResponse gerarRelatorioMovimentacoes(
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
                                        MovimentacaoEstoque::getDataHora
                                ).reversed()
                        )
                        .toList();

        long totalMovimentacoes =
                movimentacoes.size();

        long totalEntradas =
                movimentacoes
                        .stream()
                        .filter(
                                movimentacao ->
                                        movimentacao.getTipo()
                                                == TipoMovimentacao.ENTRADA
                        )
                        .count();

        long totalSaidas =
                movimentacoes
                        .stream()
                        .filter(
                                movimentacao ->
                                        movimentacao.getTipo()
                                                == TipoMovimentacao.SAIDA
                        )
                        .count();

        long unidadesEntrada =
                movimentacoes
                        .stream()
                        .filter(
                                movimentacao ->
                                        movimentacao.getTipo()
                                                == TipoMovimentacao.ENTRADA
                        )
                        .mapToLong(
                                movimentacao ->
                                        movimentacao.getQuantidade()
                                                == null
                                                ? 0
                                                : movimentacao.getQuantidade()
                        )
                        .sum();

        long unidadesSaida =
                movimentacoes
                        .stream()
                        .filter(
                                movimentacao ->
                                        movimentacao.getTipo()
                                                == TipoMovimentacao.SAIDA
                        )
                        .mapToLong(
                                movimentacao ->
                                        movimentacao.getQuantidade()
                                                == null
                                                ? 0
                                                : movimentacao.getQuantidade()
                        )
                        .sum();

        List<RelatorioMovimentacaoResponse.MovimentacaoRelatorioItem>
                itens =
                movimentacoes
                        .stream()
                        .map(
                                this::toMovimentacaoResponse
                        )
                        .toList();

        return new RelatorioMovimentacaoResponse(
                totalMovimentacoes,
                totalEntradas,
                totalSaidas,
                unidadesEntrada,
                unidadesSaida,
                itens
        );
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

    private BigDecimal calcularValorProduto(
            Produto produto
    ) {

        if (
                produto.getPreco() == null ||
                        produto.getQuantidade() == null
        ) {
            return BigDecimal.ZERO;
        }

        return produto
                .getPreco()
                .multiply(
                        BigDecimal.valueOf(
                                produto.getQuantidade()
                        )
                );
    }

    private boolean dentroDoPeriodo(
            MovimentacaoEstoque movimentacao,
            LocalDateTime dataInicio,
            LocalDateTime dataFim
    ) {

        if (
                movimentacao.getDataHora()
                        == null
        ) {
            return false;
        }

        if (
                dataInicio != null &&
                        movimentacao
                                .getDataHora()
                                .isBefore(
                                        dataInicio
                                )
        ) {
            return false;
        }

        if (
                dataFim != null &&
                        movimentacao
                                .getDataHora()
                                .isAfter(
                                        dataFim
                                )
        ) {
            return false;
        }

        return true;
    }

    private RelatorioEstoqueResponse.ProdutoRelatorioResponse
    toProdutoResponse(
            Produto produto
    ) {

        String categoria =
                produto.getCategoria()
                        == null
                        ? "Sem categoria"
                        : produto
                        .getCategoria()
                        .getNome();

        return new RelatorioEstoqueResponse.ProdutoRelatorioResponse(
                produto.getId(),
                produto.getSku(),
                produto.getNome(),
                categoria,
                produto.getPreco(),
                produto.getQuantidade(),
                produto.getEstoqueMinimo(),
                estaComEstoqueBaixo(
                        produto
                ),
                calcularValorProduto(
                        produto
                )
        );
    }

    private RelatorioMovimentacaoResponse.MovimentacaoRelatorioItem
    toMovimentacaoResponse(
            MovimentacaoEstoque movimentacao
    ) {

        Produto produto =
                movimentacao
                        .getProduto();

        return new RelatorioMovimentacaoResponse.MovimentacaoRelatorioItem(
                movimentacao.getId(),
                produto.getId(),
                produto.getNome(),
                movimentacao.getTipo(),
                movimentacao.getQuantidade(),
                movimentacao.getDataHora(),
                movimentacao.getResponsavelNome(),
                movimentacao.getResponsavelEmail(),
                movimentacao.getResponsavelPerfil(),
                movimentacao.getObservacao()
        );
    }
}