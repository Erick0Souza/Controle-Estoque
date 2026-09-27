package com.erick.estoque.movimentacao;

import com.erick.estoque.auditoria.AuditoriaService;
import com.erick.estoque.auditoria.TipoAcaoAuditoria;
import com.erick.estoque.produto.Produto;
import com.erick.estoque.produto.ProdutoRepository;
import com.erick.estoque.security.UserEntity;
import com.erick.estoque.security.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MovimentacaoService {

    private final MovimentacaoRepository movimentacaoRepository;
    private final ProdutoRepository produtoRepository;
    private final UserRepository userRepository;
    private final AuditoriaService auditoriaService;

    public MovimentacaoService(
            MovimentacaoRepository movimentacaoRepository,
            ProdutoRepository produtoRepository,
            UserRepository userRepository,
            AuditoriaService auditoriaService
    ) {
        this.movimentacaoRepository =
                movimentacaoRepository;

        this.produtoRepository =
                produtoRepository;

        this.userRepository =
                userRepository;

        this.auditoriaService =
                auditoriaService;
    }

    @Transactional
    public MovimentacaoResponse movimentar(
            MovimentacaoRequest request
    ) {

        Produto produto =
                produtoRepository
                        .findById(
                                request.produtoId()
                        )
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Produto não encontrado"
                                        )
                        );

        UserEntity usuario =
                obterUsuarioAutenticado();

        int estoqueAtual =
                produto.getQuantidade();

        if (
                request.tipo()
                        == TipoMovimentacao.ENTRADA
        ) {

            estoqueAtual =
                    estoqueAtual
                            + request.quantidade();

        } else if (
                request.tipo()
                        == TipoMovimentacao.SAIDA
        ) {

            if (
                    estoqueAtual
                            < request.quantidade()
            ) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Estoque insuficiente"
                );
            }

            estoqueAtual =
                    estoqueAtual
                            - request.quantidade();
        }

        produto.setQuantidade(
                estoqueAtual
        );

        produtoRepository.save(
                produto
        );

        MovimentacaoEstoque movimentacao =
                new MovimentacaoEstoque();

        movimentacao.setProduto(
                produto
        );

        movimentacao.setTipo(
                request.tipo()
        );

        movimentacao.setQuantidade(
                request.quantidade()
        );

        movimentacao.setObservacao(
                normalizarObservacao(
                        request.observacao()
                )
        );

        movimentacao.setDataHora(
                LocalDateTime.now()
        );

        movimentacao.setResponsavelNome(
                usuario.getNomeUsuario()
        );

        movimentacao.setResponsavelEmail(
                usuario.getEmail()
        );

        movimentacao.setResponsavelPerfil(
                usuario.getPerfil()
        );

        MovimentacaoEstoque salva =
                movimentacaoRepository.save(
                        movimentacao
                );

        registrarAuditoriaMovimentacao(
                salva,
                estoqueAtual
        );

        return toResponse(
                salva
        );
    }

    public List<MovimentacaoResponse> listar() {

        return movimentacaoRepository
                .findAll()
                .stream()
                .map(
                        this::toResponse
                )
                .toList();
    }

    public List<MovimentacaoResponse> listarPorProduto(
            Long produtoId
    ) {

        if (
                !produtoRepository.existsById(
                        produtoId
                )
        ) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Produto não encontrado"
            );
        }

        return movimentacaoRepository
                .findByProdutoIdOrderByDataHoraDesc(
                        produtoId
                )
                .stream()
                .map(
                        this::toResponse
                )
                .toList();
    }

    private UserEntity obterUsuarioAutenticado() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (
                authentication == null
                        || !authentication.isAuthenticated()
                        || authentication.getName() == null
        ) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Usuário não autenticado"
            );
        }

        String email =
                authentication
                        .getName()
                        .trim()
                        .toLowerCase();

        return userRepository
                .findByEmail(
                        email
                )
                .orElseThrow(
                        () ->
                                new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "Usuário autenticado não encontrado"
                                )
                );
    }

    private String normalizarObservacao(
            String observacao
    ) {

        if (
                observacao == null
        ) {
            return null;
        }

        String observacaoTratada =
                observacao.trim();

        if (
                observacaoTratada.isBlank()
        ) {
            return null;
        }

        return observacaoTratada;
    }

    private void registrarAuditoriaMovimentacao(
            MovimentacaoEstoque movimentacao,
            int estoqueAtual
    ) {

        TipoAcaoAuditoria acao;

        if (
                movimentacao.getTipo()
                        == TipoMovimentacao.ENTRADA
        ) {

            acao =
                    TipoAcaoAuditoria
                            .MOVIMENTACAO_ENTRADA;

        } else {

            acao =
                    TipoAcaoAuditoria
                            .MOVIMENTACAO_SAIDA;
        }

        Produto produto =
                movimentacao.getProduto();

        String descricao =
                "Movimentação de "
                        + movimentacao
                        .getTipo()
                        .name()
                        .toLowerCase()
                        + " de "
                        + movimentacao.getQuantidade()
                        + " unidade(s) no produto \""
                        + produto.getNome()
                        + "\". Estoque resultante: "
                        + estoqueAtual;

        auditoriaService.registrar(
                acao,
                "MOVIMENTACAO",
                movimentacao.getId(),
                descricao
        );
    }

    private MovimentacaoResponse toResponse(
            MovimentacaoEstoque movimentacao
    ) {

        Produto produto =
                movimentacao.getProduto();

        return new MovimentacaoResponse(

                movimentacao.getId(),

                produto.getId(),

                produto.getNome(),

                movimentacao.getTipo(),

                movimentacao.getQuantidade(),

                produto.getQuantidade(),

                movimentacao.getResponsavelNome(),

                movimentacao.getResponsavelEmail(),

                movimentacao.getResponsavelPerfil(),

                movimentacao.getObservacao(),

                movimentacao.getDataHora()
        );
    }
}