package com.erick.estoque.dashboard;

import com.erick.estoque.auditoria.TipoAcaoAuditoria;
import com.erick.estoque.security.PerfilUsuario;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record DashboardResponse(

        long totalProdutos,

        long totalCategorias,

        long totalUsuarios,

        long totalMovimentacoes,

        long totalEntradas,

        long totalSaidas,

        long produtosEstoqueBaixo,

        long totalUnidadesEstoque,

        BigDecimal valorTotalEstoque,

        List<AtividadeRecenteResponse> atividadesRecentes

) {

    public record AtividadeRecenteResponse(

            Long id,

            LocalDateTime dataHora,

            String usuarioNome,

            String usuarioEmail,

            PerfilUsuario usuarioPerfil,

            TipoAcaoAuditoria acao,

            String entidade,

            Long entidadeId,

            String descricao

    ) {
    }
}