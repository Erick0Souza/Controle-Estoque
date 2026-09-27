package com.erick.estoque.auditoria;

import com.erick.estoque.security.UserEntity;
import com.erick.estoque.security.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

@Service
public class AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;
    private final UserRepository userRepository;

    public AuditoriaService(
            AuditoriaRepository auditoriaRepository,
            UserRepository userRepository
    ) {
        this.auditoriaRepository =
                auditoriaRepository;

        this.userRepository =
                userRepository;
    }

    @Transactional
    public void registrar(
            TipoAcaoAuditoria acao,
            String entidade,
            Long entidadeId,
            String descricao
    ) {

        Auditoria auditoria =
                new Auditoria();

        auditoria.setDataHora(
                LocalDateTime.now()
        );

        auditoria.setAcao(
                acao
        );

        auditoria.setEntidade(
                normalizarEntidade(
                        entidade
                )
        );

        auditoria.setEntidadeId(
                entidadeId
        );

        auditoria.setDescricao(
                normalizarDescricao(
                        descricao
                )
        );

        preencherUsuarioResponsavel(
                auditoria
        );

        auditoriaRepository.save(
                auditoria
        );
    }

    private void preencherUsuarioResponsavel(
            Auditoria auditoria
    ) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (
                authentication == null ||
                        !authentication.isAuthenticated() ||
                        authentication.getName() == null ||
                        authentication.getName().isBlank()
        ) {

            auditoria.setUsuarioNome(
                    "SISTEMA"
            );

            return;
        }

        String email =
                authentication
                        .getName()
                        .trim()
                        .toLowerCase(
                                Locale.ROOT
                        );

        Optional<UserEntity> usuarioOptional =
                userRepository.findByEmail(
                        email
                );

        if (
                usuarioOptional.isEmpty()
        ) {

            auditoria.setUsuarioNome(
                    email
            );

            auditoria.setUsuarioEmail(
                    email
            );

            return;
        }

        UserEntity usuario =
                usuarioOptional.get();

        auditoria.setUsuarioNome(
                limitarTexto(
                        usuario.getNomeUsuario(),
                        50
                )
        );

        auditoria.setUsuarioEmail(
                limitarTexto(
                        usuario.getEmail(),
                        150
                )
        );

        auditoria.setUsuarioPerfil(
                usuario.getPerfil()
        );
    }

    private String normalizarEntidade(
            String entidade
    ) {

        if (
                entidade == null ||
                        entidade.isBlank()
        ) {
            return "DESCONHECIDA";
        }

        String entidadeTratada =
                entidade
                        .trim()
                        .toUpperCase(
                                Locale.ROOT
                        );

        return limitarTexto(
                entidadeTratada,
                50
        );
    }

    private String normalizarDescricao(
            String descricao
    ) {

        if (
                descricao == null ||
                        descricao.isBlank()
        ) {
            return null;
        }

        return limitarTexto(
                descricao.trim(),
                1000
        );
    }

    private String limitarTexto(
            String texto,
            int tamanhoMaximo
    ) {

        if (texto == null) {
            return null;
        }

        if (
                texto.length() <=
                        tamanhoMaximo
        ) {
            return texto;
        }

        return texto.substring(
                0,
                tamanhoMaximo
        );
    }
}