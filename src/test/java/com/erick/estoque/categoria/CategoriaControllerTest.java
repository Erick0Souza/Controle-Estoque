package com.erick.estoque.categoria;

import com.erick.estoque.movimentacao.MovimentacaoRepository;
import com.erick.estoque.produto.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CategoriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private MovimentacaoRepository movimentacaoRepository;


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
    void deveListarCategorias()
            throws Exception {

        categoriaRepository.save(
                new Categoria(
                        "Eletrônicos"
                )
        );

        categoriaRepository.save(
                new Categoria(
                        "Informática"
                )
        );


        mockMvc.perform(
                        get("/categorias")
                )

                .andExpect(
                        status().isOk()
                )

                .andExpect(
                        content()
                                .contentTypeCompatibleWith(
                                        MediaType.APPLICATION_JSON
                                )
                )

                .andExpect(
                        jsonPath("$", hasSize(2))
                );
    }


    @Test
    @WithMockUser(
            username = "admin@teste.com",
            roles = "ADMIN"
    )
    void deveCriarCategoria()
            throws Exception {

        String json =
                """
                {
                  "nome": "Periféricos"
                }
                """;


        mockMvc.perform(
                        post("/categorias")
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
                        jsonPath("$.nome")
                                .value(
                                        "Periféricos"
                                )
                );
    }


    @Test
    @WithMockUser(
            username = "admin@teste.com",
            roles = "ADMIN"
    )
    void deveBuscarCategoriaPorId()
            throws Exception {

        Categoria categoria =
                categoriaRepository.save(
                        new Categoria(
                                "Monitores"
                        )
                );


        mockMvc.perform(
                        get(
                                "/categorias/{id}",
                                categoria.getId()
                        )
                )

                .andExpect(
                        status().isOk()
                )

                .andExpect(
                        jsonPath("$.id")
                                .value(
                                        categoria.getId()
                                )
                )

                .andExpect(
                        jsonPath("$.nome")
                                .value(
                                        "Monitores"
                                )
                );
    }


    @Test
    @WithMockUser(
            username = "admin@teste.com",
            roles = "ADMIN"
    )
    void deveEditarCategoria()
            throws Exception {

        Categoria categoria =
                categoriaRepository.save(
                        new Categoria(
                                "Acessórios"
                        )
                );


        String json =
                """
                {
                  "nome": "Acessórios Gamer"
                }
                """;


        mockMvc.perform(
                        put(
                                "/categorias/{id}",
                                categoria.getId()
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
                        jsonPath("$.id")
                                .value(
                                        categoria.getId()
                                )
                )

                .andExpect(
                        jsonPath("$.nome")
                                .value(
                                        "Acessórios Gamer"
                                )
                );
    }


    @Test
    @WithMockUser(
            username = "admin@teste.com",
            roles = "ADMIN"
    )
    void deveExcluirCategoria()
            throws Exception {

        Categoria categoria =
                categoriaRepository.save(
                        new Categoria(
                                "Categoria Temporária"
                        )
                );


        mockMvc.perform(
                        delete(
                                "/categorias/{id}",
                                categoria.getId()
                        )
                )

                .andExpect(
                        status().isNoContent()
                );


        mockMvc.perform(
                        get(
                                "/categorias/{id}",
                                categoria.getId()
                        )
                )

                .andExpect(
                        status().isNotFound()
                );
    }


    @Test
    @WithMockUser(
            username = "admin@teste.com",
            roles = "ADMIN"
    )
    void deveRejeitarNomeVazio()
            throws Exception {

        String json =
                """
                {
                  "nome": ""
                }
                """;


        mockMvc.perform(
                        post("/categorias")
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
                                        "nome: O nome da categoria é obrigatório"
                                )
                );
    }


    @Test
    @WithMockUser(
            username = "admin@teste.com",
            roles = "ADMIN"
    )
    void deveRetornar404ParaCategoriaInexistente()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/categorias/{id}",
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
                                        "Categoria não encontrada"
                                )
                );
    }


    @Test
    @WithMockUser(
            username = "admin@teste.com",
            roles = "ADMIN"
    )
    void naoDevePermitirCategoriaDuplicada()
            throws Exception {

        categoriaRepository.saveAndFlush(
                new Categoria(
                        "Informática"
                )
        );


        String json =
                """
                {
                  "nome": "Informática"
                }
                """;


        mockMvc.perform(
                        post("/categorias")
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
                );
    }


    @Test
    @WithMockUser(
            username = "operador@teste.com",
            roles = "OPERADOR"
    )
    void operadorNaoPodeCriarCategoria()
            throws Exception {

        String json =
                """
                {
                  "nome": "Categoria Operador"
                }
                """;


        mockMvc.perform(
                        post("/categorias")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        json
                                )
                )

                .andExpect(
                        status().isForbidden()
                )

                .andExpect(
                        jsonPath("$.status")
                                .value(
                                        403
                                )
                );
    }


    @Test
    @WithMockUser(
            username = "consulta@teste.com",
            roles = "CONSULTA"
    )
    void consultaNaoPodeExcluirCategoria()
            throws Exception {

        Categoria categoria =
                categoriaRepository.save(
                        new Categoria(
                                "Categoria Protegida"
                        )
                );


        mockMvc.perform(
                        delete(
                                "/categorias/{id}",
                                categoria.getId()
                        )
                )

                .andExpect(
                        status().isForbidden()
                )

                .andExpect(
                        jsonPath("$.status")
                                .value(
                                        403
                                )
                );
    }
}