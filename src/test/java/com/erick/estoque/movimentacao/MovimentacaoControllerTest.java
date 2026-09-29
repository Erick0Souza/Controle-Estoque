package com.erick.estoque.movimentacao;

import com.erick.estoque.auditoria.AuditoriaService;
import com.erick.estoque.auditoria.TipoAcaoAuditoria;
import com.erick.estoque.categoria.Categoria;
import com.erick.estoque.categoria.CategoriaRepository;
import com.erick.estoque.produto.Produto;
import com.erick.estoque.produto.ProdutoRepository;
import com.erick.estoque.security.PerfilUsuario;
import com.erick.estoque.security.UserEntity;
import com.erick.estoque.security.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MovimentacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MovimentacaoRepository movimentacaoRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @MockBean
    private AuditoriaService auditoriaService;


    @BeforeEach
    void prepararBanco() {

        movimentacaoRepository.deleteAll();

        produtoRepository.deleteAll();

        categoriaRepository.deleteAll();

        userRepository.deleteAll();


        criarUsuario(
                "Operador Teste",
                "operador@teste.com",
                PerfilUsuario.OPERADOR
        );


        criarUsuario(
                "Consulta Teste",
                "consulta@teste.com",
                PerfilUsuario.CONSULTA
        );
    }


    @Test
    @WithMockUser(
            username = "operador@teste.com",
            roles = "OPERADOR"
    )
    void deveRegistrarEntradaEAtualizarEstoque()
            throws Exception {

        Categoria categoria =
                criarCategoria(
                        "Periféricos"
                );

        Produto produto =
                criarProduto(
                        "TEC-001",
                        "Teclado Mecânico",
                        10,
                        categoria
                );


        String json =
                """
                {
                  "produtoId": %d,
                  "tipo": "ENTRADA",
                  "quantidade": 5,
                  "observacao": "  Compra de fornecedor  "
                }
                """
                        .formatted(
                                produto.getId()
                        );


        mockMvc.perform(
                        post("/movimentacoes")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        json
                                )
                )

                .andExpect(
                        status().isCreated()
                )

                .andExpect(
                        jsonPath("$.id")
                                .isNumber()
                )

                .andExpect(
                        jsonPath("$.produtoId")
                                .value(
                                        produto.getId()
                                )
                )

                .andExpect(
                        jsonPath("$.produtoNome")
                                .value(
                                        "Teclado Mecânico"
                                )
                )

                .andExpect(
                        jsonPath("$.tipo")
                                .value(
                                        "ENTRADA"
                                )
                )

                .andExpect(
                        jsonPath("$.quantidade")
                                .value(
                                        5
                                )
                )

                .andExpect(
                        jsonPath("$.estoqueAtual")
                                .value(
                                        15
                                )
                )

                .andExpect(
                        jsonPath("$.responsavelNome")
                                .value(
                                        "Operador Teste"
                                )
                )

                .andExpect(
                        jsonPath("$.responsavelEmail")
                                .value(
                                        "operador@teste.com"
                                )
                )

                .andExpect(
                        jsonPath("$.responsavelPerfil")
                                .value(
                                        "OPERADOR"
                                )
                )

                .andExpect(
                        jsonPath("$.observacao")
                                .value(
                                        "Compra de fornecedor"
                                )
                )

                .andExpect(
                        jsonPath("$.dataHora")
                                .isNotEmpty()
                );


        Produto atualizado =
                produtoRepository
                        .findById(
                                produto.getId()
                        )
                        .orElseThrow();


        org.junit.jupiter.api.Assertions
                .assertEquals(
                        15,
                        atualizado.getQuantidade()
                );


        verify(
                auditoriaService
        ).registrar(
                eq(
                        TipoAcaoAuditoria.MOVIMENTACAO_ENTRADA
                ),
                eq(
                        "MOVIMENTACAO"
                ),
                anyLong(),
                contains(
                        "Teclado Mecânico"
                )
        );
    }


    @Test
    @WithMockUser(
            username = "operador@teste.com",
            roles = "OPERADOR"
    )
    void deveRegistrarSaidaEAtualizarEstoque()
            throws Exception {

        Categoria categoria =
                criarCategoria(
                        "Periféricos"
                );

        Produto produto =
                criarProduto(
                        "MOU-001",
                        "Mouse Gamer",
                        20,
                        categoria
                );


        String json =
                """
                {
                  "produtoId": %d,
                  "tipo": "SAIDA",
                  "quantidade": 7,
                  "observacao": "Venda"
                }
                """
                        .formatted(
                                produto.getId()
                        );


        mockMvc.perform(
                        post("/movimentacoes")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        json
                                )
                )

                .andExpect(
                        status().isCreated()
                )

                .andExpect(
                        jsonPath("$.tipo")
                                .value(
                                        "SAIDA"
                                )
                )

                .andExpect(
                        jsonPath("$.quantidade")
                                .value(
                                        7
                                )
                )

                .andExpect(
                        jsonPath("$.estoqueAtual")
                                .value(
                                        13
                                )
                );


        Produto atualizado =
                produtoRepository
                        .findById(
                                produto.getId()
                        )
                        .orElseThrow();


        org.junit.jupiter.api.Assertions
                .assertEquals(
                        13,
                        atualizado.getQuantidade()
                );


        verify(
                auditoriaService
        ).registrar(
                eq(
                        TipoAcaoAuditoria.MOVIMENTACAO_SAIDA
                ),
                eq(
                        "MOVIMENTACAO"
                ),
                anyLong(),
                contains(
                        "Mouse Gamer"
                )
        );
    }


    @Test
    @WithMockUser(
            username = "operador@teste.com",
            roles = "OPERADOR"
    )
    void naoDevePermitirSaidaMaiorQueEstoque()
            throws Exception {

        Categoria categoria =
                criarCategoria(
                        "Informática"
                );

        Produto produto =
                criarProduto(
                        "SSD-001",
                        "SSD 1TB",
                        5,
                        categoria
                );


        String json =
                """
                {
                  "produtoId": %d,
                  "tipo": "SAIDA",
                  "quantidade": 10,
                  "observacao": "Venda"
                }
                """
                        .formatted(
                                produto.getId()
                        );


        mockMvc.perform(
                        post("/movimentacoes")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        json
                                )
                )

                .andExpect(
                        status().isBadRequest()
                )

                .andExpect(
                        jsonPath("$.status")
                                .value(
                                        400
                                )
                )

                .andExpect(
                        jsonPath("$.mensagem")
                                .value(
                                        "Estoque insuficiente"
                                )
                );


        Produto atualizado =
                produtoRepository
                        .findById(
                                produto.getId()
                        )
                        .orElseThrow();


        org.junit.jupiter.api.Assertions
                .assertEquals(
                        5,
                        atualizado.getQuantidade()
                );


        org.junit.jupiter.api.Assertions
                .assertEquals(
                        0,
                        movimentacaoRepository.count()
                );


        verify(
                auditoriaService,
                never()
        ).registrar(
                eq(
                        TipoAcaoAuditoria.MOVIMENTACAO_SAIDA
                ),
                eq(
                        "MOVIMENTACAO"
                ),
                anyLong(),
                contains(
                        "SSD 1TB"
                )
        );
    }


    @Test
    @WithMockUser(
            username = "operador@teste.com",
            roles = "OPERADOR"
    )
    void deveRetornar404QuandoProdutoNaoExiste()
            throws Exception {

        String json =
                """
                {
                  "produtoId": 999999,
                  "tipo": "ENTRADA",
                  "quantidade": 5,
                  "observacao": "Teste"
                }
                """;


        mockMvc.perform(
                        post("/movimentacoes")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        json
                                )
                )

                .andExpect(
                        status().isNotFound()
                )

                .andExpect(
                        jsonPath("$.status")
                                .value(
                                        404
                                )
                )

                .andExpect(
                        jsonPath("$.mensagem")
                                .value(
                                        "Produto não encontrado"
                                )
                );
    }


    @Test
    @WithMockUser(
            username = "operador@teste.com",
            roles = "OPERADOR"
    )
    void deveRejeitarQuantidadeZero()
            throws Exception {

        Categoria categoria =
                criarCategoria(
                        "Informática"
                );

        Produto produto =
                criarProduto(
                        "MEM-001",
                        "Memória RAM",
                        10,
                        categoria
                );


        String json =
                """
                {
                  "produtoId": %d,
                  "tipo": "ENTRADA",
                  "quantidade": 0,
                  "observacao": "Teste"
                }
                """
                        .formatted(
                                produto.getId()
                        );


        mockMvc.perform(
                        post("/movimentacoes")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        json
                                )
                )

                .andExpect(
                        status().isBadRequest()
                )

                .andExpect(
                        jsonPath("$.status")
                                .value(
                                        400
                                )
                );
    }


    @Test
    @WithMockUser(
            username = "operador@teste.com",
            roles = "OPERADOR"
    )
    void deveRejeitarTipoAusente()
            throws Exception {

        Categoria categoria =
                criarCategoria(
                        "Informática"
                );

        Produto produto =
                criarProduto(
                        "PROC-001",
                        "Processador",
                        10,
                        categoria
                );


        String json =
                """
                {
                  "produtoId": %d,
                  "quantidade": 2,
                  "observacao": "Teste"
                }
                """
                        .formatted(
                                produto.getId()
                        );


        mockMvc.perform(
                        post("/movimentacoes")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        json
                                )
                )

                .andExpect(
                        status().isBadRequest()
                );
    }


    @Test
    @WithMockUser(
            username = "operador@teste.com",
            roles = "OPERADOR"
    )
    void deveConverterObservacaoEmBrancoParaNull()
            throws Exception {

        Categoria categoria =
                criarCategoria(
                        "Periféricos"
                );

        Produto produto =
                criarProduto(
                        "CAB-001",
                        "Cabo USB",
                        10,
                        categoria
                );


        String json =
                """
                {
                  "produtoId": %d,
                  "tipo": "ENTRADA",
                  "quantidade": 1,
                  "observacao": "     "
                }
                """
                        .formatted(
                                produto.getId()
                        );


        mockMvc.perform(
                        post("/movimentacoes")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        json
                                )
                )

                .andExpect(
                        status().isCreated()
                )

                .andExpect(
                        jsonPath("$.observacao")
                                .doesNotExist()
                );
    }


    @Test
    @WithMockUser(
            username = "consulta@teste.com",
            roles = "CONSULTA"
    )
    void consultaPodeListarMovimentacoes()
            throws Exception {

        Categoria categoria =
                criarCategoria(
                        "Teste"
                );

        Produto produto =
                criarProduto(
                        "PROD-001",
                        "Produto Teste",
                        10,
                        categoria
                );


        criarMovimentacaoDireta(
                produto,
                TipoMovimentacao.ENTRADA,
                3,
                LocalDateTime.now()
                        .minusHours(1)
        );


        mockMvc.perform(
                        get("/movimentacoes")
                )

                .andExpect(
                        status().isOk()
                )

                .andExpect(
                        jsonPath("$", hasSize(1))
                )

                .andExpect(
                        jsonPath("$[0].produtoId")
                                .value(
                                        produto.getId()
                                )
                )

                .andExpect(
                        jsonPath("$[0].tipo")
                                .value(
                                        "ENTRADA"
                                )
                );
    }


    @Test
    @WithMockUser(
            username = "consulta@teste.com",
            roles = "CONSULTA"
    )
    void deveListarMovimentacoesSomenteDoProdutoInformado()
            throws Exception {

        Categoria categoria =
                criarCategoria(
                        "Teste"
                );


        Produto primeiro =
                criarProduto(
                        "PROD-001",
                        "Primeiro Produto",
                        10,
                        categoria
                );

        Produto segundo =
                criarProduto(
                        "PROD-002",
                        "Segundo Produto",
                        20,
                        categoria
                );


        criarMovimentacaoDireta(
                primeiro,
                TipoMovimentacao.ENTRADA,
                2,
                LocalDateTime.now()
                        .minusHours(2)
        );

        criarMovimentacaoDireta(
                segundo,
                TipoMovimentacao.SAIDA,
                1,
                LocalDateTime.now()
                        .minusHours(1)
        );


        mockMvc.perform(
                        get(
                                "/movimentacoes/produto/{produtoId}",
                                primeiro.getId()
                        )
                )

                .andExpect(
                        status().isOk()
                )

                .andExpect(
                        jsonPath("$", hasSize(1))
                )

                .andExpect(
                        jsonPath("$[0].produtoId")
                                .value(
                                        primeiro.getId()
                                )
                )

                .andExpect(
                        jsonPath("$[0].produtoNome")
                                .value(
                                        "Primeiro Produto"
                                )
                );
    }


    @Test
    @WithMockUser(
            username = "consulta@teste.com",
            roles = "CONSULTA"
    )
    void deveOrdenarHistoricoDoProdutoDoMaisRecenteParaOMaisAntigo()
            throws Exception {

        Categoria categoria =
                criarCategoria(
                        "Teste"
                );

        Produto produto =
                criarProduto(
                        "HIST-001",
                        "Produto Histórico",
                        10,
                        categoria
                );


        criarMovimentacaoDireta(
                produto,
                TipoMovimentacao.ENTRADA,
                5,
                LocalDateTime.of(
                        2026,
                        1,
                        1,
                        10,
                        0
                )
        );

        criarMovimentacaoDireta(
                produto,
                TipoMovimentacao.SAIDA,
                2,
                LocalDateTime.of(
                        2026,
                        1,
                        2,
                        10,
                        0
                )
        );


        mockMvc.perform(
                        get(
                                "/movimentacoes/produto/{produtoId}",
                                produto.getId()
                        )
                )

                .andExpect(
                        status().isOk()
                )

                .andExpect(
                        jsonPath("$", hasSize(2))
                )

                .andExpect(
                        jsonPath("$[0].tipo")
                                .value(
                                        "SAIDA"
                                )
                )

                .andExpect(
                        jsonPath("$[1].tipo")
                                .value(
                                        "ENTRADA"
                                )
                );
    }


    @Test
    @WithMockUser(
            username = "consulta@teste.com",
            roles = "CONSULTA"
    )
    void deveRetornar404AoBuscarHistoricoDeProdutoInexistente()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/movimentacoes/produto/{produtoId}",
                                999999L
                        )
                )

                .andExpect(
                        status().isNotFound()
                )

                .andExpect(
                        jsonPath("$.mensagem")
                                .value(
                                        "Produto não encontrado"
                                )
                );
    }


    @Test
    @WithMockUser(
            username = "consulta@teste.com",
            roles = "CONSULTA"
    )
    void consultaNaoPodeRegistrarMovimentacao()
            throws Exception {

        Categoria categoria =
                criarCategoria(
                        "Teste"
                );

        Produto produto =
                criarProduto(
                        "SEC-001",
                        "Produto Segurança",
                        10,
                        categoria
                );


        String json =
                """
                {
                  "produtoId": %d,
                  "tipo": "ENTRADA",
                  "quantidade": 1
                }
                """
                        .formatted(
                                produto.getId()
                        );


        mockMvc.perform(
                        post("/movimentacoes")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        json
                                )
                )

                .andExpect(
                        status().isForbidden()
                );
    }


    @Test
    @WithMockUser(
            username = "usuario-inexistente@teste.com",
            roles = "OPERADOR"
    )
    void deveRejeitarMovimentacaoQuandoUsuarioAutenticadoNaoExisteNoBanco()
            throws Exception {

        Categoria categoria =
                criarCategoria(
                        "Teste"
                );

        Produto produto =
                criarProduto(
                        "USR-001",
                        "Produto Usuário",
                        10,
                        categoria
                );


        String json =
                """
                {
                  "produtoId": %d,
                  "tipo": "ENTRADA",
                  "quantidade": 2
                }
                """
                        .formatted(
                                produto.getId()
                        );


        mockMvc.perform(
                        post("/movimentacoes")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        json
                                )
                )

                .andExpect(
                        status().isUnauthorized()
                )

                .andExpect(
                        jsonPath("$.mensagem")
                                .value(
                                        "Usuário autenticado não encontrado"
                                )
                );
    }


    private Categoria criarCategoria(
            String nome
    ) {

        return categoriaRepository.save(
                new Categoria(
                        nome
                )
        );
    }


    private Produto criarProduto(
            String sku,
            String nome,
            Integer quantidade,
            Categoria categoria
    ) {

        Produto produto =
                new Produto();

        produto.setSku(
                sku
        );

        produto.setNome(
                nome
        );

        produto.setPreco(
                new BigDecimal(
                        "100.00"
                )
        );

        produto.setQuantidade(
                quantidade
        );

        produto.setEstoqueMinimo(
                2
        );

        produto.setCategoria(
                categoria
        );

        produto.setDescricao(
                "Produto usado nos testes"
        );


        return produtoRepository.save(
                produto
        );
    }


    private UserEntity criarUsuario(
            String nomeUsuario,
            String email,
            PerfilUsuario perfil
    ) {

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
                        "Senha@123"
                )
        );

        usuario.setPerfil(
                perfil
        );


        return userRepository.save(
                usuario
        );
    }


    private MovimentacaoEstoque criarMovimentacaoDireta(
            Produto produto,
            TipoMovimentacao tipo,
            Integer quantidade,
            LocalDateTime dataHora
    ) {

        MovimentacaoEstoque movimentacao =
                new MovimentacaoEstoque();

        movimentacao.setProduto(
                produto
        );

        movimentacao.setTipo(
                tipo
        );

        movimentacao.setQuantidade(
                quantidade
        );

        movimentacao.setDataHora(
                dataHora
        );

        movimentacao.setResponsavelNome(
                "Usuário Histórico"
        );

        movimentacao.setResponsavelEmail(
                "historico@teste.com"
        );

        movimentacao.setResponsavelPerfil(
                PerfilUsuario.OPERADOR
        );

        movimentacao.setObservacao(
                "Movimentação para teste"
        );


        return movimentacaoRepository.save(
                movimentacao
        );
    }
}