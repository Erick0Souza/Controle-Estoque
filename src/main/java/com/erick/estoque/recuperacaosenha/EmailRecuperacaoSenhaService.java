package com.erick.estoque.recuperacaosenha;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class EmailRecuperacaoSenhaService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    EmailRecuperacaoSenhaService.class
            );

    private static final DateTimeFormatter FORMATADOR_DATA =
            DateTimeFormatter.ofPattern(
                    "dd/MM/yyyy HH:mm"
            );

    private final JavaMailSender javaMailSender;

    private final String remetente;

    private final String appUrl;

    public EmailRecuperacaoSenhaService(
            JavaMailSender javaMailSender,

            @Value(
                    "${app.mail.remetente:}"
            )
            String remetente,

            @Value(
                    "${app.url:http://localhost:8081}"
            )
            String appUrl
    ) {
        this.javaMailSender =
                javaMailSender;

        this.remetente =
                remetente;

        this.appUrl =
                normalizarAppUrl(
                        appUrl
                );
    }

    public boolean enviarLinkRecuperacao(
            String email,
            String token,
            LocalDateTime expiracao
    ) {

        try {

            String linkRecuperacao =
                    appUrl +
                            "/redefinir-senha.html?token=" +
                            token;

            SimpleMailMessage mensagem =
                    new SimpleMailMessage();

            if (
                    remetente != null &&
                            !remetente.isBlank()
            ) {

                mensagem.setFrom(
                        remetente
                );
            }

            mensagem.setTo(
                    email
            );

            mensagem.setSubject(
                    "Recuperação de senha - Controle de Estoque"
            );

            mensagem.setText(
                    criarMensagem(
                            linkRecuperacao,
                            expiracao
                    )
            );

            javaMailSender.send(
                    mensagem
            );

            return true;

        } catch (
                MailException exception
        ) {

            LOGGER.error(
                    "Falha ao enviar e-mail de recuperação de senha.",
                    exception
            );

            return false;
        }
    }

    private String criarMensagem(
            String linkRecuperacao,
            LocalDateTime expiracao
    ) {

        return """
                Olá,

                Foi solicitada uma redefinição de senha para sua conta no Controle de Estoque.

                Para criar uma nova senha, acesse o link abaixo:

                %s

                Este link é válido até:

                %s

                Se você não solicitou esta alteração, ignore este e-mail.

                Por segurança, o link poderá ser utilizado somente enquanto o token estiver válido.
                """.formatted(
                linkRecuperacao,
                expiracao.format(
                        FORMATADOR_DATA
                )
        );
    }

    private String normalizarAppUrl(
            String valor
    ) {

        if (
                valor == null ||
                        valor.isBlank()
        ) {

            return "http://localhost:8081";
        }

        String url =
                valor.trim();

        while (
                url.endsWith("/")
        ) {

            url =
                    url.substring(
                            0,
                            url.length() - 1
                    );
        }

        return url;
    }
}