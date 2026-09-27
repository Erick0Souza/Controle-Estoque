package com.erick.estoque.security;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@RestController
@RequestMapping("/admin/usuarios")
public class UsuarioAdminController {

    private final UserRepository userRepository;

    public UsuarioAdminController(
            UserRepository userRepository
    ) {
        this.userRepository =
                userRepository;
    }

    @GetMapping
    public Page<UsuarioResponse> listar(
            @RequestParam(required = false)
            String busca,

            @RequestParam(required = false)
            PerfilUsuario perfil,

            @RequestParam(defaultValue = "0")
            Integer page,

            @RequestParam(defaultValue = "10")
            Integer size,

            @RequestParam(defaultValue = "nomeUsuario")
            String sort,

            @RequestParam(defaultValue = "asc")
            String direction
    ) {

        validarPaginacao(
                page,
                size
        );

        String campoOrdenacao =
                normalizarCampoOrdenacao(
                        sort
                );

        Sort.Direction direcao =
                normalizarDirecao(
                        direction
                );

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(
                                direcao,
                                campoOrdenacao
                        )
                );

        Page<UserEntity> usuarios =
                userRepository.findAll(
                        UsuarioSpecification.comFiltros(
                                busca,
                                perfil
                        ),
                        pageable
                );

        return usuarios.map(
                this::toResponse
        );
    }

    @GetMapping("/{id}")
    public UsuarioResponse buscar(
            @PathVariable Long id
    ) {

        UserEntity usuario =
                buscarUsuario(
                        id
                );

        return toResponse(
                usuario
        );
    }

    @PutMapping("/{id}/perfil")
    public UsuarioResponse alterarPerfil(
            @PathVariable Long id,
            @Valid
            @RequestBody
            AlterarPerfilRequest request,
            Authentication authentication
    ) {

        UserEntity usuario =
                buscarUsuario(
                        id
                );

        PerfilUsuario perfilAtual =
                usuario.getPerfil();

        PerfilUsuario novoPerfil =
                request.perfil();

        if (
                perfilAtual == novoPerfil
        ) {

            return toResponse(
                    usuario
            );
        }

        String emailUsuarioLogado =
                authentication.getName();

        boolean alterandoProprioPerfil =
                usuario.getEmail()
                        .equalsIgnoreCase(
                                emailUsuarioLogado
                        );

        if (
                alterandoProprioPerfil &&
                        novoPerfil != PerfilUsuario.ADMIN
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Você não pode remover o perfil ADMIN da própria conta"
            );
        }

        if (
                perfilAtual == PerfilUsuario.ADMIN &&
                        novoPerfil != PerfilUsuario.ADMIN
        ) {

            long quantidadeAdmins =
                    userRepository.countByPerfil(
                            PerfilUsuario.ADMIN
                    );

            if (
                    quantidadeAdmins <= 1
            ) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "O sistema precisa possuir pelo menos um administrador"
                );
            }
        }

        usuario.setPerfil(
                novoPerfil
        );

        UserEntity usuarioSalvo =
                userRepository.save(
                        usuario
                );

        return toResponse(
                usuarioSalvo
        );
    }

    private UserEntity buscarUsuario(
            Long id
    ) {

        return userRepository
                .findById(
                        id
                )
                .orElseThrow(
                        () ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Usuário não encontrado"
                                )
                );
    }

    private void validarPaginacao(
            Integer page,
            Integer size
    ) {

        if (
                page == null ||
                        page < 0
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A página não pode ser negativa"
            );
        }

        if (
                size == null ||
                        size < 1 ||
                        size > 100
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O tamanho da página deve estar entre 1 e 100"
            );
        }
    }

    private String normalizarCampoOrdenacao(
            String sort
    ) {

        if (
                sort == null ||
                        sort.isBlank()
        ) {

            return "nomeUsuario";
        }

        return switch (
                sort
                        .trim()
                        .toLowerCase(
                                Locale.ROOT
                        )
                ) {

            case "id" ->
                    "id";

            case "nome",
                 "nomeusuario" ->
                    "nomeUsuario";

            case "email" ->
                    "email";

            case "perfil" ->
                    "perfil";

            default ->
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Campo de ordenação inválido"
                    );
        };
    }

    private Sort.Direction normalizarDirecao(
            String direction
    ) {

        if (
                direction == null ||
                        direction.isBlank() ||
                        direction.equalsIgnoreCase(
                                "asc"
                        )
        ) {

            return Sort.Direction.ASC;
        }

        if (
                direction.equalsIgnoreCase(
                        "desc"
                )
        ) {

            return Sort.Direction.DESC;
        }

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "A direção deve ser asc ou desc"
        );
    }

    private UsuarioResponse toResponse(
            UserEntity usuario
    ) {

        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNomeUsuario(),
                usuario.getEmail(),
                usuario.getPerfil()
        );
    }

    public record AlterarPerfilRequest(

            @NotNull(
                    message = "O perfil é obrigatório"
            )
            PerfilUsuario perfil

    ) {
    }

    public record UsuarioResponse(

            Long id,

            String nomeUsuario,

            String email,

            PerfilUsuario perfil

    ) {
    }
}