package com.erick.estoque;

import com.erick.estoque.auditoria.AuditoriaRepository;
import com.erick.estoque.auditoria.AuditoriaService;
import com.erick.estoque.auditoria.TipoAcaoAuditoria;
import com.erick.estoque.categoria.Categoria;
import com.erick.estoque.categoria.CategoriaRepository;
import com.erick.estoque.exportacao.ExportacaoExcelService;
import com.erick.estoque.exportacao.ExportacaoPdfService;
import com.erick.estoque.movimentacao.MovimentacaoEstoque;
import com.erick.estoque.movimentacao.MovimentacaoRepository;
import com.erick.estoque.movimentacao.TipoMovimentacao;
import com.erick.estoque.produto.Produto;
import com.erick.estoque.produto.ProdutoRepository;
import com.erick.estoque.security.PerfilUsuario;
import com.erick.estoque.security.UserEntity;
import com.erick.estoque.security.UserRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(
        username = "admin@teste.com",
        roles = "ADMIN"
)
class AdminDashboardRelatoriosExportacoesTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private MovimentacaoRepository movimentacaoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditoriaRepository auditoriaRepository;

    @Autowired
    private AuditoriaService auditoriaService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ExportacaoExcelService exportacaoExcelService;

    @Autowired
    private ExportacaoPdfService exportacaoPdfService;


    @BeforeEach
    void prepararBanco() {

        auditoriaRepository.deleteAll();

        movimentacaoRepository.deleteAll();

        produtoRepository.deleteAll();

        categoriaRepository.deleteAll();

        userRepository.deleteAll();


        criarUsuarioAdmin();
    }


    @Test
    void deveRetornarResumoCorretoNoDashboard()
            throws Exception {

        Categoria categoria =
                criarCategoria(
                        "Informática"
                );


        Produto produtoBaixo =
                criarProduto(
                        "SSD-001",
                        "SSD",
                        new BigDecimal("50.00"),
                        3,
                        5,
                        categoria
                );


        Produto produtoNormal =
                criarProduto(
                        "RAM-001",
                        "Memória",
                        new BigDecimal("100.00"),
                        10,
                        2,
                        categoria
                );


        criarMovimentacao(
                produtoBaixo,
                TipoMovimentacao.ENTRADA,
                5,
                LocalDateTime.of(
                        2026,
                        9,
                        20,
                        10,
                        0
                )
        );


        criarMovimentacao(
                produtoNormal,
                TipoMovimentacao.SAIDA,
                2,
                LocalDateTime.of(
                        2026,
                        9,
                        21,
                        10,
                        0
                )
        );


        auditoriaService.registrar(
                TipoAcaoAuditoria.PRODUTO_CRIADO,
                "PRODUTO",
                produtoBaixo.getId(),
                "Produto criado para teste"
        );


        mockMvc.perform(
                        get("/admin/dashboard")
                )

                .andExpect(
                        status().isOk()
                )

                .andExpect(
                        jsonPath("$.totalProdutos")
                                .value(
                                        2
                                )
                )

                .andExpect(
                        jsonPath("$.totalCategorias")
                                .value(
                                        1
                                )
                )

                .andExpect(
                        jsonPath("$.totalUsuarios")
                                .value(
                                        1
                                )
                )

                .andExpect(
                        jsonPath("$.totalMovimentacoes")
                                .value(
                                        2
                                )
                )

                .andExpect(
                        jsonPath("$.totalEntradas")
                                .value(
                                        1
                                )
                )

                .andExpect(
                        jsonPath("$.totalSaidas")
                                .value(
                                        1
                                )
                )

                .andExpect(
                        jsonPath("$.produtosEstoqueBaixo")
                                .value(
                                        1
                                )
                )

                .andExpect(
                        jsonPath("$.totalUnidadesEstoque")
                                .value(
                                        13
                                )
                )

                .andExpect(
                        jsonPath("$.valorTotalEstoque")
                                .value(
                                        1150.0
                                )
                )

                .andExpect(
                        jsonPath(
                                "$.atividadesRecentes",
                                hasSize(1)
                        )
                )

                .andExpect(
                        jsonPath(
                                "$.atividadesRecentes[0].acao"
                        )
                                .value(
                                        "PRODUTO_CRIADO"
                                )
                )

                .andExpect(
                        jsonPath(
                                "$.atividadesRecentes[0].usuarioEmail"
                        )
                                .value(
                                        "admin@teste.com"
                                )
                );
    }


    @Test
    @WithMockUser(
            username = "operador@teste.com",
            roles = "OPERADOR"
    )
    void operadorNaoPodeAcessarDashboard()
            throws Exception {

        mockMvc.perform(
                        get("/admin/dashboard")
                )

                .andExpect(
                        status().isForbidden()
                );
    }


    @Test
    void deveGerarRelatorioCompletoDeEstoque()
            throws Exception {

        Categoria categoria =
                criarCategoria(
                        "Periféricos"
                );


        criarProduto(
                "MOU-001",
                "Mouse",
                new BigDecimal("50.00"),
                3,
                5,
                categoria
        );


        criarProduto(
                "TEC-001",
                "Teclado",
                new BigDecimal("100.00"),
                10,
                2,
                categoria
        );


        mockMvc.perform(
                        get(
                                "/admin/relatorios/estoque"
                        )
                )

                .andExpect(
                        status().isOk()
                )

                .andExpect(
                        jsonPath("$.totalProdutos")
                                .value(
                                        2
                                )
                )

                .andExpect(
                        jsonPath("$.totalUnidades")
                                .value(
                                        13
                                )
                )

                .andExpect(
                        jsonPath("$.produtosEstoqueBaixo")
                                .value(
                                        1
                                )
                )

                .andExpect(
                        jsonPath("$.valorTotalEstoque")
                                .value(
                                        1150.0
                                )
                )

                .andExpect(
                        jsonPath(
                                "$.produtos",
                                hasSize(2)
                        )
                )

                .andExpect(
                        jsonPath("$.produtos[0].nome")
                                .value(
                                        "Mouse"
                                )
                )

                .andExpect(
                        jsonPath("$.produtos[1].nome")
                                .value(
                                        "Teclado"
                                )
                );
    }


    @Test
    void deveFiltrarRelatorioSomentePorEstoqueBaixo()
            throws Exception {

        Categoria categoria =
                criarCategoria(
                        "Periféricos"
                );


        criarProduto(
                "MOU-001",
                "Mouse",
                new BigDecimal("50.00"),
                3,
                5,
                categoria
        );


        criarProduto(
                "TEC-001",
                "Teclado",
                new BigDecimal("100.00"),
                10,
                2,
                categoria
        );


        mockMvc.perform(
                        get(
                                "/admin/relatorios/estoque"
                        )
                                .param(
                                        "somenteEstoqueBaixo",
                                        "true"
                                )
                )

                .andExpect(
                        status().isOk()
                )

                .andExpect(
                        jsonPath("$.totalProdutos")
                                .value(
                                        2
                                )
                )

                .andExpect(
                        jsonPath("$.produtosEstoqueBaixo")
                                .value(
                                        1
                                )
                )

                .andExpect(
                        jsonPath(
                                "$.produtos",
                                hasSize(1)
                        )
                )

                .andExpect(
                        jsonPath("$.produtos[0].sku")
                                .value(
                                        "MOU-001"
                                )
                )

                .andExpect(
                        jsonPath("$.produtos[0].estoqueBaixo")
                                .value(
                                        true
                                )
                );
    }


    @Test
    void deveFiltrarRelatorioDeMovimentacoesPorPeriodoETipo()
            throws Exception {

        Categoria categoria =
                criarCategoria(
                        "Informática"
                );


        Produto produto =
                criarProduto(
                        "PROD-001",
                        "Produto",
                        new BigDecimal("100.00"),
                        20,
                        5,
                        categoria
                );


        criarMovimentacao(
                produto,
                TipoMovimentacao.ENTRADA,
                5,
                LocalDateTime.of(
                        2026,
                        9,
                        10,
                        10,
                        0
                )
        );


        criarMovimentacao(
                produto,
                TipoMovimentacao.SAIDA,
                2,
                LocalDateTime.of(
                        2026,
                        9,
                        15,
                        10,
                        0
                )
        );


        criarMovimentacao(
                produto,
                TipoMovimentacao.ENTRADA,
                7,
                LocalDateTime.of(
                        2026,
                        9,
                        20,
                        10,
                        0
                )
        );


        mockMvc.perform(
                        get(
                                "/admin/relatorios/movimentacoes"
                        )
                                .param(
                                        "dataInicio",
                                        "2026-09-12T00:00:00"
                                )
                                .param(
                                        "dataFim",
                                        "2026-09-30T23:59:59"
                                )
                                .param(
                                        "tipo",
                                        "ENTRADA"
                                )
                )

                .andExpect(
                        status().isOk()
                )

                .andExpect(
                        jsonPath("$.totalMovimentacoes")
                                .value(
                                        1
                                )
                )

                .andExpect(
                        jsonPath("$.totalEntradas")
                                .value(
                                        1
                                )
                )

                .andExpect(
                        jsonPath("$.totalSaidas")
                                .value(
                                        0
                                )
                )

                .andExpect(
                        jsonPath("$.unidadesEntrada")
                                .value(
                                        7
                                )
                )

                .andExpect(
                        jsonPath("$.unidadesSaida")
                                .value(
                                        0
                                )
                )

                .andExpect(
                        jsonPath(
                                "$.movimentacoes",
                                hasSize(1)
                        )
                )

                .andExpect(
                        jsonPath(
                                "$.movimentacoes[0].tipo"
                        )
                                .value(
                                        "ENTRADA"
                                )
                )

                .andExpect(
                        jsonPath(
                                "$.movimentacoes[0].quantidade"
                        )
                                .value(
                                        7
                                )
                );
    }


    @Test
    void deveRejeitarPeriodoInvalidoNoRelatorioDeMovimentacoes()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/admin/relatorios/movimentacoes"
                        )
                                .param(
                                        "dataInicio",
                                        "2026-09-30T10:00:00"
                                )
                                .param(
                                        "dataFim",
                                        "2026-09-01T10:00:00"
                                )
                )

                .andExpect(
                        status().isBadRequest()
                )

                .andExpect(
                        jsonPath("$.mensagem")
                                .value(
                                        "A data inicial não pode ser posterior à data final"
                                )
                );
    }


    @Test
    void deveExportarEstoqueEmCsv()
            throws Exception {

        Categoria categoria =
                criarCategoria(
                        "Periféricos"
                );


        criarProduto(
                "TEC-001",
                "Teclado",
                new BigDecimal("199.90"),
                10,
                5,
                categoria
        );


        mockMvc.perform(
                        get(
                                "/admin/exportacoes/estoque/csv"
                        )
                )

                .andExpect(
                        status().isOk()
                )

                .andExpect(
                        content()
                                .contentTypeCompatibleWith(
                                        MediaType.parseMediaType(
                                                "text/csv;charset=UTF-8"
                                        )
                                )
                )

                .andExpect(
                        header()
                                .string(
                                        HttpHeaders.CONTENT_DISPOSITION,
                                        containsString(
                                                "relatorio-estoque.csv"
                                        )
                                )
                )

                .andExpect(
                        content()
                                .string(
                                        containsString(
                                                "RELATORIO DE ESTOQUE"
                                        )
                                )
                )

                .andExpect(
                        content()
                                .string(
                                        containsString(
                                                "TEC-001"
                                        )
                                )
                )

                .andExpect(
                        content()
                                .string(
                                        containsString(
                                                "Teclado"
                                        )
                                )
                );
    }


    @Test
    void deveExportarMovimentacoesEmCsvAplicandoFiltro()
            throws Exception {

        Categoria categoria =
                criarCategoria(
                        "Informática"
                );


        Produto produto =
                criarProduto(
                        "SSD-001",
                        "SSD",
                        new BigDecimal("300.00"),
                        10,
                        2,
                        categoria
                );


        criarMovimentacao(
                produto,
                TipoMovimentacao.ENTRADA,
                5,
                LocalDateTime.of(
                        2026,
                        9,
                        20,
                        10,
                        0
                )
        );


        criarMovimentacao(
                produto,
                TipoMovimentacao.SAIDA,
                2,
                LocalDateTime.of(
                        2026,
                        9,
                        21,
                        10,
                        0
                )
        );


        mockMvc.perform(
                        get(
                                "/admin/exportacoes/movimentacoes/csv"
                        )
                                .param(
                                        "tipo",
                                        "SAIDA"
                                )
                )

                .andExpect(
                        status().isOk()
                )

                .andExpect(
                        content()
                                .contentTypeCompatibleWith(
                                        MediaType.parseMediaType(
                                                "text/csv;charset=UTF-8"
                                        )
                                )
                )

                .andExpect(
                        header()
                                .string(
                                        HttpHeaders.CONTENT_DISPOSITION,
                                        containsString(
                                                "relatorio-movimentacoes.csv"
                                        )
                                )
                )

                .andExpect(
                        content()
                                .string(
                                        containsString(
                                                "RELATORIO DE MOVIMENTACOES"
                                        )
                                )
                )

                .andExpect(
                        content()
                                .string(
                                        containsString(
                                                "Total de movimentacoes;1"
                                        )
                                )
                )

                .andExpect(
                        content()
                                .string(
                                        containsString(
                                                "Total de saidas;1"
                                        )
                                )
                );
    }


    @Test
    void deveRejeitarPeriodoInvalidoNaExportacaoCsv()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/admin/exportacoes/movimentacoes/csv"
                        )
                                .param(
                                        "dataInicio",
                                        "2026-09-30T10:00:00"
                                )
                                .param(
                                        "dataFim",
                                        "2026-09-01T10:00:00"
                                )
                )

                .andExpect(
                        status().isBadRequest()
                )

                .andExpect(
                        jsonPath("$.mensagem")
                                .value(
                                        "A data inicial não pode ser posterior à data final"
                                )
                );
    }


    @Test
    @WithMockUser(
            username = "operador@teste.com",
            roles = "OPERADOR"
    )
    void operadorNaoPodeAcessarExportacoesAdministrativas()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/admin/exportacoes/estoque/csv"
                        )
                )

                .andExpect(
                        status().isForbidden()
                );
    }


    @Test
    void deveGerarExcelDeEstoqueValido()
            throws Exception {

        Categoria categoria =
                criarCategoria(
                        "Informática"
                );


        criarProduto(
                "SSD-001",
                "SSD",
                new BigDecimal("300.00"),
                4,
                2,
                categoria
        );


        criarProduto(
                "RAM-001",
                "Memória RAM",
                new BigDecimal("200.00"),
                8,
                3,
                categoria
        );


        byte[] arquivo =
                exportacaoExcelService
                        .exportarEstoque(
                                false
                        );


        assertTrue(
                arquivo.length > 0
        );


        try (
                Workbook workbook =
                        new XSSFWorkbook(
                                new ByteArrayInputStream(
                                        arquivo
                                )
                        )
        ) {

            Sheet sheet =
                    workbook.getSheet(
                            "Estoque"
                    );


            assertNotNull(
                    sheet
            );


            assertEquals(
                    "SKU",
                    sheet
                            .getRow(0)
                            .getCell(1)
                            .getStringCellValue()
            );


            assertEquals(
                    3,
                    sheet.getPhysicalNumberOfRows()
            );
        }
    }


    @Test
    void deveGerarExcelDeMovimentacoesValido()
            throws Exception {

        Categoria categoria =
                criarCategoria(
                        "Informática"
                );


        Produto produto =
                criarProduto(
                        "PROD-001",
                        "Produto",
                        new BigDecimal("100.00"),
                        10,
                        2,
                        categoria
                );


        criarMovimentacao(
                produto,
                TipoMovimentacao.ENTRADA,
                5,
                LocalDateTime.of(
                        2026,
                        9,
                        20,
                        10,
                        0
                )
        );


        criarMovimentacao(
                produto,
                TipoMovimentacao.SAIDA,
                2,
                LocalDateTime.of(
                        2026,
                        9,
                        21,
                        10,
                        0
                )
        );


        byte[] arquivo =
                exportacaoExcelService
                        .exportarMovimentacoes(
                                null,
                                null,
                                null
                        );


        assertTrue(
                arquivo.length > 0
        );


        try (
                Workbook workbook =
                        new XSSFWorkbook(
                                new ByteArrayInputStream(
                                        arquivo
                                )
                        )
        ) {

            Sheet sheet =
                    workbook.getSheet(
                            "Movimentações"
                    );


            assertNotNull(
                    sheet
            );


            assertEquals(
                    "Produto",
                    sheet
                            .getRow(0)
                            .getCell(2)
                            .getStringCellValue()
            );


            assertEquals(
                    3,
                    sheet.getPhysicalNumberOfRows()
            );
        }
    }


    @Test
    void deveGerarPdfsValidosMesmoSemDados()
            throws Exception {

        byte[] estoquePdf =
                exportacaoPdfService
                        .exportarEstoque(
                                false
                        );


        byte[] movimentacoesPdf =
                exportacaoPdfService
                        .exportarMovimentacoes(
                                null,
                                null,
                                null
                        );


        assertTrue(
                estoquePdf.length > 0
        );

        assertTrue(
                movimentacoesPdf.length > 0
        );


        try (
                PDDocument documento =
                        Loader.loadPDF(
                                estoquePdf
                        )
        ) {

            assertTrue(
                    documento.getNumberOfPages()
                            >= 1
            );
        }


        try (
                PDDocument documento =
                        Loader.loadPDF(
                                movimentacoesPdf
                        )
        ) {

            assertTrue(
                    documento.getNumberOfPages()
                            >= 1
            );
        }
    }


    private Categoria criarCategoria(
            String nome
    ) {

        return categoriaRepository.save(
                new Categoria(
                        nome
                )
        );
    }


    private Produto criarProduto(
            String sku,
            String nome,
            BigDecimal preco,
            Integer quantidade,
            Integer estoqueMinimo,
            Categoria categoria
    ) {

        Produto produto =
                new Produto();

        produto.setSku(
                sku
        );

        produto.setNome(
                nome
        );

        produto.setPreco(
                preco
        );

        produto.setQuantidade(
                quantidade
        );

        produto.setEstoqueMinimo(
                estoqueMinimo
        );

        produto.setCategoria(
                categoria
        );

        produto.setDescricao(
                "Produto de teste"
        );


        return produtoRepository.save(
                produto
        );
    }


    private MovimentacaoEstoque criarMovimentacao(
            Produto produto,
            TipoMovimentacao tipo,
            Integer quantidade,
            LocalDateTime dataHora
    ) {

        MovimentacaoEstoque movimentacao =
                new MovimentacaoEstoque();

        movimentacao.setProduto(
                produto
        );

        movimentacao.setTipo(
                tipo
        );

        movimentacao.setQuantidade(
                quantidade
        );

        movimentacao.setDataHora(
                dataHora
        );

        movimentacao.setResponsavelNome(
                "Administrador"
        );

        movimentacao.setResponsavelEmail(
                "admin@teste.com"
        );

        movimentacao.setResponsavelPerfil(
                PerfilUsuario.ADMIN
        );

        movimentacao.setObservacao(
                "Movimentação de teste"
        );


        return movimentacaoRepository.save(
                movimentacao
        );
    }


    private UserEntity criarUsuarioAdmin() {

        UserEntity usuario =
                new UserEntity();

        usuario.setNomeUsuario(
                "Administrador"
        );

        usuario.setEmail(
                "admin@teste.com"
        );

        usuario.setSenha(
                passwordEncoder.encode(
                        "Senha@123"
                )
        );

        usuario.setPerfil(
                PerfilUsuario.ADMIN
        );


        return userRepository.save(
                usuario
        );
    }
}