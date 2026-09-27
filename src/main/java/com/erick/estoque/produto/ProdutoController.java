package com.erick.estoque.produto;

import com.erick.estoque.auditoria.AuditoriaService;
import com.erick.estoque.auditoria.TipoAcaoAuditoria;
import com.erick.estoque.categoria.Categoria;
import com.erick.estoque.categoria.CategoriaRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Locale;

@RestController
@RequestMapping("/produtos")
@SecurityRequirement(name = "bearerAuth")
public class ProdutoController {

    private final ProdutoRepository produtoRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProdutoImagemService produtoImagemService;
    private final ProdutoExclusaoService produtoExclusaoService;
    private final AuditoriaService auditoriaService;

    public ProdutoController(
            ProdutoRepository produtoRepository,
            CategoriaRepository categoriaRepository,
            ProdutoImagemService produtoImagemService,
            ProdutoExclusaoService produtoExclusaoService,
            AuditoriaService auditoriaService
    ) {
        this.produtoRepository = produtoRepository;
        this.categoriaRepository = categoriaRepository;
        this.produtoImagemService = produtoImagemService;
        this.produtoExclusaoService = produtoExclusaoService;
        this.auditoriaService = auditoriaService;
    }

    @GetMapping
    public Page<ProdutoResponse> listar(
            @RequestParam(required = false)
            String nome,

            @RequestParam(required = false)
            String sku,

            @RequestParam(required = false)
            Long categoriaId,

            @RequestParam(required = false)
            BigDecimal precoMin,

            @RequestParam(required = false)
            BigDecimal precoMax,

            @RequestParam(required = false)
            Integer quantidadeMin,

            @RequestParam(required = false)
            Integer quantidadeMax,

            @RequestParam(defaultValue = "0")
            Integer page,

            @RequestParam(defaultValue = "10")
            Integer size,

            @RequestParam(defaultValue = "nome")
            String sort,

            @RequestParam(defaultValue = "asc")
            String direction
    ) {

        validarFiltros(
                page,
                size,
                precoMin,
                precoMax,
                quantidadeMin,
                quantidadeMax
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

        Page<Produto> produtos =
                produtoRepository.findAll(
                        ProdutoSpecification.comFiltros(
                                nome,
                                sku,
                                categoriaId,
                                precoMin,
                                precoMax,
                                quantidadeMin,
                                quantidadeMax
                        ),
                        pageable
                );

        return produtos.map(
                this::toResponse
        );
    }

    @GetMapping("/{id}")
    public ProdutoResponse buscar(
            @PathVariable Long id
    ) {

        Produto produto =
                buscarProduto(
                        id
                );

        return toResponse(
                produto
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProdutoResponse criar(
            @Valid
            @RequestBody
            ProdutoRequest request
    ) {

        String sku =
                normalizarSku(
                        request.sku()
                );

        if (
                produtoRepository
                        .existsBySkuIgnoreCase(
                                sku
                        )
        ) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Já existe um produto com este SKU"
            );
        }

        Categoria categoria =
                buscarCategoria(
                        request.categoriaId()
                );

        Produto produto =
                new Produto();

        produto.setSku(
                sku
        );

        produto.setNome(
                request.nome()
                        .trim()
        );

        produto.setPreco(
                request.preco()
        );

        produto.setQuantidade(
                request.quantidade()
        );

        produto.setEstoqueMinimo(
                request.estoqueMinimo()
        );

        produto.setCategoria(
                categoria
        );

        produto.setDescricao(
                normalizarDescricao(
                        request.descricao()
                )
        );

        Produto produtoSalvo =
                produtoRepository.save(
                        produto
                );

        auditoriaService.registrar(
                TipoAcaoAuditoria.PRODUTO_CRIADO,
                "PRODUTO",
                produtoSalvo.getId(),
                "Produto \"" +
                        produtoSalvo.getNome() +
                        "\" criado"
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
                buscarProduto(
                        id
                );

        String sku =
                normalizarSku(
                        request.sku()
                );

        if (
                produtoRepository
                        .existsBySkuIgnoreCaseAndIdNot(
                                sku,
                                id
                        )
        ) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Já existe outro produto com este SKU"
            );
        }

        Categoria categoria =
                buscarCategoria(
                        request.categoriaId()
                );

        produto.setSku(
                sku
        );

        produto.setNome(
                request.nome()
                        .trim()
        );

        produto.setPreco(
                request.preco()
        );

        produto.setQuantidade(
                request.quantidade()
        );

        produto.setEstoqueMinimo(
                request.estoqueMinimo()
        );

        produto.setCategoria(
                categoria
        );

        produto.setDescricao(
                normalizarDescricao(
                        request.descricao()
                )
        );

        Produto produtoSalvo =
                produtoRepository.save(
                        produto
                );

        auditoriaService.registrar(
                TipoAcaoAuditoria.PRODUTO_EDITADO,
                "PRODUTO",
                produtoSalvo.getId(),
                "Produto \"" +
                        produtoSalvo.getNome() +
                        "\" editado"
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
                buscarProduto(
                        id
                );

        String imagemAnterior =
                produto.getImagemUrl();

        String novaImagemUrl =
                produtoImagemService
                        .salvarImagem(
                                imagem
                        );

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
                imagemAnterior != null &&
                        !imagemAnterior.isBlank() &&
                        !imagemAnterior.equals(
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
                buscarProduto(
                        id
                );

        String imagemUrl =
                produto.getImagemUrl();

        if (
                imagemUrl == null ||
                        imagemUrl.isBlank()
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

        produtoExclusaoService
                .excluirProduto(
                        id
                );
    }

    private Produto buscarProduto(
            Long id
    ) {

        return produtoRepository
                .findById(
                        id
                )
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
                .findById(
                        id
                )
                .orElseThrow(
                        () ->
                                new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Categoria inválida"
                                )
                );
    }

    private void validarFiltros(
            Integer page,
            Integer size,
            BigDecimal precoMin,
            BigDecimal precoMax,
            Integer quantidadeMin,
            Integer quantidadeMax
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

        if (
                precoMin != null &&
                        precoMin.compareTo(
                                BigDecimal.ZERO
                        ) < 0
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O preço mínimo não pode ser negativo"
            );
        }

        if (
                precoMax != null &&
                        precoMax.compareTo(
                                BigDecimal.ZERO
                        ) < 0
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O preço máximo não pode ser negativo"
            );
        }

        if (
                precoMin != null &&
                        precoMax != null &&
                        precoMin.compareTo(
                                precoMax
                        ) > 0
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O preço mínimo não pode ser maior que o preço máximo"
            );
        }

