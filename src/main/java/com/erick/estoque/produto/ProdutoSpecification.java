package com.erick.estoque.produto;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProdutoSpecification {

    private ProdutoSpecification() {
    }

    public static Specification<Produto> comFiltros(
            String nome,
            String sku,
            Long categoriaId,
            BigDecimal precoMin,
            BigDecimal precoMax,
            Integer quantidadeMin,
            Integer quantidadeMax
    ) {

        return (
                root,
                query,
                criteriaBuilder
        ) -> {

            List<Predicate> predicates =
                    new ArrayList<>();

            if (
                    nome != null &&
                            !nome.isBlank()
            ) {

                String nomePesquisa =
                        "%" +
                                nome
                                        .trim()
                                        .toLowerCase(
                                                Locale.ROOT
                                        )
                                + "%";

                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(
                                        root.get("nome")
                                ),
                                nomePesquisa
                        )
                );
            }

            if (
                    sku != null &&
                            !sku.isBlank()
            ) {

                String skuPesquisa =
                        "%" +
                                sku
                                        .trim()
                                        .toLowerCase(
                                                Locale.ROOT
                                        )
                                + "%";

                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(
                                        root.get("sku")
                                ),
                                skuPesquisa
                        )
                );
            }

            if (
                    categoriaId != null
            ) {

                predicates.add(
                        criteriaBuilder.equal(
                                root
                                        .get("categoria")
                                        .get("id"),
                                categoriaId
                        )
                );
            }

            if (
                    precoMin != null
            ) {

                predicates.add(
                        criteriaBuilder.greaterThanOrEqualTo(
                                root.get("preco"),
                                precoMin
                        )
                );
            }

            if (
                    precoMax != null
            ) {

                predicates.add(
                        criteriaBuilder.lessThanOrEqualTo(
                                root.get("preco"),
                                precoMax
                        )
                );
            }

            if (
                    quantidadeMin != null
            ) {

                predicates.add(
                        criteriaBuilder.greaterThanOrEqualTo(
                                root.get("quantidade"),
                                quantidadeMin
                        )
                );
            }

            if (
                    quantidadeMax != null
            ) {

                predicates.add(
                        criteriaBuilder.lessThanOrEqualTo(
                                root.get("quantidade"),
                                quantidadeMax
                        )
                );
            }

            return criteriaBuilder.and(
                    predicates.toArray(
                            new Predicate[0]
                    )
            );
        };
    }
}