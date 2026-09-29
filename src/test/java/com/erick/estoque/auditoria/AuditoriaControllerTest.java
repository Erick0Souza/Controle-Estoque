package com.erick.estoque.auditoria;

import com.erick.estoque.security.PerfilUsuario;
import com.erick.estoque.security.UserEntity;
import com.erick.estoque.security.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(
        username = "admin@teste.com",
        roles = "ADMIN"
)
class AuditoriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuditoriaRepository auditoriaRepository;

    @Autowired
    private AuditoriaService auditoriaService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;


    @BeforeEach
    void prepararBanco() {

        auditoriaRepository.deleteAll();

        userRepository.deleteAll();


        criarUsuario(
                "Administrador Teste",
                "admin@teste.com",
                PerfilUsuario.ADMIN
        );
    }


    @Test
    void deveRegistrarAuditoriaComResponsavelENormalizacao()
            throws Exception {

        auditoriaService.registrar(
                TipoAcaoAuditoria.PRODUTO_CRIADO,
                "  produto  ",
                100L,
                "  Produto criado no teste  "
        );


        mockMvc.perform(
                        get("/admin/auditorias")
                                .param(
                                        "acao",
                                        "PRODUTO_CRIADO"
                                )
                )

                .andExpect(
                        status().isOk()
                )

                .andExpect(
                        jsonPath(
                                "$.content",
                                hasSize(1)
                        )
                )

                .andExpect(
                        jsonPath("$.content[0].usuarioNome")
                                .value(
                                        "Administrador Teste"
                                )
                )

                .andExpect(
                        jsonPath("$.content[0].usuarioEmail")
                                .value(
                                        "admin@teste.com"
                                )
                )

                .andExpect(
                        jsonPath("$.content[0].usuarioPerfil")
                                .value(
                                        "ADMIN"
                                )
                )

                .andExpect(
                        jsonPath("$.content[0].acao")
                                .value(
                                        "PRODUTO_CRIADO"
                                )
                )

                .andExpect(
                        jsonPath("$.content[0].entidade")
                                .value(
                                        "PRODUTO"
                                )
                )

                .andExpect(
                        jsonPath("$.content[0].entidadeId")
                                .value(
                                        100
                                )
                )

                .andExpect(
                        jsonPath("$.content[0].descricao")
                                .value(
                                        "Produto criado no teste"
                                )
                );
    }


    @Test
    void deveFiltrarAuditoriaPorAcaoEEntidade()
            throws Exception {

        auditoriaService.registrar(
                TipoAcaoAuditoria.PRODUTO_CRIADO,
                "PRODUTO",
                1L,
                "Produto criado"
        );

        auditoriaService.registrar(
                TipoAcaoAuditoria.MOVIMENTACAO_ENTRADA,
                "MOVIMENTACAO",
                2L,
                "Entrada realizada"
        );


        mockMvc.perform(
                        get("/admin/auditorias")
                                .param(
                                        "acao",
                                        "MOVIMENTACAO_ENTRADA"
                                )
                                .param(
                                        "entidade",
                                        "MOVIMENTACAO"
                                )
                )

                .andExpect(
                        status().isOk()
                )

                .andExpect(
                        jsonPath(
                                "$.content",
                                hasSize(1)
                        )
                )

                .andExpect(
                        jsonPath("$.content[0].acao")
                                .value(
                                        "MOVIMENTACAO_ENTRADA"
                                )
                )

                .andExpect(
                        jsonPath("$.content[0].entidade")
                                .value(
                                        "MOVIMENTACAO"
                                )
                )

                .andExpect(
                        jsonPath("$.content[0].entidadeId")
                                .value(
                                        2
                                )
                );
    }


    @Test
    void devePaginarEOrdenarAuditorias()
            throws Exception {

        auditoriaService.registrar(
                TipoAcaoAuditoria.PRODUTO_CRIADO,
                "PRODUTO",
                1L,
                "Primeiro"
        );

        auditoriaService.registrar(
                TipoAcaoAuditoria.PRODUTO_EDITADO,
                "PRODUTO",
                2L,
                "Segundo"
        );

        auditoriaService.registrar(
                TipoAcaoAuditoria.PRODUTO_EXCLUIDO,
                "PRODUTO",
                3L,
                "Terceiro"
        );


        mockMvc.perform(
                        get("/admin/auditorias")
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
                                        "id"
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
                        jsonPath(
                                "$.content",
                                hasSize(2)
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
                        jsonPath("$.content[0].entidadeId")
                                .value(
                                        3
                                )
                )

                .andExpect(
                        jsonPath("$.content[1].entidadeId")
                                .value(
                                        2
                                )
                );
    }


    @Test
    void deveBuscarAuditoriaPorId()
            throws Exception {

        auditoriaService.registrar(
                TipoAcaoAuditoria.PRODUTO_EDITADO,
                "PRODUTO",
                50L,
                "Produto alterado"
        );


        String resposta =
                mockMvc.perform(
                                get("/admin/auditorias")
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


        long auditoriaId =
                json.get("content")
                        .get(0)
                        .get("id")
                        .asLong();


        mockMvc.perform(
                        get(
                                "/admin/auditorias/{id}",
                                auditoriaId
                        )
                )

                .andExpect(
                        status().isOk()
                )

                .andExpect(
                        jsonPath("$.id")
                                .value(
                                        auditoriaId
                                )
                )

                .andExpect(
                        jsonPath("$.acao")
                                .value(
                                        "PRODUTO_EDITADO"
                                )
                )

                .andExpect(
                        jsonPath("$.entidadeId")
                                .value(
                                        50
                                )
                );
    }


    @Test
    void deveRetornar404ParaAuditoriaInexistente()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/admin/auditorias/{id}",
                                999999L
                        )
                )

                .andExpect(
                        status().isNotFound()
                )

                .andExpect(
                        jsonPath("$.mensagem")
                                .value(
                                        "Registro de auditoria não encontrado"
                                )
                );
    }


    @Test
    void deveRejeitarPeriodoInvalido()
            throws Exception {

        mockMvc.perform(
                        get("/admin/auditorias")
                                .param(
                                        "dataInicio",
                                        "2026-10-10T10:00:00"
                                )
                                .param(
                                        "dataFim",
                                        "2026-10-01T10:00:00"
                                )
                )

                .andExpect(
                        status().isBadRequest()
                )

                .andExpect(
                        jsonPath("$.mensagem")
                                .value(
                                        "A data inicial não pode ser posterior à data final"
                                )
                );
    }


    @Test
    void deveRejeitarParametrosInvalidosDePaginacaoEOrdenacao()
            throws Exception {

        mockMvc.perform(
                        get("/admin/auditorias")
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


        mockMvc.perform(
                        get("/admin/auditorias")
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


        mockMvc.perform(
                        get("/admin/auditorias")
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


        mockMvc.perform(
                        get("/admin/auditorias")
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
    void operadorNaoPodeAcessarAuditoria()
            throws Exception {

        mockMvc.perform(
                        get("/admin/auditorias")
                )

                .andExpect(
                        status().isForbidden()
                );
    }


    private UserEntity criarUsuario(
            String nome,
            String email,
            PerfilUsuario perfil
    ) {

        UserEntity usuario =
                new UserEntity();

        usuario.setNomeUsuario(
                nome
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
}