        if (
                quantidadeMin != null &&
                        quantidadeMin < 0
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A quantidade mínima não pode ser negativa"
            );
        }

        if (
                quantidadeMax != null &&
                        quantidadeMax < 0
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A quantidade máxima não pode ser negativa"
            );
        }

        if (
                quantidadeMin != null &&
                        quantidadeMax != null &&
                        quantidadeMin > quantidadeMax
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A quantidade mínima não pode ser maior que a quantidade máxima"
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

            return "nome";
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

            case "sku" ->
                    "sku";

            case "nome" ->
                    "nome";

            case "preco" ->
                    "preco";

            case "quantidade" ->
                    "quantidade";

            case "estoqueminimo" ->
                    "estoqueMinimo";

            case "categoria" ->
                    "categoria.nome";

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
                                "asc"
                        )
        ) {

            return Sort.Direction.ASC;
        }

        if (
                direction.equalsIgnoreCase(
                        "desc"
                )
        ) {

            return Sort.Direction.DESC;
        }

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "A direção deve ser asc ou desc"
        );
    }

    private String normalizarSku(
            String sku
    ) {

        return sku
                .trim()
                .toUpperCase(
                        Locale.ROOT
                );
    }

    private String normalizarDescricao(
            String descricao
    ) {

        if (
                descricao == null
        ) {

            return null;
        }

        String descricaoTratada =
                descricao.trim();

        if (
                descricaoTratada.isBlank()
        ) {

            return null;
        }

        return descricaoTratada;
    }

    private ProdutoResponse toResponse(
            Produto produto
    ) {

        int quantidade =
                produto.getQuantidade() != null
                        ? produto.getQuantidade()
                        : 0;

        int estoqueMinimo =
                produto.getEstoqueMinimo() != null
                        ? produto.getEstoqueMinimo()
                        : 0;

        boolean estoqueBaixo =
                quantidade <= estoqueMinimo;

        return new ProdutoResponse(
                produto.getId(),
                produto.getSku(),
                produto.getNome(),
                produto.getPreco(),
                quantidade,
                estoqueMinimo,
                estoqueBaixo,
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