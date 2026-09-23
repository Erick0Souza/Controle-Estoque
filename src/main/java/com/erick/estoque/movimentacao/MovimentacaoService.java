package com.erick.estoque.movimentacao;

import com.erick.estoque.produto.Produto;
import com.erick.estoque.produto.ProdutoRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MovimentacaoService {

    private final MovimentacaoRepository movimentacaoRepository;
    private final ProdutoRepository produtoRepository;

    public MovimentacaoService(
            MovimentacaoRepository movimentacaoRepository,
            ProdutoRepository produtoRepository
    ) {
        this.movimentacaoRepository = movimentacaoRepository;
        this.produtoRepository = produtoRepository;
    }

    @Transactional
    public MovimentacaoResponse movimentar(
            MovimentacaoRequest request
    ) {

        Produto produto =
                produtoRepository
                        .findById(request.produtoId())
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Produto não encontrado"
                                )
                        );


        int estoqueAtual =
                produto.getQuantidade();


        if (request.tipo()
                == TipoMovimentacao.ENTRADA) {

            estoqueAtual =
                    estoqueAtual
                            + request.quantidade();

        } else if (
                request.tipo()
                        == TipoMovimentacao.SAIDA
        ) {

            if (
                    estoqueAtual
                            < request.quantidade()
            ) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Estoque insuficiente"
                );
            }

            estoqueAtual =
                    estoqueAtual
                            - request.quantidade();
        }


        produto.setQuantidade(
                estoqueAtual
        );


        produtoRepository.save(
                produto
        );


        MovimentacaoEstoque movimentacao =
                new MovimentacaoEstoque();


        movimentacao.setProduto(
                produto
        );

        movimentacao.setTipo(
                request.tipo()
        );

        movimentacao.setQuantidade(
                request.quantidade()
        );

        movimentacao.setObservacao(
                request.observacao()
        );

        movimentacao.setDataHora(
                LocalDateTime.now()
        );


        MovimentacaoEstoque salva =
                movimentacaoRepository.save(
                        movimentacao
                );


        return toResponse(
                salva
        );
    }


    public List<MovimentacaoResponse> listar() {

        return movimentacaoRepository
                .findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }


    public List<MovimentacaoResponse> listarPorProduto(
            Long produtoId
    ) {

        if (
                !produtoRepository.existsById(
                        produtoId
                )
        ) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Produto não encontrado"
            );
        }


        return movimentacaoRepository
                .findByProdutoIdOrderByDataHoraDesc(
                        produtoId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }


    private MovimentacaoResponse toResponse(
            MovimentacaoEstoque movimentacao
    ) {

        Produto produto =
                movimentacao.getProduto();


        return new MovimentacaoResponse(

                movimentacao.getId(),

                produto.getId(),

                produto.getNome(),

                movimentacao.getTipo(),

                movimentacao.getQuantidade(),

                produto.getQuantidade(),

                movimentacao.getObservacao(),

                movimentacao.getDataHora()
        );
    }
}