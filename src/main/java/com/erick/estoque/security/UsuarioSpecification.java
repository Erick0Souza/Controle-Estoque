package com.erick.estoque.security;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UsuarioSpecification {

    private UsuarioSpecification() {
    }

    public static Specification<UserEntity> comFiltros(
            String busca,
            PerfilUsuario perfil
    ) {

        return (
                root,
                query,
                criteriaBuilder
        ) -> {

            List<Predicate> predicates =
                    new ArrayList<>();

            if (
                    busca != null &&
                            !busca.isBlank()
            ) {

                String termo =
                        "%"
                                + busca
                                .trim()
                                .toLowerCase(
                                        Locale.ROOT
                                )
                                + "%";

                Predicate porNome =
                        criteriaBuilder.like(
                                criteriaBuilder.lower(
                                        root.get(
                                                "nomeUsuario"
                                        )
                                ),
                                termo
                        );

                Predicate porEmail =
                        criteriaBuilder.like(
                                criteriaBuilder.lower(
                                        root.get(
                                                "email"
                                        )
                                ),
                                termo
                        );

                predicates.add(
                        criteriaBuilder.or(
                                porNome,
                                porEmail
                        )
                );
            }

            if (
                    perfil != null
            ) {

                predicates.add(
                        criteriaBuilder.equal(
                                root.get(
                                        "perfil"
                                ),
                                perfil
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