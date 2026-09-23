package com.erick.estoque;

import com.erick.estoque.categoria.CategoriaRepository;
import com.erick.estoque.movimentacao.MovimentacaoRepository;
import com.erick.estoque.produto.ProdutoRepository;
import com.erick.estoque.security.UserEntity;
import com.erick.estoque.security.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private MovimentacaoRepository movimentacaoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;


    @BeforeEach
    void prepararBanco() {

        movimentacaoRepository.deleteAll();

        produtoRepository.deleteAll();

        categoriaRepository.deleteAll();

        userRepository.deleteAll();


        UserEntity usuario =
                new UserEntity();

        usuario.setEmail(
                "teste@estoque.com"
        );

        usuario.setSenha(
                passwordEncoder.encode(
                        "123456"
                )
        );


        userRepository.save(
                usuario
        );
    }


    @Test
    void deveBloquearProdutosSemToken()
            throws Exception {

        mockMvc.perform(
                        get("/produtos")
                )

                .andExpect(
                        status().isUnauthorized()
                );
    }


    @Test
    void deveFazerLoginComSucesso()
            throws Exception {

        String json =
                """
                {
                  "email": "teste@estoque.com",
                  "senha": "123456"
                }
                """;


        mockMvc.perform(
                        post("/auth/login")

                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )

                                .content(json)
                )

                .andExpect(
                        status().isOk()
                )

                .andExpect(
                        jsonPath("$.token")
                                .isNotEmpty()
                );
    }


    @Test
    void deveRejeitarLoginComSenhaErrada()
            throws Exception {

        String json =
                """
                {
                  "email": "teste@estoque.com",
                  "senha": "senhaErrada"
                }
                """;


        mockMvc.perform(
                        post("/auth/login")

                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )

                                .content(json)
                )

                .andExpect(
                        status().isUnauthorized()
                );
    }


    @Test
    void deveListarProdutosComTokenValido()
            throws Exception {

        String token =
                realizarLogin();


        mockMvc.perform(
                        get("/produtos")

                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )

                .andExpect(
                        status().isOk()
                )

                .andExpect(
                        content()
                                .contentTypeCompatibleWith(
                                        MediaType.APPLICATION_JSON
                                )
                );
    }


    @Test
    void deveRejeitarProdutoInvalido()
            throws Exception {

        String token =
                realizarLogin();


        String produtoInvalido =
                """
                {
                  "nome": "",
                  "preco": -10,
                  "quantidade": -5,
                  "categoriaId": null,
                  "descricao": "Teste"
                }
                """;


        mockMvc.perform(
                        post("/produtos")

                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )

                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )

                                .content(
                                        produtoInvalido
                                )
                )

                .andExpect(
                        status().isBadRequest()
                )

                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                );
    }


    private String realizarLogin()
            throws Exception {

        String loginJson =
                """
                {
                  "email": "teste@estoque.com",
                  "senha": "123456"
                }
                """;


        String resposta =
                mockMvc.perform(
                                post("/auth/login")

                                        .contentType(
                                                MediaType.APPLICATION_JSON
                                        )

                                        .content(
                                                loginJson
                                        )
                        )

                        .andExpect(
                                status().isOk()
                        )

                        .andReturn()

                        .getResponse()

                        .getContentAsString();


        JsonNode json =
                objectMapper.readTree(
                        resposta
                );


        return json
                .get("token")
                .asText();
    }
}