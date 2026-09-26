package com.erick.estoque.produto;

import com.erick.estoque.categoria.Categoria;
import com.erick.estoque.categoria.CategoriaRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/produtos")
@SecurityRequirement(name = "bearerAuth")
public class ProdutoController {

 private final ProdutoRepository produtoRepository;
 private final CategoriaRepository categoriaRepository;
 private final ProdutoImagemService produtoImagemService;

 public ProdutoController(
         ProdutoRepository produtoRepository,
         CategoriaRepository categoriaRepository,
         ProdutoImagemService produtoImagemService
 ) {
  this.produtoRepository = produtoRepository;
  this.categoriaRepository = categoriaRepository;
  this.produtoImagemService = produtoImagemService;
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
   produtos =
           produtoRepository.findAll();
  }

  return produtos.stream()
          .map(this::toResponse)
          .toList();
 }

 @GetMapping("/{id}")
 public ProdutoResponse buscar(
         @PathVariable Long id
 ) {

  Produto produto =
          buscarProduto(id);

  return toResponse(produto);
 }

 @PostMapping
 @ResponseStatus(HttpStatus.CREATED)
 public ProdutoResponse criar(
         @Valid
         @RequestBody
         ProdutoRequest request
 ) {

  Categoria categoria =
          buscarCategoria(
                  request.categoriaId()
          );

  Produto produto =
          new Produto();

  produto.setNome(
          request.nome()
  );

  produto.setPreco(
          request.preco()
  );

  produto.setQuantidade(
          request.quantidade()
  );

  produto.setCategoria(
          categoria
  );

  produto.setDescricao(
          request.descricao()
  );

  Produto produtoSalvo =
          produtoRepository.save(
                  produto
          );

  return toResponse(
          produtoSalvo
  );
 }

 @PutMapping("/{id}")
 public ProdutoResponse editar(
         @PathVariable Long id,
         @Valid
         @RequestBody
         ProdutoRequest request
 ) {

  Produto produto =
          buscarProduto(id);

  Categoria categoria =
          buscarCategoria(
                  request.categoriaId()
          );

  produto.setNome(
          request.nome()
  );

  produto.setPreco(
          request.preco()
  );

  produto.setQuantidade(
          request.quantidade()
  );

  produto.setCategoria(
          categoria
  );

  produto.setDescricao(
          request.descricao()
  );

  Produto produtoSalvo =
          produtoRepository.save(
                  produto
          );

  return toResponse(
          produtoSalvo
  );
 }

 @PostMapping(
         value = "/{id}/imagem",
         consumes = MediaType.MULTIPART_FORM_DATA_VALUE
 )
 public ImagemResponse adicionarImagem(
         @PathVariable Long id,
         @RequestPart("imagem")
         MultipartFile imagem
 ) {

  Produto produto =
          buscarProduto(id);

  String imagemAnterior =
          produto.getImagemUrl();

  String novaImagemUrl =
          produtoImagemService
                  .salvarImagem(imagem);

  produto.setImagemUrl(
          novaImagemUrl
  );

  try {

   produtoRepository.save(
           produto
   );

  } catch (RuntimeException exception) {

   produtoImagemService
           .excluirImagem(
                   novaImagemUrl
           );

   throw exception;
  }

  if (
          imagemAnterior != null
                  && !imagemAnterior.isBlank()
                  && !imagemAnterior.equals(
                  novaImagemUrl
          )
  ) {

   produtoImagemService
           .excluirImagem(
                   imagemAnterior
           );
  }

  return new ImagemResponse(
          produto.getId(),
          produto.getImagemUrl(),
          "Imagem adicionada com sucesso"
  );
 }

 @DeleteMapping("/{id}/imagem")
 @ResponseStatus(HttpStatus.NO_CONTENT)
 public void excluirImagem(
         @PathVariable Long id
 ) {

  Produto produto =
          buscarProduto(id);

  String imagemUrl =
          produto.getImagemUrl();

  if (
          imagemUrl == null
                  || imagemUrl.isBlank()
  ) {

   throw new ResponseStatusException(
           HttpStatus.NOT_FOUND,
           "Produto não possui imagem"
   );
  }

  produto.setImagemUrl(
          null
  );

  produtoRepository.save(
          produto
  );

  produtoImagemService
          .excluirImagem(
                  imagemUrl
          );
 }

 @DeleteMapping("/{id}")
 @ResponseStatus(HttpStatus.NO_CONTENT)
 public void excluir(
         @PathVariable Long id
 ) {

  Produto produto =
          buscarProduto(id);

  String imagemUrl =
          produto.getImagemUrl();

  produtoRepository.delete(
          produto
  );

  if (
          imagemUrl != null
                  && !imagemUrl.isBlank()
  ) {

   produtoImagemService
           .excluirImagem(
                   imagemUrl
           );
  }
 }

 private Produto buscarProduto(
         Long id
 ) {

  return produtoRepository
          .findById(id)
          .orElseThrow(
                  () ->
                          new ResponseStatusException(
                                  HttpStatus.NOT_FOUND,
                                  "Produto não encontrado"
                          )
          );
 }

 private Categoria buscarCategoria(
         Long id
 ) {

  return categoriaRepository
          .findById(id)
          .orElseThrow(
                  () ->
                          new ResponseStatusException(
                                  HttpStatus.BAD_REQUEST,
                                  "Categoria inválida"
                          )
          );
 }

 private ProdutoResponse toResponse(
         Produto produto
 ) {

  return new ProdutoResponse(
          produto.getId(),
          produto.getNome(),
          produto.getPreco(),
          produto.getQuantidade(),
          produto.getCategoria().getId(),
          produto.getCategoria().getNome(),
          produto.getDescricao(),
          produto.getImagemUrl()
  );
 }

 public record ImagemResponse(
         Long produtoId,
         String imagemUrl,
         String mensagem
 ) {
 }
}