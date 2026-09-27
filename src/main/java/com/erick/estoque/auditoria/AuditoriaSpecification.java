package com.erick.estoque.auditoria;

import com.erick.estoque.security.PerfilUsuario;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AuditoriaSpecification {

    private AuditoriaSpecification() {
    }

    public static Specification<Auditoria> comFiltros(
            String usuario,
            PerfilUsuario perfil,
            TipoAcaoAuditoria acao,
            String entidade,
            LocalDateTime dataInicio,
            LocalDateTime dataFim
    ) {

        return (
                root,
                query,
                criteriaBuilder
        ) -> {

            List<Predicate> predicates =
                    new ArrayList<>();

            if (
                    usuario != null &&
                            !usuario.isBlank()
            ) {

                String termo =
                        "%"
                                + usuario
                                .trim()
                                .toLowerCase(
                                        Locale.ROOT
                                )
                                + "%";

                Predicate porNome =
                        criteriaBuilder.like(
                                criteriaBuilder.lower(
                                        root.get(
                                                "usuarioNome"
                                        )
                                ),
                                termo
                        );

                Predicate porEmail =
                        criteriaBuilder.like(
                                criteriaBuilder.lower(
                                        root.get(
                                                "usuarioEmail"
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
                                        "usuarioPerfil"
                                ),
                                perfil
                        )
                );
            }

            if (
                    acao != null
            ) {

                predicates.add(
                        criteriaBuilder.equal(
                                root.get(
                                        "acao"
                                ),
                                acao
                        )
                );
            }

            if (
                    entidade != null &&
                            !entidade.isBlank()
            ) {

                predicates.add(
                        criteriaBuilder.equal(
                                criteriaBuilder.upper(
                                        root.get(
                                                "entidade"
                                        )
                                ),
                                entidade
                                        .trim()
                                        .toUpperCase(
                                                Locale.ROOT
                                        )
                        )
                );
            }

            if (
                    dataInicio != null
            ) {

                predicates.add(
                        criteriaBuilder
                                .greaterThanOrEqualTo(
                                        root.get(
                                                "dataHora"
                                        ),
                                        dataInicio
                                )
                );
            }

            if (
                    dataFim != null
            ) {

                predicates.add(
                        criteriaBuilder
                                .lessThanOrEqualTo(
                                        root.get(
                                                "dataHora"
                                        ),
                                        dataFim
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