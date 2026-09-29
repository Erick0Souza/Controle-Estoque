package com.erick.estoque.security;

import com.erick.estoque.auditoria.AuditoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(
        username = "admin@teste.com",
        roles = "ADMIN"
)
class UsuarioAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditoriaRepository auditoriaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UserEntity adminLogado;


    @BeforeEach
    void prepararBanco() {

        auditoriaRepository.deleteAll();

        userRepository.deleteAll();


        adminLogado =
                criarUsuario(
                        "Administrador",
                        "admin@teste.com",
                        "Senha@123",
                        PerfilUsuario.ADMIN
                );
    }


    @Test
    void deveListarUsuariosFiltrandoPorPerfil()
            throws Exception {

        criarUsuario(
                "Operador Um",
                "operador@teste.com",
                "Senha@123",
                PerfilUsuario.OPERADOR
        );

        criarUsuario(
                "Consulta Um",
                "consulta@teste.com",
                "Senha@123",
                PerfilUsuario.CONSULTA
        );


        mockMvc.perform(
                        get("/admin/usuarios")
                                .param(
                                        "perfil",
                                        "OPERADOR"
                                )
                                .param(
                                        "page",
                                        "0"
                                )
                                .param(
                                        "size",
                                        "10"
                                )
                                .param(
                                        "sort",
                                        "email"
                                )
                                .param(
                                        "direction",
                                        "asc"
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
                        jsonPath("$.content[0].email")
                                .value(
                                        "operador@teste.com"
                                )
                )

                .andExpect(
                        jsonPath("$.content[0].perfil")
                                .value(
                                        "OPERADOR"
                                )
                );
    }


    @Test
    void deveBuscarUsuarioPorId()
            throws Exception {

        UserEntity usuario =
                criarUsuario(
                        "Usuario Busca",
                        "busca@teste.com",
                        "Senha@123",
                        PerfilUsuario.CONSULTA
                );


        mockMvc.perform(
                        get(
                                "/admin/usuarios/{id}",
                                usuario.getId()
                        )
                )

                .andExpect(
                        status().isOk()
                )

                .andExpect(
                        jsonPath("$.id")
                                .value(
                                        usuario.getId()
                                )
                )

                .andExpect(
                        jsonPath("$.nomeUsuario")
                                .value(
                                        "Usuario Busca"
                                )
                )

                .andExpect(
                        jsonPath("$.email")
                                .value(
                                        "busca@teste.com"
                                )
                )

                .andExpect(
                        jsonPath("$.perfil")
                                .value(
                                        "CONSULTA"
                                )
                );
    }


    @Test
    void deveRetornar404ParaUsuarioInexistente()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/admin/usuarios/{id}",
                                999999L
                        )
                )

                .andExpect(
                        status().isNotFound()
                )

                .andExpect(
                        jsonPath("$.mensagem")
                                .value(
                                        "Usuário não encontrado"
                                )
                );
    }


    @Test
    void deveAlterarPerfilERegistrarAuditoria()
            throws Exception {

        UserEntity usuario =
                criarUsuario(
                        "Usuario Perfil",
                        "perfil@teste.com",
                        "Senha@123",
                        PerfilUsuario.CONSULTA
                );


        String json =
                """
                {
                  "perfil": "OPERADOR"
                }
                """;


        mockMvc.perform(
                        put(
                                "/admin/usuarios/{id}/perfil",
                                usuario.getId()
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
                        jsonPath("$.perfil")
                                .value(
                                        "OPERADOR"
                                )
                );


        UserEntity atualizado =
                userRepository
                        .findById(
                                usuario.getId()
                        )
                        .orElseThrow();


        assertEquals(
                PerfilUsuario.OPERADOR,
                atualizado.getPerfil()
        );


        mockMvc.perform(
                        get("/admin/auditorias")
                                .param(
                                        "acao",
                                        "PERFIL_USUARIO_ALTERADO"
                                )
                                .param(
                                        "entidade",
                                        "USUARIO"
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
                        jsonPath("$.content[0].entidadeId")
                                .value(
                                        usuario.getId()
                                )
                )

                .andExpect(
                        jsonPath("$.content[0].usuarioEmail")
                                .value(
                                        "admin@teste.com"
                                )
                );
    }


    @Test
    void naoDevePermitirAdminRemoverProprioPerfilAdmin()
            throws Exception {

        String json =
                """
                {
                  "perfil": "OPERADOR"
                }
                """;


        mockMvc.perform(
                        put(
                                "/admin/usuarios/{id}/perfil",
                                adminLogado.getId()
                        )
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
                        jsonPath("$.mensagem")
                                .value(
                                        "Você não pode remover o perfil ADMIN da própria conta"
                                )
                );


        UserEntity atualizado =
                userRepository
                        .findById(
                                adminLogado.getId()
                        )
                        .orElseThrow();


        assertEquals(
                PerfilUsuario.ADMIN,
                atualizado.getPerfil()
        );
    }


    @Test
    void naoDevePermitirSistemaFicarSemAdministrador()
            throws Exception {

        userRepository.delete(
                adminLogado
        );


        UserEntity unicoAdmin =
                criarUsuario(
                        "Único Administrador",
                        "unico@teste.com",
                        "Senha@123",
                        PerfilUsuario.ADMIN
                );


        String json =
                """
                {
                  "perfil": "CONSULTA"
                }
                """;


        mockMvc.perform(
                        put(
                                "/admin/usuarios/{id}/perfil",
                                unicoAdmin.getId()
                        )
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
                        jsonPath("$.mensagem")
                                .value(
                                        "O sistema precisa possuir pelo menos um administrador"
                                )
                );
    }


    @Test
    void deveRedefinirSenhaComBCryptERegistrarAuditoria()
            throws Exception {

        UserEntity usuario =
                criarUsuario(
                        "Usuario Senha",
                        "senha@teste.com",
                        "SenhaAntiga@123",
                        PerfilUsuario.OPERADOR
                );


        String json =
                """
                {
                  "novaSenha": "NovaSenha@456",
                  "confirmarSenha": "NovaSenha@456"
                }
                """;


        mockMvc.perform(
                        put(
                                "/admin/usuarios/{id}/senha",
                                usuario.getId()
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
                        jsonPath("$.mensagem")
                                .value(
                                        "Senha redefinida com sucesso."
                                )
                );


        UserEntity atualizado =
                userRepository
                        .findById(
                                usuario.getId()
                        )
                        .orElseThrow();


        assertTrue(
                passwordEncoder.matches(
                        "NovaSenha@456",
                        atualizado.getSenha()
                )
        );


        mockMvc.perform(
                        get("/admin/auditorias")
                                .param(
                                        "acao",
                                        "SENHA_USUARIO_REDEFINIDA"
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
                        jsonPath("$.content[0].entidadeId")
                                .value(
                                        usuario.getId()
                                )
                );
    }


    @Test
    void deveRejeitarConfirmacaoDeSenhaDiferente()
            throws Exception {

        UserEntity usuario =
                criarUsuario(
                        "Usuario Senha",
                        "senhadiferente@teste.com",
                        "Senha@123",
                        PerfilUsuario.CONSULTA
                );


        String json =
                """
                {
                  "novaSenha": "NovaSenha@123",
                  "confirmarSenha": "OutraSenha@123"
                }
                """;


        mockMvc.perform(
                        put(
                                "/admin/usuarios/{id}/senha",
                                usuario.getId()
                        )
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
                        jsonPath("$.mensagem")
                                .value(
                                        "A nova senha e a confirmação não são iguais"
                                )
                );


        assertEquals(
                0,
                auditoriaRepository.count()
        );
    }


    @Test
    void deveRejeitarPaginaNegativa()
            throws Exception {

        mockMvc.perform(
                        get("/admin/usuarios")
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
            username = "operador@teste.com",
            roles = "OPERADOR"
    )
    void operadorNaoPodeAcessarAdministracaoDeUsuarios()
            throws Exception {

        mockMvc.perform(
                        get("/admin/usuarios")
                )

                .andExpect(
                        status().isForbidden()
                );
    }


    private UserEntity criarUsuario(
            String nome,
            String email,
            String senha,
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
                        senha
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