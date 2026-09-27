package com.erick.estoque.auditoria;

import com.erick.estoque.security.PerfilUsuario;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Locale;

@RestController
@RequestMapping("/admin/auditorias")
@SecurityRequirement(name = "bearerAuth")
public class AuditoriaController {

    private final AuditoriaRepository auditoriaRepository;

    public AuditoriaController(
            AuditoriaRepository auditoriaRepository
    ) {
        this.auditoriaRepository =
                auditoriaRepository;
    }

    @GetMapping
    public Page<AuditoriaResponse> listar(
            @RequestParam(required = false)
            String usuario,

            @RequestParam(required = false)
            PerfilUsuario perfil,

            @RequestParam(required = false)
            TipoAcaoAuditoria acao,

            @RequestParam(required = false)
            String entidade,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE_TIME
            )
            LocalDateTime dataInicio,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE_TIME
            )
            LocalDateTime dataFim,

            @RequestParam(defaultValue = "0")
            Integer page,

            @RequestParam(defaultValue = "20")
            Integer size,

            @RequestParam(defaultValue = "dataHora")
            String sort,

            @RequestParam(defaultValue = "desc")
            String direction
    ) {

        validarPaginacao(
                page,
                size
        );

        validarPeriodo(
                dataInicio,
                dataFim
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

        Page<Auditoria> auditorias =
                auditoriaRepository.findAll(
                        AuditoriaSpecification.comFiltros(
                                usuario,
                                perfil,
                                acao,
                                entidade,
                                dataInicio,
                                dataFim
                        ),
                        pageable
                );

        return auditorias.map(
                this::toResponse
        );
    }

    @GetMapping("/{id}")
    public AuditoriaResponse buscar(
            @PathVariable Long id
    ) {

        Auditoria auditoria =
                auditoriaRepository
                        .findById(
                                id
                        )
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Registro de auditoria não encontrado"
                                        )
                        );

        return toResponse(
                auditoria
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

    private void validarPeriodo(
            LocalDateTime dataInicio,
            LocalDateTime dataFim
    ) {

        if (
                dataInicio != null &&
                        dataFim != null &&
                        dataInicio.isAfter(
                                dataFim
                        )
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A data inicial não pode ser posterior à data final"
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

            return "dataHora";
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

            case "data",
                 "datahora" ->
                    "dataHora";

            case "usuario",
                 "usuarionome" ->
                    "usuarioNome";

            case "email",
                 "usuarioemail" ->
                    "usuarioEmail";

            case "perfil",
                 "usuarioperfil" ->
                    "usuarioPerfil";

            case "acao" ->
                    "acao";

            case "entidade" ->
                    "entidade";

            case "entidadeid" ->
                    "entidadeId";

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
                                "desc"
                        )
        ) {

            return Sort.Direction.DESC;
        }

        if (
                direction.equalsIgnoreCase(
                        "asc"
                )
        ) {

            return Sort.Direction.ASC;
        }

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "A direção deve ser asc ou desc"
        );
    }

    private AuditoriaResponse toResponse(
            Auditoria auditoria
    ) {

        return new AuditoriaResponse(
                auditoria.getId(),
                auditoria.getDataHora(),
                auditoria.getUsuarioNome(),
                auditoria.getUsuarioEmail(),
                auditoria.getUsuarioPerfil(),
                auditoria.getAcao(),
                auditoria.getEntidade(),
                auditoria.getEntidadeId(),
                auditoria.getDescricao()
        );
    }

    public record AuditoriaResponse(

            Long id,

            LocalDateTime dataHora,

            String usuarioNome,

            String usuarioEmail,

            PerfilUsuario usuarioPerfil,

            TipoAcaoAuditoria acao,

            String entidade,

            Long entidadeId,

            String descricao

    ) {
    }
}