package com.erick.estoque.security;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public CadastroResponse cadastrar(
            @Valid
            @RequestBody
            CadastroRequest request
    ) {

        String nomeUsuario =
                request.nomeUsuario()
                        .trim();

        String email =
                request.email()
                        .trim()
                        .toLowerCase(
                                Locale.ROOT
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
            LoginRequest request
    ) {

        String email =
                request.email()
                        .trim()
                        .toLowerCase(
                                Locale.ROOT
                        );

        UserEntity usuario =
                userRepository
                        .findByEmail(
                                email
                        )
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.UNAUTHORIZED,
                                                "Email ou senha inválidos"
                                        )
                        );

        boolean senhaCorreta =
                passwordEncoder.matches(
                        request.senha(),
                        usuario.getSenha()
                );

        if (!senhaCorreta) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Email ou senha inválidos"
            );
        }

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
                    min = 6,
                    max = 100,
                    message = "A senha deve ter entre 6 e 100 caracteres"
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