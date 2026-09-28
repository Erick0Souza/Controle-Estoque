package com.erick.estoque.recuperacaosenha;

import com.erick.estoque.security.UserEntity;
import com.erick.estoque.security.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Locale;
import java.util.Optional;

@Service
public class RecuperacaoSenhaService {

    private static final SecureRandom SECURE_RANDOM =
            new SecureRandom();

    private final UserRepository userRepository;

    private final RecuperacaoSenhaTokenRepository
            recuperacaoSenhaTokenRepository;

    private final PasswordEncoder passwordEncoder;

    private final EmailRecuperacaoSenhaService
            emailRecuperacaoSenhaService;

    private final long expiracaoMinutos;

    public RecuperacaoSenhaService(
            UserRepository userRepository,
            RecuperacaoSenhaTokenRepository recuperacaoSenhaTokenRepository,
            PasswordEncoder passwordEncoder,
            EmailRecuperacaoSenhaService emailRecuperacaoSenhaService,
            @Value(
                    "${app.recuperacao-senha.expiracao-minutos:30}"
            )
            long expiracaoMinutos
    ) {
        this.userRepository =
                userRepository;

        this.recuperacaoSenhaTokenRepository =
                recuperacaoSenhaTokenRepository;

        this.passwordEncoder =
                passwordEncoder;

        this.emailRecuperacaoSenhaService =
                emailRecuperacaoSenhaService;

        this.expiracaoMinutos =
                expiracaoMinutos;
    }

    @Transactional
    public Optional<TokenGerado> solicitarRecuperacao(
            String email
    ) {

        LocalDateTime agora =
                LocalDateTime.now();

        limparTokensExpirados(
                agora
        );

        String emailNormalizado =
                normalizarEmail(
                        email
                );

        Optional<UserEntity> usuarioOptional =
                userRepository.findByEmail(
                        emailNormalizado
                );

        if (
                usuarioOptional.isEmpty()
        ) {

            return Optional.empty();
        }

        UserEntity usuario =
                usuarioOptional.get();

        recuperacaoSenhaTokenRepository
                .deleteByUsuarioId(
                        usuario.getId()
                );

        String token =
                gerarTokenSeguro();

        String tokenHash =
                gerarHashToken(
                        token
                );

        LocalDateTime expiracao =
                agora.plusMinutes(
                        expiracaoMinutos
                );

        RecuperacaoSenhaToken recuperacao =
                new RecuperacaoSenhaToken();

        recuperacao.setUsuarioId(
                usuario.getId()
        );

        recuperacao.setTokenHash(
                tokenHash
        );

        recuperacao.setCriadoEm(
                agora
        );

        recuperacao.setExpiraEm(
                expiracao
        );

        recuperacaoSenhaTokenRepository.save(
                recuperacao
        );

        boolean emailEnviado =
                emailRecuperacaoSenhaService
                        .enviarLinkRecuperacao(
                                usuario.getEmail(),
                                token,
                                expiracao
                        );

        if (
                !emailEnviado
        ) {

            recuperacaoSenhaTokenRepository
                    .deleteByUsuarioId(
                            usuario.getId()
                    );

            return Optional.empty();
        }

        return Optional.of(
                new TokenGerado(
                        usuario.getEmail(),
                        token,
                        expiracao
                )
        );
    }

    @Transactional
    public boolean validarToken(
            String token
    ) {

        if (
                token == null ||
                        token.isBlank()
        ) {
            return false;
        }

        String tokenHash =
                gerarHashToken(
                        token.trim()
                );

        Optional<RecuperacaoSenhaToken>
                tokenOptional =
                recuperacaoSenhaTokenRepository
                        .findByTokenHash(
                                tokenHash
                        );

        if (
                tokenOptional.isEmpty()
        ) {
            return false;
        }

        RecuperacaoSenhaToken recuperacao =
                tokenOptional.get();

        LocalDateTime agora =
                LocalDateTime.now();

        if (
                recuperacao
                        .getExpiraEm()
                        .isBefore(
                                agora
                        ) ||
                        recuperacao
                                .getExpiraEm()
                                .isEqual(
                                        agora
                                )
        ) {

            recuperacaoSenhaTokenRepository
                    .delete(
                            recuperacao
                    );

            return false;
        }

        return userRepository.existsById(
                recuperacao.getUsuarioId()
        );
    }

    @Transactional
    public void redefinirSenha(
            String token,
            String novaSenha,
            String confirmarSenha
    ) {

        if (
                token == null ||
                        token.isBlank()
        ) {

            throw tokenInvalido();
        }

        if (
                novaSenha == null ||
                        novaSenha.isBlank()
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A nova senha é obrigatória"
            );
        }

        if (
                !novaSenha.equals(
                        confirmarSenha
                )
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A nova senha e a confirmação não são iguais"
            );
        }

        int tamanhoSenhaBytes =
                novaSenha
                        .getBytes(
                                StandardCharsets.UTF_8
                        )
                        .length;

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

        String tokenHash =
                gerarHashToken(
                        token.trim()
                );

        RecuperacaoSenhaToken recuperacao =
                recuperacaoSenhaTokenRepository
                        .findByTokenHash(
                                tokenHash
                        )
                        .orElseThrow(
                                this::tokenInvalido
                        );

        LocalDateTime agora =
                LocalDateTime.now();

        if (
                recuperacao
                        .getExpiraEm()
                        .isBefore(
                                agora
                        ) ||
                        recuperacao
                                .getExpiraEm()
                                .isEqual(
                                        agora
                                )
        ) {

            recuperacaoSenhaTokenRepository
                    .delete(
                            recuperacao
                    );

            throw tokenInvalido();
        }

        UserEntity usuario =
                userRepository
                        .findById(
                                recuperacao.getUsuarioId()
                        )
                        .orElseThrow(
                                this::tokenInvalido
                        );

        String senhaCriptografada =
                passwordEncoder.encode(
                        novaSenha
                );

        usuario.setSenha(
                senhaCriptografada
        );

        userRepository.save(
                usuario
        );

        recuperacaoSenhaTokenRepository
                .deleteByUsuarioId(
                        usuario.getId()
                );
    }

    @Transactional
    public void limparTokensExpirados(
            LocalDateTime agora
    ) {

        recuperacaoSenhaTokenRepository
                .deleteByExpiraEmBefore(
                        agora
                );
    }

    private String gerarTokenSeguro() {

        byte[] bytes =
                new byte[32];

        SECURE_RANDOM.nextBytes(
                bytes
        );

        return Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(
                        bytes
                );
    }

    private String gerarHashToken(
            String token
    ) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );

            byte[] hash =
                    digest.digest(
                            token.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            return Base64
                    .getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(
                            hash
                    );

        } catch (
                NoSuchAlgorithmException exception
        ) {

            throw new IllegalStateException(
                    "Não foi possível processar o token de recuperação.",
                    exception
            );
        }
    }

    private String normalizarEmail(
            String email
    ) {

        if (
                email == null
        ) {
            return "";
        }

        return email
                .trim()
                .toLowerCase(
                        Locale.ROOT
                );
    }

    private ResponseStatusException tokenInvalido() {

        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Token de recuperação inválido ou expirado"
        );
    }

    public record TokenGerado(

            String email,

            String token,

            LocalDateTime expiracao

    ) {
    }
}