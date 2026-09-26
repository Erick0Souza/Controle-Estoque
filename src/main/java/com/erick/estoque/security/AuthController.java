package com.erick.estoque.security;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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

        String email =
                request.email()
                        .trim()
                        .toLowerCase();


        if (userRepository.existsByEmail(email)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email já cadastrado"
            );
        }


        UserEntity usuario =
                new UserEntity();


        usuario.setEmail(
                email
        );


        usuario.setSenha(
                passwordEncoder.encode(
                        request.senha()
                )
        );


        UserEntity usuarioSalvo =
                userRepository.save(
                        usuario
                );


        return new CadastroResponse(
                usuarioSalvo.getId(),
                usuarioSalvo.getEmail(),
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
                        .toLowerCase();


        UserEntity usuario =
                userRepository
                        .findByEmail(email)
                        .orElseThrow(() ->
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
                token
        );
    }

    public record CadastroRequest(

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

            String email,

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

            String token

    ) {
    }
}