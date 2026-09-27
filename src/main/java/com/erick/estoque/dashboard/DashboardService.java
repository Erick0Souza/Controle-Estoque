package com.erick.estoque.dashboard;

import com.erick.estoque.auditoria.Auditoria;
import com.erick.estoque.auditoria.AuditoriaRepository;
import com.erick.estoque.categoria.CategoriaRepository;
import com.erick.estoque.movimentacao.MovimentacaoEstoque;
import com.erick.estoque.movimentacao.MovimentacaoRepository;
import com.erick.estoque.movimentacao.TipoMovimentacao;
import com.erick.estoque.produto.Produto;
import com.erick.estoque.produto.ProdutoRepository;
import com.erick.estoque.security.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class DashboardService {

    private final ProdutoRepository produtoRepository;
    private final CategoriaRepository categoriaRepository;
    private final UserRepository userRepository;
    private final MovimentacaoRepository movimentacaoRepository;
    private final AuditoriaRepository auditoriaRepository;

    public DashboardService(
            ProdutoRepository produtoRepository,
            CategoriaRepository categoriaRepository,
            UserRepository userRepository,
            MovimentacaoRepository movimentacaoRepository,
            AuditoriaRepository auditoriaRepository
    ) {
        this.produtoRepository =
                produtoRepository;

        this.categoriaRepository =
                categoriaRepository;

        this.userRepository =
                userRepository;

        this.movimentacaoRepository =
                movimentacaoRepository;

        this.auditoriaRepository =
                auditoriaRepository;
    }

    @Transactional(readOnly = true)
    public DashboardResponse obterDashboard() {

        List<Produto> produtos =
                produtoRepository
                        .findAll();

        List<MovimentacaoEstoque> movimentacoes =
                movimentacaoRepository
                        .findAll();

        long totalProdutos =
                produtos.size();

        long totalCategorias =
                categoriaRepository
                        .count();

        long totalUsuarios =
                userRepository
                        .count();

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

        long produtosEstoqueBaixo =
                produtos
                        .stream()
                        .filter(
                                this::estaComEstoqueBaixo
                        )
                        .count();

        long totalUnidadesEstoque =
                produtos
                        .stream()
                        .mapToLong(
                                produto ->
                                        produto.getQuantidade()
                                                == null
                                                ? 0
                                                : produto.getQuantidade()
                        )
                        .sum();

        BigDecimal valorTotalEstoque =
                calcularValorTotalEstoque(
                        produtos
                );

        List<DashboardResponse.AtividadeRecenteResponse>
                atividadesRecentes =
                buscarAtividadesRecentes();

        return new DashboardResponse(
                totalProdutos,
                totalCategorias,
                totalUsuarios,
                totalMovimentacoes,
                totalEntradas,
                totalSaidas,
                produtosEstoqueBaixo,
                totalUnidadesEstoque,
                valorTotalEstoque,
                atividadesRecentes
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

    private BigDecimal calcularValorTotalEstoque(
            List<Produto> produtos
    ) {

        return produtos
                .stream()
                .filter(
                        produto ->
                                produto.getPreco()
                                        != null
                )
                .filter(
                        produto ->
                                produto.getQuantidade()
                                        != null
                )
                .map(
                        produto ->
                                produto
                                        .getPreco()
                                        .multiply(
                                                BigDecimal.valueOf(
                                                        produto.getQuantidade()
                                                )
                                        )
                )
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }

    private List<DashboardResponse.AtividadeRecenteResponse>
    buscarAtividadesRecentes() {

        PageRequest pagina =
                PageRequest.of(
                        0,
                        5,
                        Sort.by(
                                Sort.Direction.DESC,
                                "dataHora"
                        )
                );

        List<Auditoria> auditorias =
                auditoriaRepository
                        .findAll(
                                pagina
                        )
                        .getContent();

        return auditorias
                .stream()
                .map(
                        this::toAtividadeResponse
                )
                .toList();
    }

    private DashboardResponse.AtividadeRecenteResponse
    toAtividadeResponse(
            Auditoria auditoria
    ) {

        return new DashboardResponse.AtividadeRecenteResponse(
                auditoria.getId(),
                auditoria.getDataHora(),
                auditoria.getUsuarioNome(),
                auditoria.getUsuarioEmail(),
                auditoria.getUsuarioPerfil(),
                auditoria.getAcao(),
                auditoria.getEntidade(),
                auditoria.getEntidadeId(),
                auditoria.getDescricao()
        );
    }
}