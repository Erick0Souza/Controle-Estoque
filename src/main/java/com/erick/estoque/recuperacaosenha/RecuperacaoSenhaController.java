package com.erick.estoque.recuperacaosenha;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(
        "/auth/recuperacao-senha"
)
public class RecuperacaoSenhaController {

    private final RecuperacaoSenhaService
            recuperacaoSenhaService;

    public RecuperacaoSenhaController(
            RecuperacaoSenhaService recuperacaoSenhaService
    ) {
        this.recuperacaoSenhaService =
                recuperacaoSenhaService;
    }

    @PostMapping(
            "/solicitar"
    )
    public Map<String, String> solicitar(
            @Valid
            @RequestBody
            SolicitarRecuperacaoRequest request
    ) {

        recuperacaoSenhaService
                .solicitarRecuperacao(
                        request.email()
                );

        return Map.of(
                "mensagem",
                "Se o e-mail estiver cadastrado, as instruções de recuperação serão enviadas."
        );
    }

    @GetMapping(
            "/validar"
    )
    public Map<String, Boolean> validar(
            @RequestParam
            String token
    ) {

        boolean valido =
                recuperacaoSenhaService
                        .validarToken(
                                token
                        );

        return Map.of(
                "valido",
                valido
        );
    }

    @PostMapping(
            "/redefinir"
    )
    public Map<String, String> redefinir(
            @Valid
            @RequestBody
            RedefinirSenhaRequest request
    ) {

        recuperacaoSenhaService
                .redefinirSenha(
                        request.token(),
                        request.novaSenha(),
                        request.confirmarSenha()
                );

        return Map.of(
                "mensagem",
                "Senha redefinida com sucesso."
        );
    }
}