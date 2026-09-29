package com.erick.estoque.movimentacao;

import com.erick.estoque.auditoria.AuditoriaService;
import com.erick.estoque.produto.Produto;
import com.erick.estoque.produto.ProdutoRepository;
import com.erick.estoque.security.PerfilUsuario;
import com.erick.estoque.security.UserEntity;
import com.erick.estoque.security.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovimentacaoServiceTest {

    @Mock
    private MovimentacaoRepository movimentacaoRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private MovimentacaoService movimentacaoService;

    private Produto produto;

    private UserEntity usuario;

    private static final String EMAIL_USUARIO =
            "operador@teste.com";

    @BeforeEach
    void prepararTeste() {

        produto =
                new Produto();

        produto.setNome(
                "Teclado Mecânico"
        );

        produto.setPreco(
                new BigDecimal(
                        "199.90"
                )
        );

        produto.setQuantidade(
                10
        );

        produto.setDescricao(
                "Produto de teste"
        );


        usuario =
                new UserEntity();

        usuario.setNomeUsuario(
                "Operador Teste"
        );

        usuario.setEmail(
                EMAIL_USUARIO
        );

        usuario.setPerfil(
                PerfilUsuario.OPERADOR
        );


        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        EMAIL_USUARIO,
                        null,
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_OPERADOR"
                                )
                        )
                );

        SecurityContextHolder
                .getContext()
                .setAuthentication(
                        authentication
                );


        when(
                userRepository.findByEmail(
                        EMAIL_USUARIO
                )
        ).thenReturn(
                Optional.of(
                        usuario
                )
        );
    }

    @AfterEach
    void limparAutenticacao() {

        SecurityContextHolder
                .clearContext();
    }

    @Test
    void deveRegistrarEntradaNoEstoque() {

        when(
                produtoRepository.findById(
                        1L
                )
        ).thenReturn(
                Optional.of(
                        produto
                )
        );


        when(
                movimentacaoRepository.save(
                        any(
                                MovimentacaoEstoque.class
                        )
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(
                                0
                        )
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


        assertEquals(
                "Operador Teste",
                response.responsavelNome()
        );


        assertEquals(
                EMAIL_USUARIO,
                response.responsavelEmail()
        );


        assertEquals(
                PerfilUsuario.OPERADOR,
                response.responsavelPerfil()
        );


        verify(
                produtoRepository
        ).save(
                produto
        );


        verify(
                movimentacaoRepository
        ).save(
                any(
                        MovimentacaoEstoque.class
                )
        );


        verify(
                auditoriaService
        ).registrar(
                eq(
                        com.erick.estoque.auditoria.TipoAcaoAuditoria
                                .MOVIMENTACAO_ENTRADA
                ),
                eq(
                        "MOVIMENTACAO"
                ),
                any(),
                any()
        );
    }


    @Test
    void deveRegistrarSaidaDoEstoque() {

        when(
                produtoRepository.findById(
                        1L
                )
        ).thenReturn(
                Optional.of(
                        produto
                )
        );


        when(
                movimentacaoRepository.save(
                        any(
                                MovimentacaoEstoque.class
                        )
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(
                                0
                        )
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


        assertEquals(
                "Operador Teste",
                response.responsavelNome()
        );


        assertEquals(
                EMAIL_USUARIO,
                response.responsavelEmail()
        );


        assertEquals(
                PerfilUsuario.OPERADOR,
                response.responsavelPerfil()
        );


        verify(
                produtoRepository
        ).save(
                produto
        );


        verify(
                movimentacaoRepository
        ).save(
                any(
                        MovimentacaoEstoque.class
                )
        );


        verify(
                auditoriaService
        ).registrar(
                eq(
                        com.erick.estoque.auditoria.TipoAcaoAuditoria
                                .MOVIMENTACAO_SAIDA
                ),
                eq(
                        "MOVIMENTACAO"
                ),
                any(),
                any()
        );
    }


    @Test
    void naoDevePermitirSaidaMaiorQueEstoque() {

        when(
                produtoRepository.findById(
                        1L
                )
        ).thenReturn(
                Optional.of(
                        produto
                )
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
                                        .movimentar(
                                                request
                                        )
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
        ).save(
                any()
        );


        verify(
                movimentacaoRepository,
                never()
        ).save(
                any()
        );


        verify(
                auditoriaService,
                never()
        ).registrar(
                any(),
                any(),
                any(),
                any()
        );
    }
}