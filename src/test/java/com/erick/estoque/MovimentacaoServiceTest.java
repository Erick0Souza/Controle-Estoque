package com.erick.estoque.movimentacao;

import com.erick.estoque.produto.Produto;
import com.erick.estoque.produto.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovimentacaoServiceTest {

    @Mock
    private MovimentacaoRepository movimentacaoRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    @InjectMocks
    private MovimentacaoService movimentacaoService;

    private Produto produto;

    @BeforeEach
    void prepararProduto() {

        produto = new Produto();

        produto.setNome("Teclado Mecânico");
        produto.setPreco(new BigDecimal("199.90"));
        produto.setQuantidade(10);
        produto.setDescricao("Produto de teste");
    }

    @Test
    void deveRegistrarEntradaNoEstoque() {

        when(
                produtoRepository.findById(1L)
        ).thenReturn(
                Optional.of(produto)
        );


        when(
                movimentacaoRepository.save(
                        any(MovimentacaoEstoque.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );


        MovimentacaoRequest request =
                new MovimentacaoRequest(
                        1L,
                        TipoMovimentacao.ENTRADA,
                        5,
                        "Entrada para teste"
                );


        MovimentacaoResponse response =
                movimentacaoService.movimentar(
                        request
                );


        assertEquals(
                15,
                produto.getQuantidade()
        );


        assertEquals(
                TipoMovimentacao.ENTRADA,
                response.tipo()
        );


        assertEquals(
                5,
                response.quantidade()
        );


        assertEquals(
                15,
                response.estoqueAtual()
        );


        verify(
                produtoRepository
        ).save(produto);


        verify(
                movimentacaoRepository
        ).save(
                any(MovimentacaoEstoque.class)
        );
    }


    @Test
    void deveRegistrarSaidaDoEstoque() {

        when(
                produtoRepository.findById(1L)
        ).thenReturn(
                Optional.of(produto)
        );


        when(
                movimentacaoRepository.save(
                        any(MovimentacaoEstoque.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );


        MovimentacaoRequest request =
                new MovimentacaoRequest(
                        1L,
                        TipoMovimentacao.SAIDA,
                        3,
                        "Venda"
                );


        MovimentacaoResponse response =
                movimentacaoService.movimentar(
                        request
                );


        assertEquals(
                7,
                produto.getQuantidade()
        );


        assertEquals(
                TipoMovimentacao.SAIDA,
                response.tipo()
        );


        assertEquals(
                3,
                response.quantidade()
        );


        assertEquals(
                7,
                response.estoqueAtual()
        );


        verify(
                produtoRepository
        ).save(produto);


        verify(
                movimentacaoRepository
        ).save(
                any(MovimentacaoEstoque.class)
        );
    }


    @Test
    void naoDevePermitirSaidaMaiorQueEstoque() {

        when(
                produtoRepository.findById(1L)
        ).thenReturn(
                Optional.of(produto)
        );


        MovimentacaoRequest request =
                new MovimentacaoRequest(
                        1L,
                        TipoMovimentacao.SAIDA,
                        50,
                        "Tentativa inválida"
                );


        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                movimentacaoService
                                        .movimentar(request)
                );


        assertEquals(
                "Estoque insuficiente",
                exception.getReason()
        );


        assertEquals(
                10,
                produto.getQuantidade()
        );


        verify(
                produtoRepository,
                never()
        ).save(any());


        verify(
                movimentacaoRepository,
                never()
        ).save(any());
    }
}