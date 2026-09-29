package com.erick.estoque.produto;

import com.erick.estoque.auditoria.AuditoriaService;
import com.erick.estoque.auditoria.TipoAcaoAuditoria;
import com.erick.estoque.categoria.Categoria;
import com.erick.estoque.categoria.CategoriaRepository;
import com.erick.estoque.movimentacao.MovimentacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProdutoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private MovimentacaoRepository movimentacaoRepository;


    @MockBean
    private AuditoriaService auditoriaService;

    @MockBean
    private ProdutoExclusaoService produtoExclusaoService;

    @MockBean
    private ProdutoImagemService produtoImagemService;


    @BeforeEach
    void prepararBanco() {

        movimentacaoRepository.deleteAll();

        produtoRepository.deleteAll();

        categoriaRepository.deleteAll();
    }


    @Test
    @WithMockUser(
            username = "admin@teste.com",
            roles = "ADMIN"
    )
    void deveCriarProdutoComSucesso()
            throws Exception {

        Categoria categoria =
                criarCategoria(
                        "Periféricos"
                );


        String json =
                """
                {
                  "sku": "tec-001",
                  "nome": "  Teclado Mecânico  ",
                  "preco": 199.90,
                  "quantidade": 10,
                  "estoqueMinimo": 5,
                  "categoriaId": %d,
                  "descricao": "  Teclado para jogos  "
                }
                """
                        .formatted(
                                categoria.getId()
                        );


        mockMvc.perform(
                        post("/produtos")
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
                        jsonPath("$.sku")
                                .value(
                                        "TEC-001"
                                )
                )

                .andExpect(
                        jsonPath("$.nome")
                                .value(
                                        "Teclado Mecânico"
                                )
                )

                .andExpect(
                        jsonPath("$.preco")
                                .value(
                                        199.9
                                )
                )

                .andExpect(
                        jsonPath("$.quantidade")
                                .value(
                                        10
                                )
                )

                .andExpect(
                        jsonPath("$.estoqueMinimo")
                                .value(
                                        5
                                )
                )

                .andExpect(
                        jsonPath("$.estoqueBaixo")
                                .value(
                                        false
                                )
                )

                .andExpect(
                        jsonPath("$.categoriaId")
                                .value(
                                        categoria.getId()
                                )
                )

                .andExpect(
                        jsonPath("$.categoriaNome")
                                .value(
                                        "Periféricos"
                                )
                )

                .andExpect(
                        jsonPath("$.descricao")
                                .value(
                                        "Teclado para jogos"
                                )
                );


        verify(
                auditoriaService
        ).registrar(
                eq(
                        TipoAcaoAuditoria.PRODUTO_CRIADO
                ),
                eq(
                        "PRODUTO"
                ),
                anyLong(),
                contains(
                        "Teclado Mecânico"
                )
        );
    }


    @Test
    @WithMockUser(
            username = "admin@teste.com",
            roles = "ADMIN"
    )
    void naoDevePermitirSkuDuplicadoIgnorandoMaiusculasEMinusculas()
            throws Exception {

        Categoria categoria =
                criarCategoria(
                        "Informática"
                );

        criarProduto(
                "ABC-001",
                "Produto Original",
                new BigDecimal("100.00"),
                10,
                5,
                categoria
        );


        String json =
                """
                {
                  "sku": "abc-001",
                  "nome": "Outro Produto",
                  "preco": 200.00,
                  "quantidade": 20,
                  "estoqueMinimo": 5,
                  "categoriaId": %d,
                  "descricao": "Teste"
                }
                """
                        .formatted(
                                categoria.getId()
                        );


        mockMvc.perform(
                        post("/produtos")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        json
                                )
                )

                .andExpect(
                        status().isConflict()
                )

                .andExpect(
                        jsonPath("$.status")
                                .value(
                                        409
                                )
                )

                .andExpect(
                        jsonPath("$.mensagem")
                                .value(
                                        "Já existe um produto com este SKU"
                                )
                );
    }


    @Test
    @WithMockUser(
            username = "admin@teste.com",
            roles = "ADMIN"
    )
    void deveRejeitarCategoriaInexistente()
            throws Exception {

        String json =
                """
                {
                  "sku": "PROD-001",
                  "nome": "Produto Teste",
                  "preco": 100.00,
                  "quantidade": 5,
                  "estoqueMinimo": 2,
                  "categoriaId": 999999,
                  "descricao": "Teste"
                }
                """;


        mockMvc.perform(
                        post("/produtos")
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
                                        "Categoria inválida"
                                )
                );
    }


    @Test
    @WithMockUser(
            username = "admin@teste.com",
            roles = "ADMIN"
    )
    void deveRejeitarSkuVazio()
            throws Exception {

        Categoria categoria =
                criarCategoria(
                        "Teste"
                );


        String json =
                """
                {
                  "sku": "",
                  "nome": "Produto Teste",
                  "preco": 100.00,
                  "quantidade": 5,
                  "estoqueMinimo": 2,
                  "categoriaId": %d
                }
                """
                        .formatted(
                                categoria.getId()
                        );


        mockMvc.perform(
                        post("/produtos")
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
            username = "admin@teste.com",
            roles = "ADMIN"
    )
    void deveRejeitarSkuComCaracteresInvalidos()
            throws Exception {

        Categoria categoria =
                criarCategoria(
                        "Teste"
                );


        String json =
                """
                {
                  "sku": "ABC 001@",
                  "nome": "Produto Teste",
                  "preco": 100.00,
                  "quantidade": 5,
                  "estoqueMinimo": 2,
                  "categoriaId": %d
                }
                """
                        .formatted(
                                categoria.getId()
                        );


        mockMvc.perform(
                        post("/produtos")
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
            username = "admin@teste.com",
            roles = "ADMIN"
    )
    void deveRejeitarPrecoNegativo()
            throws Exception {

        Categoria categoria =
                criarCategoria(
                        "Teste"
                );


        String json =
                """
                {
                  "sku": "PROD-001",
                  "nome": "Produto Teste",
                  "preco": -1.00,
                  "quantidade": 5,
                  "estoqueMinimo": 2,
                  "categoriaId": %d
                }
                """
                        .formatted(
                                categoria.getId()
                        );


        mockMvc.perform(
                        post("/produtos")
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
            username = "admin@teste.com",
            roles = "ADMIN"
    )
    void deveRejeitarQuantidadeNegativa()
            throws Exception {

        Categoria categoria =
                criarCategoria(
                        "Teste"
                );


        String json =
                """
                {
                  "sku": "PROD-001",
                  "nome": "Produto Teste",
                  "preco": 100.00,
                  "quantidade": -1,
                  "estoqueMinimo": 2,
                  "categoriaId": %d
                }
                """
                        .formatted(
                                categoria.getId()
                        );


        mockMvc.perform(
                        post("/produtos")
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
            username = "admin@teste.com",
            roles = "ADMIN"
    )
    void deveRejeitarEstoqueMinimoNegativo()
            throws Exception {

        Categoria categoria =
                criarCategoria(
                        "Teste"
                );


        String json =
                """
                {
                  "sku": "PROD-001",
                  "nome": "Produto Teste",
                  "preco": 100.00,
                  "quantidade": 10,
                  "estoqueMinimo": -1,
                  "categoriaId": %d
                }
                """
                        .formatted(
                                categoria.getId()
                        );


        mockMvc.perform(
                        post("/produtos")
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
            username = "consulta@teste.com",
            roles = "CONSULTA"
    )
    void deveBuscarProdutoPorId()
            throws Exception {

        Categoria categoria =
                criarCategoria(
                        "Periféricos"
                );

        Produto produto =
                criarProduto(
                        "MOU-001",
                        "Mouse Gamer",
                        new BigDecimal("149.90"),
                        20,
                        5,
                        categoria
                );


        mockMvc.perform(
                        get(
                                "/produtos/{id}",
                                produto.getId()
                        )
                )

                .andExpect(
                        status().isOk()
                )

                .andExpect(
                        jsonPath("$.id")
                                .value(
                                        produto.getId()
                                )
                )

                .andExpect(
                        jsonPath("$.sku")
                                .value(
                                        "MOU-001"
                                )
                )

                .andExpect(
                        jsonPath("$.nome")
                                .value(
                                        "Mouse Gamer"
                                )
                );
    }


    @Test
    @WithMockUser(
            username = "consulta@teste.com",
            roles = "CONSULTA"
    )
    void deveRetornar404ParaProdutoInexistente()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/produtos/{id}",
                                999999L
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
            username = "consulta@teste.com",
            roles = "CONSULTA"
    )
    void deveIndicarEstoqueBaixoQuandoQuantidadeForIgualAoMinimo()
            throws Exception {

        Categoria categoria =
                criarCategoria(
                        "Eletrônicos"
                );

        Produto produto =
                criarProduto(
                        "CAB-001",
                        "Cabo USB",
                        new BigDecimal("29.90"),
                        5,
                        5,
                        categoria
                );


        mockMvc.perform(
                        get(
                                "/produtos/{id}",
                                produto.getId()
                        )
                )

                .andExpect(
                        status().isOk()
                )

                .andExpect(
                        jsonPath("$.quantidade")
                                .value(
                                        5
                                )
                )

                .andExpect(
                        jsonPath("$.estoqueMinimo")
                                .value(
                                        5
                                )
                )

                .andExpect(
                        jsonPath("$.estoqueBaixo")
                                .value(
                                        true
                                )
                );
    }


    @Test
    @WithMockUser(
            username = "consulta@teste.com",
            roles = "CONSULTA"
    )
    void deveFiltrarProdutosPorNome()
            throws Exception {

        Categoria categoria =
                criarCategoria(
                        "Periféricos"
                );


        criarProduto(
                "TEC-001",
                "Teclado Mecânico",
                new BigDecimal("300.00"),
                10,
                3,
                categoria
        );

        criarProduto(
                "MOU-001",
                "Mouse Gamer",
                new BigDecimal("150.00"),
                10,
                3,
                categoria
        );


        mockMvc.perform(
                        get("/produtos")
                                .param(
                                        "nome",
                                        "teclado"
                                )
                )

                .andExpect(
                        status().isOk()
                )

                .andExpect(
                        jsonPath("$.content", hasSize(1))
                )

                .andExpect(
                        jsonPath("$.totalElements")
                                .value(
                                        1
                                )
                )

                .andExpect(
                        jsonPath("$.content[0].sku")
                                .value(
                                        "TEC-001"
                                )
                );
    }


    @Test
    @WithMockUser(
            username = "consulta@teste.com",
            roles = "CONSULTA"
    )
    void deveFiltrarProdutosPorCategoria()
            throws Exception {

        Categoria perifericos =
                criarCategoria(
                        "Periféricos"
                );

        Categoria monitores =
                criarCategoria(
                        "Monitores"
                );


        criarProduto(
                "TEC-001",
                "Teclado",
                new BigDecimal("300.00"),
                10,
                3,
                perifericos
        );

        criarProduto(
                "MON-001",
                "Monitor",
                new BigDecimal("1200.00"),
                5,
                2,
                monitores
        );


        mockMvc.perform(
                        get("/produtos")
                                .param(
                                        "categoriaId",
                                        monitores
                                                .getId()
                                                .toString()
                                )
                )

                .andExpect(
                        status().isOk()
                )

                .andExpect(
                        jsonPath("$.content", hasSize(1))
                )

                .andExpect(
                        jsonPath("$.content[0].sku")
                                .value(
                                        "MON-001"
                                )
                );
    }


    @Test
    @WithMockUser(
            username = "consulta@teste.com",
            roles = "CONSULTA"
    )
    void deveFiltrarPorPrecoEQuantidade()
            throws Exception {

        Categoria categoria =
                criarCategoria(
                        "Teste"
                );


        criarProduto(
                "PROD-001",
                "Produto Barato",
                new BigDecimal("50.00"),
                3,
                1,
                categoria
        );

        criarProduto(
                "PROD-002",
                "Produto Médio",
                new BigDecimal("150.00"),
                10,
                2,
                categoria
        );

        criarProduto(
                "PROD-003",
                "Produto Caro",
                new BigDecimal("500.00"),
                30,
                5,
                categoria
        );


        mockMvc.perform(
                        get("/produtos")
                                .param(
                                        "precoMin",
                                        "100"
                                )
                                .param(
                                        "precoMax",
                                        "200"
                                )
                                .param(
                                        "quantidadeMin",
                                        "5"
                                )
                                .param(
                                        "quantidadeMax",
                                        "20"
                                )
                )

                .andExpect(
                        status().isOk()
                )

                .andExpect(
                        jsonPath("$.content", hasSize(1))
                )

                .andExpect(
                        jsonPath("$.content[0].sku")
                                .value(
                                        "PROD-002"
                                )
                );
    }


    @Test
    @WithMockUser(
            username = "consulta@teste.com",
            roles = "CONSULTA"
    )
    void devePaginarEOrdenarProdutos()
            throws Exception {

        Categoria categoria =
                criarCategoria(
                        "Periféricos"
                );


        criarProduto(
                "PROD-003",
                "Mouse",
                new BigDecimal("150.00"),
                5,
                1,
                categoria
        );

        criarProduto(
                "PROD-001",
                "Teclado",
                new BigDecimal("300.00"),
                5,
                1,
                categoria
        );

        criarProduto(
                "PROD-002",
                "Monitor",
                new BigDecimal("1200.00"),
                5,
                1,
                categoria
        );


        mockMvc.perform(
                        get("/produtos")
                                .param(
                                        "page",
                                        "0"
                                )
                                .param(
                                        "size",
                                        "2"
                                )
                                .param(
                                        "sort",
                                        "preco"
                                )
                                .param(
                                        "direction",
                                        "desc"
                                )
                )

                .andExpect(
                        status().isOk()
                )

                .andExpect(
                        jsonPath("$.content", hasSize(2))
                )

                .andExpect(
                        jsonPath("$.content[0].sku")
                                .value(
                                        "PROD-002"
                                )
                )

                .andExpect(
                        jsonPath("$.content[1].sku")
                                .value(
                                        "PROD-001"
                                )
                )

                .andExpect(
                        jsonPath("$.totalElements")
                                .value(
                                        3
                                )
                )

                .andExpect(
                        jsonPath("$.totalPages")
                                .value(
                                        2
                                )
                )

                .andExpect(
                        jsonPath("$.number")
                                .value(
                                        0
                                )
                )

                .andExpect(
                        jsonPath("$.size")
                                .value(
                                        2
                                )
                );
    }


    @Test
    @WithMockUser(
            username = "consulta@teste.com",
            roles = "CONSULTA"
    )
    void deveRejeitarPaginaNegativa()
            throws Exception {

        mockMvc.perform(
                        get("/produtos")
                                .param(
                                        "page",
                                        "-1"
                                )
                )

                .andExpect(
                        status().isBadRequest()
                )

                .andExpect(
                        jsonPath("$.mensagem")
                                .value(
                                        "A página não pode ser negativa"
                                )
                );
    }


    @Test
    @WithMockUser(
            username = "consulta@teste.com",
            roles = "CONSULTA"
    )
    void deveRejeitarTamanhoDePaginaMaiorQueCem()
            throws Exception {

        mockMvc.perform(
                        get("/produtos")
                                .param(
                                        "size",
                                        "101"
                                )
                )

                .andExpect(
                        status().isBadRequest()
                )

                .andExpect(
                        jsonPath("$.mensagem")
                                .value(
                                        "O tamanho da página deve estar entre 1 e 100"
                                )
                );
    }


    @Test
    @WithMockUser(
            username = "consulta@teste.com",
            roles = "CONSULTA"
    )
    void deveRejeitarCampoDeOrdenacaoInvalido()
            throws Exception {

        mockMvc.perform(
                        get("/produtos")
                                .param(
                                        "sort",
                                        "campoInexistente"
                                )
                )

                .andExpect(
                        status().isBadRequest()
                )

                .andExpect(
                        jsonPath("$.mensagem")
                                .value(
                                        "Campo de ordenação inválido"
                                )
                );
    }


    @Test
    @WithMockUser(
            username = "consulta@teste.com",
            roles = "CONSULTA"
    )
    void deveRejeitarDirecaoDeOrdenacaoInvalida()
            throws Exception {

        mockMvc.perform(
                        get("/produtos")
                                .param(
                                        "direction",
                                        "qualquer"
                                )
                )

                .andExpect(
                        status().isBadRequest()
                )

                .andExpect(
                        jsonPath("$.mensagem")
                                .value(
                                        "A direção deve ser asc ou desc"
                                )
                );
    }


    @Test
    @WithMockUser(
            username = "operador@teste.com",
            roles = "OPERADOR"
    )
    void deveEditarProdutoComSucesso()
            throws Exception {

        Categoria categoria =
                criarCategoria(
                        "Periféricos"
                );


        Produto produto =
                criarProduto(
                        "PROD-001",
                        "Produto Antigo",
                        new BigDecimal("100.00"),
                        10,
                        2,
                        categoria
                );


        String json =
                """
                {
                  "sku": "novo-001",
                  "nome": "Produto Atualizado",
                  "preco": 250.00,
                  "quantidade": 20,
                  "estoqueMinimo": 7,
                  "categoriaId": %d,
                  "descricao": "Produto editado"
                }
                """
                        .formatted(
                                categoria.getId()
                        );


        mockMvc.perform(
                        put(
                                "/produtos/{id}",
                                produto.getId()
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        json
                                )
                )

                .andExpect(
                        status().isOk()
                )

                .andExpect(
                        jsonPath("$.sku")
                                .value(
                                        "NOVO-001"
                                )
                )

                .andExpect(
                        jsonPath("$.nome")
                                .value(
                                        "Produto Atualizado"
                                )
                )

                .andExpect(
                        jsonPath("$.quantidade")
                                .value(
                                        20
                                )
                )

                .andExpect(
                        jsonPath("$.estoqueMinimo")
                                .value(
                                        7
                                )
                );


        verify(
                auditoriaService
        ).registrar(
                eq(
                        TipoAcaoAuditoria.PRODUTO_EDITADO
                ),
                eq(
                        "PRODUTO"
                ),
                eq(
                        produto.getId()
                ),
                contains(
                        "Produto Atualizado"
                )
        );
    }


    @Test
    @WithMockUser(
            username = "operador@teste.com",
            roles = "OPERADOR"
    )
    void naoDevePermitirEditarParaSkuDeOutroProduto()
            throws Exception {

        Categoria categoria =
                criarCategoria(
                        "Teste"
                );


        Produto primeiro =
                criarProduto(
                        "PROD-001",
                        "Primeiro Produto",
                        new BigDecimal("100.00"),
                        10,
                        2,
                        categoria
                );

        criarProduto(
                "PROD-002",
                "Segundo Produto",
                new BigDecimal("200.00"),
                10,
                2,
                categoria
        );


        String json =
                """
                {
                  "sku": "prod-002",
                  "nome": "Primeiro Alterado",
                  "preco": 150.00,
                  "quantidade": 10,
                  "estoqueMinimo": 2,
                  "categoriaId": %d
                }
                """
                        .formatted(
                                categoria.getId()
                        );


        mockMvc.perform(
                        put(
                                "/produtos/{id}",
                                primeiro.getId()
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        json
                                )
                )

                .andExpect(
                        status().isConflict()
                )

                .andExpect(
                        jsonPath("$.mensagem")
                                .value(
                                        "Já existe outro produto com este SKU"
                                )
                );
    }


    @Test
    @WithMockUser(
            username = "admin@teste.com",
            roles = "ADMIN"
    )
    void deveDelegarExclusaoDoProdutoParaService()
            throws Exception {

        Categoria categoria =
                criarCategoria(
                        "Teste Exclusão"
                );


        Produto produto =
                criarProduto(
                        "DEL-001",
                        "Produto para excluir",
                        new BigDecimal("50.00"),
                        1,
                        0,
                        categoria
                );


        mockMvc.perform(
                        delete(
                                "/produtos/{id}",
                                produto.getId()
                        )
                )

                .andExpect(
                        status().isNoContent()
                );


        verify(
                produtoExclusaoService
        ).excluirProduto(
                produto.getId()
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
            BigDecimal preco,
            Integer quantidade,
            Integer estoqueMinimo,
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
                preco
        );

        produto.setQuantidade(
                quantidade
        );

        produto.setEstoqueMinimo(
                estoqueMinimo
        );

        produto.setCategoria(
                categoria
        );

        produto.setDescricao(
                "Produto utilizado em teste"
        );


        return produtoRepository.save(
                produto
        );
    }
}