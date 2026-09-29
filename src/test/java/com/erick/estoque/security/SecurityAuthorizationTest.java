package com.erick.estoque.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;


    @Test
    void semTokenNaoPodeAcessarProdutos()
            throws Exception {

        mockMvc.perform(
                        get("/produtos")
                )

                .andExpect(
                        status().isUnauthorized()
                )

                .andExpect(
                        jsonPath("$.status")
                                .value(401)
                );
    }


    @Test
    @WithMockUser(
            username = "consulta@teste.com",
            roles = "CONSULTA"
    )
    void consultaPodeLerProdutos()
            throws Exception {

        mockMvc.perform(
                        get("/produtos")
                )

                .andExpect(
                        resultado ->
                                assertNotEquals(
                                        403,
                                        resultado
                                                .getResponse()
                                                .getStatus()
                                )
                );
    }


    @Test
    @WithMockUser(
            username = "consulta@teste.com",
            roles = "CONSULTA"
    )
    void consultaPodeLerCategorias()
            throws Exception {

        mockMvc.perform(
                        get("/categorias")
                )

                .andExpect(
                        resultado ->
                                assertNotEquals(
                                        403,
                                        resultado
                                                .getResponse()
                                                .getStatus()
                                )
                );
    }


    @Test
    @WithMockUser(
            username = "consulta@teste.com",
            roles = "CONSULTA"
    )
    void consultaNaoPodeCriarProduto()
            throws Exception {

        mockMvc.perform(
                        post("/produtos")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        "{}"
                                )
                )

                .andExpect(
                        status().isForbidden()
                )

                .andExpect(
                        jsonPath("$.status")
                                .value(403)
                );
    }


    @Test
    @WithMockUser(
            username = "consulta@teste.com",
            roles = "CONSULTA"
    )
    void consultaNaoPodeCriarMovimentacao()
            throws Exception {

        mockMvc.perform(
                        post("/movimentacoes")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        "{}"
                                )
                )

                .andExpect(
                        status().isForbidden()
                );
    }


    @Test
    @WithMockUser(
            username = "consulta@teste.com",
            roles = "CONSULTA"
    )
    void consultaNaoPodeCriarCategoria()
            throws Exception {

        mockMvc.perform(
                        post("/categorias")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        "{}"
                                )
                )

                .andExpect(
                        status().isForbidden()
                );
    }


    @Test
    @WithMockUser(
            username = "consulta@teste.com",
            roles = "CONSULTA"
    )
    void consultaNaoPodeAcessarAdministracao()
            throws Exception {

        mockMvc.perform(
                        get("/admin/usuarios")
                )

                .andExpect(
                        status().isForbidden()
                );
    }


    @Test
    @WithMockUser(
            username = "operador@teste.com",
            roles = "OPERADOR"
    )
    void operadorPodeLerProdutos()
            throws Exception {

        mockMvc.perform(
                        get("/produtos")
                )

                .andExpect(
                        resultado ->
                                assertNotEquals(
                                        403,
                                        resultado
                                                .getResponse()
                                                .getStatus()
                                )
                );
    }


    @Test
    @WithMockUser(
            username = "operador@teste.com",
            roles = "OPERADOR"
    )
    void operadorPodeCriarProduto()
            throws Exception {

        mockMvc.perform(
                        post("/produtos")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        "{}"
                                )
                )

                .andExpect(
                        resultado ->
                                assertNotEquals(
                                        403,
                                        resultado
                                                .getResponse()
                                                .getStatus()
                                )
                );
    }


    @Test
    @WithMockUser(
            username = "operador@teste.com",
            roles = "OPERADOR"
    )
    void operadorPodeCriarMovimentacao()
            throws Exception {

        mockMvc.perform(
                        post("/movimentacoes")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        "{}"
                                )
                )

                .andExpect(
                        resultado ->
                                assertNotEquals(
                                        403,
                                        resultado
                                                .getResponse()
                                                .getStatus()
                                )
                );
    }


    @Test
    @WithMockUser(
            username = "operador@teste.com",
            roles = "OPERADOR"
    )
    void operadorNaoPodeExcluirProduto()
            throws Exception {

        mockMvc.perform(
                        delete(
                                "/produtos/{id}",
                                999999L
                        )
                )

                .andExpect(
                        status().isForbidden()
                );
    }


    @Test
    @WithMockUser(
            username = "operador@teste.com",
            roles = "OPERADOR"
    )
    void operadorNaoPodeCriarCategoria()
            throws Exception {

        mockMvc.perform(
                        post("/categorias")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        "{}"
                                )
                )

                .andExpect(
                        status().isForbidden()
                );
    }


    @Test
    @WithMockUser(
            username = "operador@teste.com",
            roles = "OPERADOR"
    )
    void operadorNaoPodeAcessarAdministracao()
            throws Exception {

        mockMvc.perform(
                        get("/admin/usuarios")
                )

                .andExpect(
                        status().isForbidden()
                );
    }


    @Test
    @WithMockUser(
            username = "admin@teste.com",
            roles = "ADMIN"
    )
    void adminPodeAcessarAdministracao()
            throws Exception {

        mockMvc.perform(
                        get("/admin/usuarios")
                )

                .andExpect(
                        resultado ->
                                assertNotEquals(
                                        403,
                                        resultado
                                                .getResponse()
                                                .getStatus()
                                )
                );
    }


    @Test
    @WithMockUser(
            username = "admin@teste.com",
            roles = "ADMIN"
    )
    void adminPodeCriarProduto()
            throws Exception {

        mockMvc.perform(
                        post("/produtos")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        "{}"
                                )
                )

                .andExpect(
                        resultado ->
                                assertNotEquals(
                                        403,
                                        resultado
                                                .getResponse()
                                                .getStatus()
                                )
                );
    }


    @Test
    @WithMockUser(
            username = "admin@teste.com",
            roles = "ADMIN"
    )
    void adminPodeExcluirProduto()
            throws Exception {

        mockMvc.perform(
                        delete(
                                "/produtos/{id}",
                                999999L
                        )
                )

                .andExpect(
                        resultado ->
                                assertNotEquals(
                                        403,
                                        resultado
                                                .getResponse()
                                                .getStatus()
                                )
                );
    }


    @Test
    @WithMockUser(
            username = "admin@teste.com",
            roles = "ADMIN"
    )
    void adminPodeCriarMovimentacao()
            throws Exception {

        mockMvc.perform(
                        post("/movimentacoes")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        "{}"
                                )
                )

                .andExpect(
                        resultado ->
                                assertNotEquals(
                                        403,
                                        resultado
                                                .getResponse()
                                                .getStatus()
                                )
                );
    }


    @Test
    @WithMockUser(
            username = "admin@teste.com",
            roles = "ADMIN"
    )
    void adminPodeCriarCategoria()
            throws Exception {

        mockMvc.perform(
                        post("/categorias")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        "{}"
                                )
                )

                .andExpect(
                        resultado ->
                                assertNotEquals(
                                        403,
                                        resultado
                                                .getResponse()
                                                .getStatus()
                                )
                );
    }
}