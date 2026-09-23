package com.erick.estoque.config;

import com.erick.estoque.categoria.Categoria;
import com.erick.estoque.categoria.CategoriaRepository;
import com.erick.estoque.produto.Produto;
import com.erick.estoque.produto.ProdutoRepository;
import com.erick.estoque.security.UserEntity;
import com.erick.estoque.security.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@Configuration
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


        if (!userRepository.existsByEmail(email)) {

            UserEntity usuario =
                    new UserEntity();

            usuario.setEmail(email);

            usuario.setSenha(
                    passwordEncoder.encode(
                            "123456"
                    )
            );

            userRepository.save(usuario);

            System.out.println(
                    "Usuário de demonstração criado."
            );
        }
    }


    private void criarDadosEstoque(
            CategoriaRepository categoriaRepository,
            ProdutoRepository produtoRepository
    ) {

        if (categoriaRepository.count() > 0) {
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


        if (produtoRepository.count() > 0) {
            return;
        }


        Produto teclado =
                new Produto();

        teclado.setNome(
                "Teclado Mecânico"
        );

        teclado.setPreco(
                new BigDecimal("199.90")
        );

        teclado.setQuantidade(10);

        teclado.setDescricao(
                "Teclado mecânico para computador"
        );

        teclado.setCategoria(
                perifericos
        );


        Produto mouse =
                new Produto();

        mouse.setNome(
                "Mouse sem fio"
        );

        mouse.setPreco(
                new BigDecimal("89.90")
        );

        mouse.setQuantidade(15);

        mouse.setDescricao(
                "Mouse sem fio para uso diário"
        );

        mouse.setCategoria(
                perifericos
        );


        Produto monitor =
                new Produto();

        monitor.setNome(
                "Monitor 24 polegadas"
        );

        monitor.setPreco(
                new BigDecimal("899.90")
        );

        monitor.setQuantidade(5);

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