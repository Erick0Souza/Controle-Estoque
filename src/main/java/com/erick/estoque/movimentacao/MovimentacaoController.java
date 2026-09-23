package com.erick.estoque.movimentacao;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/movimentacoes")
@SecurityRequirement(name = "bearerAuth")
public class MovimentacaoController {

    private final MovimentacaoService service;

    public MovimentacaoController(
            MovimentacaoService service
    ) {
        this.service = service;
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MovimentacaoResponse movimentar(
            @Valid
            @RequestBody
            MovimentacaoRequest request
    ) {

        return service.movimentar(
                request
        );
    }


    @GetMapping
    public List<MovimentacaoResponse> listar() {

        return service.listar();
    }


    @GetMapping("/produto/{produtoId}")
    public List<MovimentacaoResponse>
    listarPorProduto(
            @PathVariable Long produtoId
    ) {

        return service.listarPorProduto(
                produtoId
        );
    }
}