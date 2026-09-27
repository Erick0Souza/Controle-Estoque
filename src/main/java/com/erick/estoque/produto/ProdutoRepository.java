package com.erick.estoque.produto;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProdutoRepository
        extends JpaRepository<Produto, Long> {

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