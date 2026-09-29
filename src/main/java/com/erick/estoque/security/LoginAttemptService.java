package com.erick.estoque.security;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private static final int MAX_TENTATIVAS_USUARIO_IP =
            5;

    private static final int MAX_TENTATIVAS_IP =
            20;

    private static final Duration JANELA_TENTATIVAS =
            Duration.ofMinutes(5);

    private static final Duration TEMPO_BLOQUEIO =
            Duration.ofMinutes(5);

    private final ConcurrentHashMap<String, EstadoTentativas>
            tentativasPorUsuarioIp =
            new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, EstadoTentativas>
            tentativasPorIp =
            new ConcurrentHashMap<>();

    public boolean estaBloqueado(
            String email,
            String ip
    ) {

        Instant agora =
                Instant.now();

        String emailNormalizado =
                normalizarEmail(
                        email
                );

        String ipNormalizado =
                normalizarIp(
                        ip
                );

        String chaveUsuarioIp =
                criarChaveUsuarioIp(
                        emailNormalizado,
                        ipNormalizado
                );

        return estaBloqueado(
                tentativasPorUsuarioIp,
                chaveUsuarioIp,
                agora
        ) || estaBloqueado(
                tentativasPorIp,
                ipNormalizado,
                agora
        );
    }

    public void registrarFalha(
            String email,
            String ip
    ) {

        Instant agora =
                Instant.now();

        String emailNormalizado =
                normalizarEmail(
                        email
                );

        String ipNormalizado =
                normalizarIp(
                        ip
                );

        String chaveUsuarioIp =
                criarChaveUsuarioIp(
                        emailNormalizado,
                        ipNormalizado
                );

        registrarFalha(
                tentativasPorUsuarioIp,
                chaveUsuarioIp,
                MAX_TENTATIVAS_USUARIO_IP,
                agora
        );

        registrarFalha(
                tentativasPorIp,
                ipNormalizado,
                MAX_TENTATIVAS_IP,
                agora
        );
    }

    public void registrarSucesso(
            String email,
            String ip
    ) {

        String emailNormalizado =
                normalizarEmail(
                        email
                );

        String ipNormalizado =
                normalizarIp(
                        ip
                );

        String chaveUsuarioIp =
                criarChaveUsuarioIp(
                        emailNormalizado,
                        ipNormalizado
                );

        tentativasPorUsuarioIp.remove(
                chaveUsuarioIp
        );
    }

    private boolean estaBloqueado(
            ConcurrentHashMap<String, EstadoTentativas> mapa,
            String chave,
            Instant agora
    ) {

        EstadoTentativas estado =
                mapa.get(
                        chave
                );

        if (estado == null) {
            return false;
        }

        if (
                estado.bloqueadoAte() != null &&
                        estado.bloqueadoAte()
                                .isAfter(
                                        agora
                                )
        ) {

            return true;
        }

        boolean janelaExpirada =
                estado.inicioJanela()
                        .plus(
                                JANELA_TENTATIVAS
                        )
                        .isBefore(
                                agora
                        );

        boolean bloqueioExpirado =
                estado.bloqueadoAte() != null &&
                        !estado.bloqueadoAte()
                                .isAfter(
                                        agora
                                );

        if (
                janelaExpirada ||
                        bloqueioExpirado
        ) {

            mapa.remove(
                    chave,
                    estado
            );
        }

        return false;
    }

    private void registrarFalha(
            ConcurrentHashMap<String, EstadoTentativas> mapa,
            String chave,
            int limite,
            Instant agora
    ) {

        mapa.compute(
                chave,
                (
                        chaveAtual,
                        estadoAtual
                ) -> {

                    if (
                            estadoAtual == null ||
                                    estadoAtual
                                            .inicioJanela()
                                            .plus(
                                                    JANELA_TENTATIVAS
                                            )
                                            .isBefore(
                                                    agora
                                            )
                    ) {

                        return new EstadoTentativas(
                                1,
                                agora,
                                null
                        );
                    }

                    if (
                            estadoAtual.bloqueadoAte() != null &&
                                    estadoAtual
                                            .bloqueadoAte()
                                            .isAfter(
                                                    agora
                                            )
                    ) {

                        return estadoAtual;
                    }

                    int quantidade =
                            estadoAtual.quantidade() + 1;

                    Instant bloqueadoAte =
                            quantidade >= limite
                                    ? agora.plus(
                                    TEMPO_BLOQUEIO
                            )
                                    : null;

                    return new EstadoTentativas(
                            quantidade,
                            estadoAtual.inicioJanela(),
                            bloqueadoAte
                    );
                }
        );
    }

    private String criarChaveUsuarioIp(
            String email,
            String ip
    ) {

        return email +
                "|" +
                ip;
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

    private String normalizarIp(
            String ip
    ) {

        if (
                ip == null ||
                        ip.isBlank()
        ) {
            return "IP_DESCONHECIDO";
        }

        return ip.trim();
    }

    private record EstadoTentativas(

            int quantidade,

            Instant inicioJanela,

            Instant bloqueadoAte

    ) {
    }
}