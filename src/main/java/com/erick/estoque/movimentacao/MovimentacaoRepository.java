package com.erick.estoque.movimentacao;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimentacaoRepository
        extends JpaRepository<MovimentacaoEstoque, Long> {

    List<MovimentacaoEstoque>
    findByProdutoIdOrderByDataHoraDesc(Long produtoId);

}