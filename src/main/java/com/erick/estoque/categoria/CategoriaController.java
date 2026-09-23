package com.erick.estoque.categoria;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/categorias")
@SecurityRequirement(name = "bearerAuth")
public class CategoriaController {

    private final CategoriaRepository repo;

    public CategoriaController(CategoriaRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Categoria> listar() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public Categoria buscar(@PathVariable Long id) {
        return repo.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Categoria não encontrada"
                        )
                );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Categoria criar(
            @Valid @RequestBody CategoriaRequest req
    ) {

        Categoria categoria = new Categoria();
        categoria.setNome(req.nome());

        return repo.save(categoria);
    }

    @PutMapping("/{id}")
    public Categoria editar(
            @PathVariable Long id,
            @Valid @RequestBody CategoriaRequest req
    ) {

        Categoria categoria = buscar(id);

        categoria.setNome(req.nome());

        return repo.save(categoria);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {

        Categoria categoria = buscar(id);

        repo.delete(categoria);
    }

    public record CategoriaRequest(

            @NotBlank(message = "O nome da categoria é obrigatório")
            String nome

    ) {
    }
}