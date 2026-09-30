package com.erick.estoque.config;

import com.erick.estoque.categoria.Categoria;
import com.erick.estoque.categoria.CategoriaRepository;
import com.erick.estoque.produto.Produto;
import com.erick.estoque.produto.ProdutoRepository;
import com.erick.estoque.security.PerfilUsuario;
import com.erick.estoque.security.UserEntity;
import com.erick.estoque.security.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@Configuration
@Profile("dev")
public class DataInitializer {

    @Bean
    CommandLineRunner carregarDadosIniciais(
            UserRepository userRepository,
            CategoriaRepository categoriaRepository,
            ProdutoRepository produtoRepository,
            PasswordEncoder passwordEncoder
    ) {

        return args -> {

            criarUsuarioDemonstracao(
                    userRepository,
                    passwordEncoder
            );

            criarDadosEstoque(
                    categoriaRepository,
                    produtoRepository
            );
        };
    }

    private void criarUsuarioDemonstracao(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {

        String email =
                "avaliador@teste.com";

        String nomeUsuario =
                "Avaliador";

        var usuarioExistente =
                userRepository.findByEmail(
                        email
                );

        if (
                usuarioExistente.isPresent()
        ) {

            UserEntity usuario =
                    usuarioExistente.get();

            boolean alterado =
                    false;

            if (
                    usuario.getNomeUsuario() == null
                            || usuario.getNomeUsuario().isBlank()
                            || usuario.getNomeUsuario().equals(
                            usuario.getEmail()
                    )
            ) {

                if (
                        !userRepository
                                .existsByNomeUsuarioIgnoreCase(
                                        nomeUsuario
                                )
                ) {

                    usuario.setNomeUsuario(
                            nomeUsuario
                    );

                    alterado = true;
                }
            }

            if (
                    usuario.getPerfil()
                            != PerfilUsuario.ADMIN
            ) {

                usuario.setPerfil(
                        PerfilUsuario.ADMIN
                );

                alterado = true;
            }

            if (alterado) {

                userRepository.save(
                        usuario
                );

                System.out.println(
                        "Usuário de demonstração atualizado."
                );
            }

            return;
        }

        UserEntity usuario =
                new UserEntity();

        usuario.setNomeUsuario(
                nomeUsuario
        );

        usuario.setEmail(
                email
        );

        usuario.setSenha(
                passwordEncoder.encode(
                        "123456"
                )
        );

        usuario.setPerfil(
                PerfilUsuario.ADMIN
        );

        userRepository.save(
                usuario
        );

        System.out.println(
                "Usuário de demonstração ADMIN criado."
        );
    }

    private void criarDadosEstoque(
            CategoriaRepository categoriaRepository,
            ProdutoRepository produtoRepository
    ) {

        if (
                categoriaRepository.count() > 0
        ) {
            return;
        }

        Categoria perifericos =
                new Categoria(
                        "Periféricos"
                );

        Categoria informatica =
                new Categoria(
                        "Informática"
                );

        Categoria acessorios =
                new Categoria(
                        "Acessórios"
                );

        perifericos =
                categoriaRepository.save(
                        perifericos
                );

        informatica =
                categoriaRepository.save(
                        informatica
                );

        acessorios =
                categoriaRepository.save(
                        acessorios
                );

        if (
                produtoRepository.count() > 0
        ) {
            return;
        }

        Produto teclado =
                new Produto();

        teclado.setSku(
                "TEC-001"
        );

        teclado.setNome(
                "Teclado Mecânico"
        );

        teclado.setPreco(
                new BigDecimal(
                        "199.90"
                )
        );

        teclado.setQuantidade(
                10
        );

        teclado.setEstoqueMinimo(
                5
        );

        teclado.setDescricao(
                "Teclado mecânico para computador"
        );

        teclado.setCategoria(
                perifericos
        );

        Produto mouse =
                new Produto();

        mouse.setSku(
                "MOU-001"
        );

        mouse.setNome(
                "Mouse sem fio"
        );

        mouse.setPreco(
                new BigDecimal(
                        "89.90"
                )
        );

        mouse.setQuantidade(
                15
        );

        mouse.setEstoqueMinimo(
                5
        );

        mouse.setDescricao(
                "Mouse sem fio para uso diário"
        );

        mouse.setCategoria(
                perifericos
        );

        Produto monitor =
                new Produto();

        monitor.setSku(
                "MON-001"
        );

        monitor.setNome(
                "Monitor 24 polegadas"
        );

        monitor.setPreco(
                new BigDecimal(
                        "899.90"
                )
        );

        monitor.setQuantidade(
                5
        );

        monitor.setEstoqueMinimo(
                2
        );

        monitor.setDescricao(
                "Monitor Full HD"
        );

        monitor.setCategoria(
                informatica
        );

        produtoRepository.save(
                teclado
        );

        produtoRepository.save(
                mouse
        );

        produtoRepository.save(
                monitor
        );
    }
}