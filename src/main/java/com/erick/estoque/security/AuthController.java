package com.erick.estoque.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LoginAttemptService loginAttemptService;

    private final String senhaFicticiaHash;

    private final boolean cadastroPublicoAtivo;

    public AuthController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            LoginAttemptService loginAttemptService,
            @Value(
                    "${app.security.public-registration:true}"
            )
            boolean cadastroPublicoAtivo
    ) {
        this.userRepository =
                userRepository;

        this.passwordEncoder =
                passwordEncoder;

        this.jwtService =
                jwtService;

        this.loginAttemptService =
                loginAttemptService;

        this.cadastroPublicoAtivo =
                cadastroPublicoAtivo;

        this.senhaFicticiaHash =
                passwordEncoder.encode(
                        UUID.randomUUID()
                                .toString()
                );
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public CadastroResponse cadastrar(
            @Valid
            @RequestBody
            CadastroRequest request
    ) {

        if (
                !cadastroPublicoAtivo
        ) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Cadastro público de usuários está desabilitado"
            );
        }

        String nomeUsuario =
                request.nomeUsuario()
                        .trim();

        String email =
                request.email()
                        .trim()
                        .toLowerCase(
                                Locale.ROOT
                        );

        validarSenhaBCrypt(
                request.senha()
        );

        if (
                userRepository
                        .existsByNomeUsuarioIgnoreCase(
                                nomeUsuario
                        )
        ) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Nome de usuário já está em uso"
            );
        }

        if (
                userRepository.existsByEmail(
                        email
                )
        ) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email já cadastrado"
            );
        }

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
                        request.senha()
                )
        );

        usuario.setPerfil(
                PerfilUsuario.CONSULTA
        );

        UserEntity usuarioSalvo =
                userRepository.save(
                        usuario
                );

        return new CadastroResponse(
                usuarioSalvo.getId(),
                usuarioSalvo.getNomeUsuario(),
                usuarioSalvo.getEmail(),
                usuarioSalvo.getPerfil(),
                "Usuário cadastrado com sucesso"
        );
    }

    @PostMapping("/login")
    public LoginResponse login(
            @Valid
            @RequestBody
            LoginRequest request,
            HttpServletRequest httpRequest
    ) {

        String email =
                request.email()
                        .trim()
                        .toLowerCase(
                                Locale.ROOT
                        );

        String ip =
                httpRequest.getRemoteAddr();

        if (
                loginAttemptService
                        .estaBloqueado(
                                email,
                                ip
                        )
        ) {

            throw muitasTentativas();
        }

        Optional<UserEntity> usuarioOptional =
                userRepository
                        .findByEmail(
                                email
                        );

        String hashParaComparacao =
                usuarioOptional
                        .map(
                                UserEntity::getSenha
                        )
                        .orElse(
                                senhaFicticiaHash
                        );

        boolean senhaCorreta =
                passwordEncoder.matches(
                        request.senha(),
                        hashParaComparacao
                );

        if (
                usuarioOptional.isEmpty() ||
                        !senhaCorreta
        ) {

            loginAttemptService
                    .registrarFalha(
                            email,
                            ip
                    );

            if (
                    loginAttemptService
                            .estaBloqueado(
                                    email,
                                    ip
                            )
            ) {

                throw muitasTentativas();
            }

            throw credenciaisInvalidas();
        }

        UserEntity usuario =
                usuarioOptional.get();

        loginAttemptService
                .registrarSucesso(
                        email,
                        ip
                );

        String token =
                jwtService.gerar(
                        usuario.getEmail()
                );

        return new LoginResponse(
                token,
                usuario.getNomeUsuario(),
                usuario.getEmail(),
                usuario.getPerfil()
        );
    }

    private void validarSenhaBCrypt(
            String senha
    ) {

        int tamanhoSenhaBytes =
                senha.getBytes(
                        StandardCharsets.UTF_8
                ).length;

        if (
                tamanhoSenhaBytes < 8
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A senha deve possuir pelo menos 8 caracteres"
            );
        }

        if (
                tamanhoSenhaBytes > 72
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A senha informada é muito longa"
            );
        }
    }

    private ResponseStatusException credenciaisInvalidas() {

        return new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Email ou senha inválidos"
        );
    }

    private ResponseStatusException muitasTentativas() {

        return new ResponseStatusException(
                HttpStatus.TOO_MANY_REQUESTS,
                "Muitas tentativas de login. Tente novamente em alguns minutos"
        );
    }

    public record CadastroRequest(

            @NotBlank(
                    message = "Nome de usuário é obrigatório"
            )
            @Size(
                    min = 3,
                    max = 50,
                    message = "O nome de usuário deve ter entre 3 e 50 caracteres"
            )
            @Pattern(
                    regexp = "^[A-Za-zÀ-ÿ0-9._ -]+$",
                    message = "Nome de usuário contém caracteres inválidos"
            )
            String nomeUsuario,

            @NotBlank(
                    message = "Email é obrigatório"
            )
            @Email(
                    message = "Email inválido"
            )
            String email,

            @NotBlank(
                    message = "Senha é obrigatória"
            )
            @Size(
                    min = 8,
                    max = 72,
                    message = "A senha deve ter entre 8 e 72 caracteres"
            )
            String senha

    ) {
    }

    public record CadastroResponse(

            Long id,

            String nomeUsuario,

            String email,

            PerfilUsuario perfil,

            String mensagem

    ) {
    }

    public record LoginRequest(

            @NotBlank(
                    message = "Email é obrigatório"
            )
            @Email(
                    message = "Email inválido"
            )
            String email,

            @NotBlank(
                    message = "Senha é obrigatória"
            )
            String senha

    ) {
    }

    public record LoginResponse(

            String token,

            String nomeUsuario,

            String email,

            PerfilUsuario perfil

    ) {
    }
}