package com.erick.estoque.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void prepararBanco() {

        userRepository.deleteAll();
    }

    @Test
    void deveFazerLoginComSucesso()
            throws Exception {

        criarUsuario(
                "Usuario Login",
                "login@teste.com",
                "Senha@123",
                PerfilUsuario.CONSULTA
        );

        String json =
                """
                {
                  "email": "login@teste.com",
                  "senha": "Senha@123"
                }
                """;

        mockMvc.perform(
                        post("/auth/login")
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
                        jsonPath("$.token")
                                .isNotEmpty()
                )

                .andExpect(
                        jsonPath("$.nomeUsuario")
                                .value(
                                        "Usuario Login"
                                )
                )

                .andExpect(
                        jsonPath("$.email")
                                .value(
                                        "login@teste.com"
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
    void deveRejeitarSenhaErrada()
            throws Exception {

        criarUsuario(
                "Usuario Senha",
                "senha@teste.com",
                "Senha@123",
                PerfilUsuario.CONSULTA
        );

        String json =
                """
                {
                  "email": "senha@teste.com",
                  "senha": "SenhaErrada"
                }
                """;

        mockMvc.perform(
                        post("/auth/login")
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
                        jsonPath("$.status")
                                .value(
                                        401
                                )
                )

                .andExpect(
                        jsonPath("$.mensagem")
                                .value(
                                        "Email ou senha inválidos"
                                )
                );
    }

    @Test
    void deveRejeitarEmailInexistente()
            throws Exception {

        String json =
                """
                {
                  "email": "naoexiste@teste.com",
                  "senha": "Senha@123"
                }
                """;

        mockMvc.perform(
                        post("/auth/login")
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
                        jsonPath("$.status")
                                .value(
                                        401
                                )
                )

                .andExpect(
                        jsonPath("$.mensagem")
                                .value(
                                        "Email ou senha inválidos"
                                )
                );
    }

    @Test
    void deveRejeitarEmailInvalidoNoLogin()
            throws Exception {

        String json =
                """
                {
                  "email": "email-invalido",
                  "senha": "Senha@123"
                }
                """;

        mockMvc.perform(
                        post("/auth/login")
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
    void deveRejeitarSenhaVaziaNoLogin()
            throws Exception {

        String json =
                """
                {
                  "email": "login@teste.com",
                  "senha": ""
                }
                """;

        mockMvc.perform(
                        post("/auth/login")
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
    void deveCadastrarUsuarioComSucesso()
            throws Exception {

        String json =
                """
                {
                  "nomeUsuario": "Novo Usuario",
                  "email": "novo@teste.com",
                  "senha": "Senha@123"
                }
                """;

        mockMvc.perform(
                        post("/auth/register")
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
                        jsonPath("$.nomeUsuario")
                                .value(
                                        "Novo Usuario"
                                )
                )

                .andExpect(
                        jsonPath("$.email")
                                .value(
                                        "novo@teste.com"
                                )
                )

                .andExpect(
                        jsonPath("$.perfil")
                                .value(
                                        "CONSULTA"
                                )
                )

                .andExpect(
                        jsonPath("$.mensagem")
                                .value(
                                        "Usuário cadastrado com sucesso"
                                )
                );
    }

    @Test
    void deveRejeitarEmailDuplicado()
            throws Exception {

        criarUsuario(
                "Usuario Existente",
                "duplicado@teste.com",
                "Senha@123",
                PerfilUsuario.CONSULTA
        );

        String json =
                """
                {
                  "nomeUsuario": "Outro Usuario",
                  "email": "duplicado@teste.com",
                  "senha": "Senha@456"
                }
                """;

        mockMvc.perform(
                        post("/auth/register")
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
                                        "Email já cadastrado"
                                )
                );
    }

    @Test
    void deveRejeitarNomeUsuarioDuplicado()
            throws Exception {

        criarUsuario(
                "Usuario Duplicado",
                "original@teste.com",
                "Senha@123",
                PerfilUsuario.CONSULTA
        );

        String json =
                """
                {
                  "nomeUsuario": "Usuario Duplicado",
                  "email": "outro@teste.com",
                  "senha": "Senha@456"
                }
                """;

        mockMvc.perform(
                        post("/auth/register")
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
                                        "Nome de usuário já está em uso"
                                )
                );
    }

    @Test
    void deveRejeitarSenhaCurtaNoCadastro()
            throws Exception {

        String json =
                """
                {
                  "nomeUsuario": "Senha Curta",
                  "email": "curta@teste.com",
                  "senha": "1234567"
                }
                """;

        mockMvc.perform(
                        post("/auth/register")
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
    void deveBloquearDepoisDeCincoTentativasErradas()
            throws Exception {

        criarUsuario(
                "Usuario Bloqueio",
                "bloqueio@teste.com",
                "Senha@123",
                PerfilUsuario.CONSULTA
        );

        String json =
                """
                {
                  "email": "bloqueio@teste.com",
                  "senha": "SenhaErrada"
                }
                """;

        for (
                int tentativa = 1;
                tentativa <= 4;
                tentativa++
        ) {

            mockMvc.perform(
                            post("/auth/login")
                                    .contentType(
                                            MediaType.APPLICATION_JSON
                                    )
                                    .content(
                                            json
                                    )
                    )

                    .andExpect(
                            status().isUnauthorized()
                    );
        }

        mockMvc.perform(
                        post("/auth/login")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        json
                                )
                )

                .andExpect(
                        status().isTooManyRequests()
                )

                .andExpect(
                        jsonPath("$.status")
                                .value(
                                        429
                                )
                )

                .andExpect(
                        jsonPath("$.mensagem")
                                .value(
                                        "Muitas tentativas de login. Tente novamente em alguns minutos"
                                )
                );
    }

    private void criarUsuario(
            String nomeUsuario,
            String email,
            String senha,
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
                        senha
                )
        );

        usuario.setPerfil(
                perfil
        );

        userRepository.save(
                usuario
        );
    }
}