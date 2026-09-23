package com.erick.estoque.produto;

import com.erick.estoque.categoria.Categoria;
import com.erick.estoque.categoria.CategoriaRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


import java.util.List;

@RestController
@RequestMapping("/produtos")
@SecurityRequirement(name = "bearerAuth")
public class ProdutoController {

 private final ProdutoRepository produtoRepository;
 private final CategoriaRepository categoriaRepository;

 public ProdutoController(
         ProdutoRepository produtoRepository,
         CategoriaRepository categoriaRepository
 ) {
  this.produtoRepository = produtoRepository;
  this.categoriaRepository = categoriaRepository;
 }


 @GetMapping
 public List<ProdutoResponse> listar(
         @RequestParam(required = false) String nome
 ) {

  List<Produto> produtos;

  if (nome != null && !nome.isBlank()) {
   produtos =
           produtoRepository.findByNomeContainingIgnoreCase(nome);
  } else {
   produtos = produtoRepository.findAll();
  }

  return produtos.stream()
          .map(this::toResponse)
          .toList();
 }


 @GetMapping("/{id}")
 public ProdutoResponse buscar(@PathVariable Long id) {

  Produto produto = buscarProduto(id);

  return toResponse(produto);
 }


 @PostMapping
 @ResponseStatus(HttpStatus.CREATED)
 public ProdutoResponse criar(
         @Valid @RequestBody ProdutoRequest request
 ) {

  Categoria categoria =
          buscarCategoria(request.categoriaId());

  Produto produto = new Produto();

  produto.setNome(request.nome());
  produto.setPreco(request.preco());
  produto.setQuantidade(request.quantidade());
  produto.setCategoria(categoria);
  produto.setDescricao(request.descricao());

  Produto produtoSalvo =
          produtoRepository.save(produto);

  return toResponse(produtoSalvo);
 }


 @PutMapping("/{id}")
 public ProdutoResponse editar(
         @PathVariable Long id,
         @Valid @RequestBody ProdutoRequest request
 ) {

  Produto produto = buscarProduto(id);

  Categoria categoria =
          buscarCategoria(request.categoriaId());

  produto.setNome(request.nome());
  produto.setPreco(request.preco());
  produto.setQuantidade(request.quantidade());
  produto.setCategoria(categoria);
  produto.setDescricao(request.descricao());

  Produto produtoSalvo =
          produtoRepository.save(produto);

  return toResponse(produtoSalvo);
 }


 @DeleteMapping("/{id}")
 @ResponseStatus(HttpStatus.NO_CONTENT)
 public void excluir(@PathVariable Long id) {

  Produto produto = buscarProduto(id);

  produtoRepository.delete(produto);
 }

 private Produto buscarProduto(Long id) {

  return produtoRepository.findById(id)
          .orElseThrow(() ->
                  new ResponseStatusException(
                          HttpStatus.NOT_FOUND,
                          "Produto não encontrado"
                  )
          );
 }

 private Categoria buscarCategoria(Long id) {

  return categoriaRepository.findById(id)
          .orElseThrow(() ->
                  new ResponseStatusException(
                          HttpStatus.BAD_REQUEST,
                          "Categoria inválida"
                  )
          );
 }

 private ProdutoResponse toResponse(Produto produto) {

  return new ProdutoResponse(
          produto.getId(),
          produto.getNome(),
          produto.getPreco(),
          produto.getQuantidade(),
          produto.getCategoria().getId(),
          produto.getCategoria().getNome(),
          produto.getDescricao()
  );
 }
}