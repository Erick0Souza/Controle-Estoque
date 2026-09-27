package com.erick.estoque.produto;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ProdutoRepository
        extends JpaRepository<Produto, Long>,
        JpaSpecificationExecutor<Produto> {

    List<Produto> findByNomeContainingIgnoreCase(
            String nome
    );

    Optional<Produto> findBySkuIgnoreCase(
            String sku
    );

    boolean existsBySkuIgnoreCase(
            String sku
    );

    boolean existsBySkuIgnoreCaseAndIdNot(
            String sku,
            Long id
    );
}