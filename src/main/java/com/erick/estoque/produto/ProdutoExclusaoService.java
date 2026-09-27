package com.erick.estoque.produto;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProdutoExclusaoService {

    private final ProdutoRepository produtoRepository;
    private final ProdutoImagemService produtoImagemService;

    @PersistenceContext
    private EntityManager entityManager;

    public ProdutoExclusaoService(
            ProdutoRepository produtoRepository,
            ProdutoImagemService produtoImagemService
    ) {
        this.produtoRepository = produtoRepository;
        this.produtoImagemService = produtoImagemService;
    }

    @Transactional
    public void excluirProduto(
            Long produtoId
    ) {

        Produto produto =
                produtoRepository
                        .findById(produtoId)
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Produto não encontrado"
                                        )
                        );

        String imagemUrl =
                produto.getImagemUrl();

        entityManager
                .createQuery(
                        """
                        DELETE FROM MovimentacaoEstoque movimentacao
                        WHERE movimentacao.produto.id = :produtoId
                        """
                )
                .setParameter(
                        "produtoId",
                        produtoId
                )
                .executeUpdate();

        produtoRepository.delete(
                produto
        );

        produtoRepository.flush();

        if (
                imagemUrl != null &&
                        !imagemUrl.isBlank()
        ) {

            produtoImagemService
                    .excluirImagem(
                            imagemUrl
                    );
        }
    }
}