let previewObjectUrl = null;

let paginaAtualProdutos = 0;
let totalPaginasProdutos = 0;

let paginaAtualUsuarios = 0;
let totalPaginasUsuarios = 0;

const TIPOS_IMAGEM_PERMITIDOS = [
    "image/jpeg",
    "image/png",
    "image/webp"
];

const TAMANHO_MAXIMO_IMAGEM =
    5 * 1024 * 1024;


function obterPerfilUsuario() {
    return (
        sessionStorage.getItem("perfil") ||
        "CONSULTA"
    ).toUpperCase();
}


function obterNomeUsuario() {
    return (
        sessionStorage.getItem("nomeUsuario") ||
        sessionStorage.getItem("email") ||
        "Usuário"
    );
}


function obterEmailUsuario() {
    return (
        sessionStorage.getItem("email") ||
        ""
    ).toLowerCase();
}


function ehAdmin() {
    return obterPerfilUsuario() === "ADMIN";
}


function podeEditarProdutos() {
    const perfil =
        obterPerfilUsuario();

    return perfil === "ADMIN"
        || perfil === "OPERADOR";
}


function podeExcluirProdutos() {
    return ehAdmin();
}


function podeMovimentarEstoque() {
    const perfil =
        obterPerfilUsuario();

    return perfil === "ADMIN"
        || perfil === "OPERADOR";
}


function aplicarClassePerfil(
    elemento,
    perfil
) {
    if (!elemento) {
        return;
    }

    elemento.classList.remove(
        "perfil-admin",
        "perfil-operador",
        "perfil-consulta"
    );

    if (perfil === "ADMIN") {
        elemento.classList.add(
            "perfil-admin"
        );

        return;
    }

    if (perfil === "OPERADOR") {
        elemento.classList.add(
            "perfil-operador"
        );

        return;
    }

    elemento.classList.add(
        "perfil-consulta"
    );
}


function aplicarPermissoesInterface() {
    const perfil =
        obterPerfilUsuario();

    const nomeUsuario =
        obterNomeUsuario();

    const usuarioLogado =
        document.getElementById(
            "usuario-logado"
        );

    const perfilUsuario =
        document.getElementById(
            "perfil-usuario"
        );

    const responsavelMovimentacao =
        document.getElementById(
            "responsavel-movimentacao-atual"
        );

    const produtoFormSection =
        document.getElementById(
            "produto-form-section"
        );

    const movimentacaoSection =
        document.getElementById(
            "movimentacao-section"
        );

    const adminUsuariosSection =
        document.getElementById(
            "admin-usuarios-section"
        );

    if (usuarioLogado) {
        usuarioLogado.textContent =
            nomeUsuario;
    }

    if (perfilUsuario) {
        perfilUsuario.textContent =
            perfil;

        aplicarClassePerfil(
            perfilUsuario,
            perfil
        );
    }

    if (responsavelMovimentacao) {
        responsavelMovimentacao.textContent =
            nomeUsuario;
    }

    if (produtoFormSection) {

        if (podeEditarProdutos()) {
            produtoFormSection
                .classList
                .remove("hidden");
        } else {
            produtoFormSection
                .classList
                .add("hidden");
        }
    }

    if (movimentacaoSection) {

        if (podeMovimentarEstoque()) {
            movimentacaoSection
                .classList
                .remove("hidden");
        } else {
            movimentacaoSection
                .classList
                .add("hidden");
        }
    }

    if (adminUsuariosSection) {

        if (ehAdmin()) {
            adminUsuariosSection
                .classList
                .remove("hidden");
        } else {
            adminUsuariosSection
                .classList
                .add("hidden");
        }
    }
}


function mostrarCadastro() {
    document
        .getElementById("form-login")
        .classList
        .add("hidden");

    document
        .getElementById("form-cadastro")
        .classList
        .remove("hidden");

    document
        .getElementById("login-mensagem")
        .textContent = "";
}


function mostrarLogin() {
    document
        .getElementById("form-cadastro")
        .classList
        .add("hidden");

    document
        .getElementById("form-login")
        .classList
        .remove("hidden");

    document
        .getElementById("cadastro-mensagem")
        .textContent = "";
}


async function cadastrarUsuario() {
    const nomeUsuario =
        document
            .getElementById(
                "cadastro-nome-usuario"
            )
            .value
            .trim();

    const email =
        document
            .getElementById("cadastro-email")
            .value
            .trim();

    const senha =
        document
            .getElementById("cadastro-senha")
            .value;

    const confirmarSenha =
        document
            .getElementById(
                "cadastro-confirmar-senha"
            )
            .value;

    const mensagem =
        document.getElementById(
            "cadastro-mensagem"
        );

    if (
        !nomeUsuario ||
        !email ||
        !senha ||
        !confirmarSenha
    ) {
        mensagem.textContent =
            "Preencha todos os campos.";

        return;
    }

    if (
        nomeUsuario.length < 3 ||
        nomeUsuario.length > 50
    ) {
        mensagem.textContent =
            "O nome de usuário deve ter entre 3 e 50 caracteres.";

        return;
    }

    if (senha.length < 6) {
        mensagem.textContent =
            "A senha deve ter pelo menos 6 caracteres.";

        return;
    }

    if (senha !== confirmarSenha) {
        mensagem.textContent =
            "As senhas não são iguais.";

        return;
    }

    mensagem.textContent =
        "Criando conta...";

    try {
        const resposta =
            await fetch(
                "/auth/register",
                {
                    method: "POST",

                    headers: {
                        "Content-Type":
                            "application/json"
                    },

                    body: JSON.stringify({
                        nomeUsuario:
                        nomeUsuario,

                        email:
                        email,

                        senha:
                        senha
                    })
                }
            );

        let dados = {};

        try {
            dados =
                await resposta.json();
        } catch (erro) {
            dados = {};
        }

        if (!resposta.ok) {
            mensagem.textContent =
                dados.mensagem ||
                dados.message ||
                dados.detail ||
                "Não foi possível criar a conta.";

            return;
        }

        mensagem.textContent =
            "Conta criada com sucesso. Perfil inicial: "
            + (
                dados.perfil ||
                "CONSULTA"
            )
            + ".";

        document
            .getElementById(
                "cadastro-nome-usuario"
            )
            .value = "";

        document
            .getElementById("cadastro-email")
            .value = "";

        document
            .getElementById("cadastro-senha")
            .value = "";

        document
            .getElementById(
                "cadastro-confirmar-senha"
            )
            .value = "";

        document
            .getElementById("email")
            .value =
            email;

        setTimeout(
            () => {
                mostrarLogin();

                document
                    .getElementById(
                        "login-mensagem"
                    )
                    .textContent =
                    "Conta criada. Faça login.";
            },
            900
        );

    } catch (erro) {
        console.error(
            "Erro ao cadastrar usuário:",
            erro
        );

        mensagem.textContent =
            "Erro ao conectar com o servidor.";
    }
}


async function login() {
    const email =
        document
            .getElementById("email")
            .value
            .trim();

    const senha =
        document
            .getElementById("senha")
            .value;

    const mensagem =
        document.getElementById(
            "login-mensagem"
        );

    if (
        !email ||
        !senha
    ) {
        mensagem.textContent =
            "Preencha email e senha.";

        return;
    }

    mensagem.textContent =
        "Entrando...";

    try {
        const resposta =
            await fetch(
                "/auth/login",
                {
                    method: "POST",

                    headers: {
                        "Content-Type":
                            "application/json"
                    },

                    body: JSON.stringify({
                        email: email,
                        senha: senha
                    })
                }
            );

        let dados = {};

        try {
            dados =
                await resposta.json();
        } catch (erro) {
            dados = {};
        }

        if (!resposta.ok) {
            mensagem.textContent =
                dados.mensagem ||
                dados.message ||
                dados.detail ||
                "Email ou senha inválidos.";

            return;
        }

        sessionStorage.setItem(
            "token",
            dados.token
        );

        sessionStorage.setItem(
            "email",
            dados.email || email
        );

        sessionStorage.setItem(
            "nomeUsuario",
            dados.nomeUsuario ||
            dados.email ||
            email
        );

        sessionStorage.setItem(
            "perfil",
            dados.perfil ||
            "CONSULTA"
        );

        mensagem.textContent = "";

        mostrarSistema();

        await carregarCategorias();

        paginaAtualProdutos = 0;

        await carregarProdutos();

        if (
            podeMovimentarEstoque()
        ) {
            await carregarProdutosMovimentacao();
        }

        await carregarHistorico();

        if (ehAdmin()) {
            paginaAtualUsuarios = 0;

            await carregarUsuariosAdmin(
                0
            );
        }

    } catch (erro) {
        console.error(
            "Erro no login:",
            erro
        );

        mensagem.textContent =
            "Erro ao conectar com o servidor.";
    }
}


function mostrarSistema() {
    document
        .getElementById("login-section")
        .classList
        .add("hidden");

    document
        .getElementById("sistema")
        .classList
        .remove("hidden");

    aplicarPermissoesInterface();
}


function logout() {
    sessionStorage.removeItem("token");
    sessionStorage.removeItem("email");
    sessionStorage.removeItem("nomeUsuario");
    sessionStorage.removeItem("perfil");

    paginaAtualProdutos = 0;
    totalPaginasProdutos = 0;

    paginaAtualUsuarios = 0;
    totalPaginasUsuarios = 0;

    document
        .getElementById("sistema")
        .classList
        .add("hidden");

    document
        .getElementById("login-section")
        .classList
        .remove("hidden");

    document
        .getElementById("produtos")
        .innerHTML = "";

    document
        .getElementById(
            "historico-movimentacoes"
        )
        .innerHTML = "";

    const listaUsuarios =
        document.getElementById(
            "admin-usuarios-lista"
        );

    if (listaUsuarios) {
        listaUsuarios.innerHTML = "";
    }

    const contadorUsuarios =
        document.getElementById(
            "admin-usuarios-contador"
        );

    if (contadorUsuarios) {
        contadorUsuarios.textContent = "";
    }

    limparFormularioProduto();
    limparFormularioMovimentacao();
    mostrarLogin();
}


function montarParametrosUsuarios(
    pagina
) {
    const params =
        new URLSearchParams();

    const buscaElemento =
        document.getElementById(
            "buscaUsuario"
        );

    const perfilElemento =
        document.getElementById(
            "filtroPerfilUsuario"
        );

    const ordenacaoElemento =
        document.getElementById(
            "ordenacaoUsuario"
        );

    const direcaoElemento =
        document.getElementById(
            "direcaoUsuario"
        );

    const tamanhoElemento =
        document.getElementById(
            "tamanhoPaginaUsuario"
        );

    const busca =
        buscaElemento
            ? buscaElemento.value.trim()
            : "";

    const perfil =
        perfilElemento
            ? perfilElemento.value
            : "";

    const sort =
        ordenacaoElemento
            ? ordenacaoElemento.value
            : "nomeUsuario";

    const direction =
        direcaoElemento
            ? direcaoElemento.value
            : "asc";

    const size =
        tamanhoElemento
            ? tamanhoElemento.value
            : "10";

    if (busca) {
        params.set(
            "busca",
            busca
        );
    }

    if (perfil) {
        params.set(
            "perfil",
            perfil
        );
    }

    params.set(
        "page",
        pagina
    );

    params.set(
        "size",
        size
    );

    params.set(
        "sort",
        sort
    );

    params.set(
        "direction",
        direction
    );

    return params;
}


async function carregarUsuariosAdmin(
    pagina = paginaAtualUsuarios
) {
    if (!ehAdmin()) {
        return;
    }

    const token =
        sessionStorage.getItem(
            "token"
        );

    const lista =
        document.getElementById(
            "admin-usuarios-lista"
        );

    const mensagem =
        document.getElementById(
            "admin-usuarios-mensagem"
        );

    if (!lista) {
        return;
    }

    lista.innerHTML =
        "<p>Carregando usuários...</p>";

    if (mensagem) {
        mensagem.textContent = "";
    }

    const params =
        montarParametrosUsuarios(
            pagina
        );

    try {
        const resposta =
            await fetch(
                "/admin/usuarios?"
                + params.toString(),
                {
                    headers: {
                        "Authorization":
                            "Bearer "
                            + token
                    }
                }
            );

        if (
            resposta.status === 401
        ) {
            logout();
            return;
        }

        let dados = {};

        try {
            dados =
                await resposta.json();
        } catch (erro) {
            dados = {};
        }

        if (
            resposta.status === 403
        ) {
            lista.innerHTML = "";

            if (mensagem) {
                mensagem.textContent =
                    dados.mensagem ||
                    "Você não possui permissão para acessar esta área.";
            }

            return;
        }

        if (!resposta.ok) {
            lista.innerHTML = "";

            if (mensagem) {
                mensagem.textContent =
                    dados.mensagem ||
                    dados.message ||
                    dados.detail ||
                    "Não foi possível carregar os usuários.";
            }

            return;
        }

        const usuarios =
            dados.content || [];

        if (
            usuarios.length === 0 &&
            pagina > 0 &&
            dados.totalPages > 0
        ) {
            paginaAtualUsuarios =
                dados.totalPages - 1;

            await carregarUsuariosAdmin(
                paginaAtualUsuarios
            );

            return;
        }

        paginaAtualUsuarios =
            dados.number || 0;

        totalPaginasUsuarios =
            dados.totalPages || 0;

        mostrarUsuariosAdmin(
            usuarios
        );

        atualizarPaginacaoUsuarios(
            dados
        );

    } catch (erro) {
        console.error(
            "Erro ao carregar usuários:",
            erro
        );

        lista.innerHTML = "";

        if (mensagem) {
            mensagem.textContent =
                "Erro ao processar a lista de usuários.";
        }
    }
}


function atualizarPaginacaoUsuarios(
    dados
) {
    const contador =
        document.getElementById(
            "admin-usuarios-contador"
        );

    const info =
        document.getElementById(
            "admin-pagina-info"
        );

    const anterior =
        document.getElementById(
            "admin-pagina-anterior"
        );

    const proxima =
        document.getElementById(
            "admin-pagina-proxima"
        );

    const total =
        dados.totalElements || 0;

    if (contador) {
        contador.textContent =
            total
            + (
                total === 1
                    ? " usuário encontrado"
                    : " usuários encontrados"
            );
    }

    if (
        !info ||
        !anterior ||
        !proxima
    ) {
        return;
    }

    if (
        !dados.totalPages ||
        dados.totalPages === 0
    ) {
        info.textContent =
            "Nenhum usuário encontrado";

        anterior.disabled = true;
        proxima.disabled = true;

        return;
    }

    info.textContent =
        "Página "
        + (dados.number + 1)
        + " de "
        + dados.totalPages;

    anterior.disabled =
        dados.first === true;

    proxima.disabled =
        dados.last === true;
}


async function aplicarFiltrosUsuarios() {
    if (!ehAdmin()) {
        return;
    }

    paginaAtualUsuarios = 0;

    await carregarUsuariosAdmin(
        0
    );
}


async function limparFiltrosUsuarios() {
    const busca =
        document.getElementById(
            "buscaUsuario"
        );

    const perfil =
        document.getElementById(
            "filtroPerfilUsuario"
        );

    const ordenacao =
        document.getElementById(
            "ordenacaoUsuario"
        );

    const direcao =
        document.getElementById(
            "direcaoUsuario"
        );

    const tamanho =
        document.getElementById(
            "tamanhoPaginaUsuario"
        );

    if (busca) {
        busca.value = "";
    }

    if (perfil) {
        perfil.value = "";
    }

    if (ordenacao) {
        ordenacao.value =
            "nomeUsuario";
    }

    if (direcao) {
        direcao.value =
            "asc";
    }

    if (tamanho) {
        tamanho.value =
            "10";
    }

    const mensagem =
        document.getElementById(
            "admin-usuarios-mensagem"
        );

    if (mensagem) {
        mensagem.textContent = "";
    }

    paginaAtualUsuarios = 0;

    await carregarUsuariosAdmin(
        0
    );
}


async function mudarPaginaUsuarios(
    direcao
) {
    const novaPagina =
        paginaAtualUsuarios
        + direcao;

    if (
        novaPagina < 0 ||
        novaPagina >= totalPaginasUsuarios
    ) {
        return;
    }

    paginaAtualUsuarios =
        novaPagina;

    await carregarUsuariosAdmin(
        paginaAtualUsuarios
    );

    const secao =
        document.getElementById(
            "admin-usuarios-section"
        );

    if (secao) {
        secao.scrollIntoView({
            behavior: "smooth",
            block: "start"
        });
    }
}


function mostrarUsuariosAdmin(
    usuarios
) {
    const lista =
        document.getElementById(
            "admin-usuarios-lista"
        );

    lista.innerHTML = "";

    if (
        !usuarios ||
        usuarios.length === 0
    ) {
        lista.innerHTML =
            "<p>Nenhum usuário encontrado.</p>";

        return;
    }

    const emailLogado =
        obterEmailUsuario();

    usuarios.forEach(
        usuario => {
            const card =
                document.createElement(
                    "div"
                );

            card.classList.add(
                "usuario-admin-card"
            );

            const cabecalho =
                document.createElement(
                    "div"
                );

            cabecalho.classList.add(
                "usuario-admin-cabecalho"
            );

            const dados =
                document.createElement(
                    "div"
                );

            const nome =
                document.createElement(
                    "h3"
                );

            nome.textContent =
                usuario.nomeUsuario ||
                usuario.email;

            const email =
                document.createElement(
                    "p"
                );

            email.textContent =
                usuario.email;

            dados.appendChild(
                nome
            );

            dados.appendChild(
                email
            );

            const badge =
                document.createElement(
                    "span"
                );

            badge.classList.add(
                "badge-perfil"
            );

            badge.textContent =
                usuario.perfil;

            aplicarClassePerfil(
                badge,
                usuario.perfil
            );

            cabecalho.appendChild(
                dados
            );

            cabecalho.appendChild(
                badge
            );

            card.appendChild(
                cabecalho
            );

            const proprioUsuario =
                usuario.email
                    .toLowerCase()
                === emailLogado;

            if (proprioUsuario) {
                const aviso =
                    document.createElement(
                        "div"
                    );

                aviso.classList.add(
                    "usuario-proprio-aviso"
                );

                aviso.textContent =
                    "Esta é sua conta administrativa.";

                card.appendChild(
                    aviso
                );
            }

            const areaPerfil =
                document.createElement(
                    "div"
                );

            areaPerfil.classList.add(
                "usuario-admin-perfil"
            );

            const label =
                document.createElement(
                    "label"
                );

            label.textContent =
                "Perfil";

            const select =
                document.createElement(
                    "select"
                );

            select.id =
                "perfil-usuario-"
                + usuario.id;

            [
                "CONSULTA",
                "OPERADOR",
                "ADMIN"
            ].forEach(
                perfil => {
                    const option =
                        document.createElement(
                            "option"
                        );

                    option.value =
                        perfil;

                    option.textContent =
                        perfil;

                    if (
                        perfil ===
                        usuario.perfil
                    ) {
                        option.selected =
                            true;
                    }

                    select.appendChild(
                        option
                    );
                }
            );

            if (proprioUsuario) {
                select.disabled =
                    true;
            }

            areaPerfil.appendChild(
                label
            );

            areaPerfil.appendChild(
                select
            );

            card.appendChild(
                areaPerfil
            );

            if (!proprioUsuario) {
                const botao =
                    document.createElement(
                        "button"
                    );

                botao.type =
                    "button";

                botao.textContent =
                    "Salvar perfil";

                botao.onclick =
                    () =>
                        alterarPerfilUsuario(
                            usuario.id,
                            usuario.nomeUsuario ||
                            usuario.email
                        );

                card.appendChild(
                    botao
                );
            }

            lista.appendChild(
                card
            );
        }
    );
}


async function alterarPerfilUsuario(
    usuarioId,
    nomeUsuario
) {
    if (!ehAdmin()) {
        return;
    }

    const select =
        document.getElementById(
            "perfil-usuario-"
            + usuarioId
        );

    if (!select) {
        return;
    }

    const novoPerfil =
        select.value;

    const confirmar =
        confirm(
            `Deseja alterar o perfil de "${nomeUsuario}" para ${novoPerfil}?`
        );

    if (!confirmar) {
        await carregarUsuariosAdmin(
            paginaAtualUsuarios
        );

        return;
    }

    const token =
        sessionStorage.getItem(
            "token"
        );

    const mensagem =
        document.getElementById(
            "admin-usuarios-mensagem"
        );

    mensagem.textContent =
        "Atualizando perfil...";

    try {
        const resposta =
            await fetch(
                "/admin/usuarios/"
                + usuarioId
                + "/perfil",
                {
                    method: "PUT",

                    headers: {
                        "Content-Type":
                            "application/json",

                        "Authorization":
                            "Bearer "
                            + token
                    },

                    body: JSON.stringify({
                        perfil:
                        novoPerfil
                    })
                }
            );

        if (
            resposta.status === 401
        ) {
            logout();
            return;
        }

        let dados = {};

        try {
            dados =
                await resposta.json();
        } catch (erro) {
            dados = {};
        }

        if (
            resposta.status === 403
        ) {
            mensagem.textContent =
                dados.mensagem ||
                "Você não possui permissão para alterar usuários.";

            await carregarUsuariosAdmin(
                paginaAtualUsuarios
            );

            return;
        }

        if (!resposta.ok) {
            mensagem.textContent =
                dados.mensagem ||
                dados.message ||
                dados.detail ||
                "Não foi possível alterar o perfil.";

            await carregarUsuariosAdmin(
                paginaAtualUsuarios
            );

            return;
        }

        mensagem.textContent =
            "Perfil de "
            + (
                dados.nomeUsuario ||
                dados.email
            )
            + " atualizado para "
            + dados.perfil
            + ".";

        await carregarUsuariosAdmin(
            paginaAtualUsuarios
        );

    } catch (erro) {
        console.error(
            "Erro ao alterar perfil:",
            erro
        );

        mensagem.textContent =
            "Erro ao conectar com o servidor.";
    }
}


async function carregarCategorias() {
    const token =
        sessionStorage.getItem("token");

    const selectCadastro =
        document.getElementById(
            "categoriaId"
        );

    const selectFiltro =
        document.getElementById(
            "filtroCategoriaId"
        );

    try {
        const resposta =
            await fetch(
                "/categorias",
                {
                    headers: {
                        "Authorization":
                            "Bearer "
                            + token
                    }
                }
            );

        if (
            resposta.status === 401
        ) {
            logout();
            return;
        }

        if (!resposta.ok) {
            return;
        }

        const categorias =
            await resposta.json();

        selectCadastro.innerHTML =
            '<option value="">Selecione uma categoria</option>';

        selectFiltro.innerHTML =
            '<option value="">Todas</option>';

        categorias.forEach(
            categoria => {
                const optionCadastro =
                    document.createElement(
                        "option"
                    );

                optionCadastro.value =
                    categoria.id;

                optionCadastro.textContent =
                    categoria.nome;

                selectCadastro.appendChild(
                    optionCadastro
                );

                const optionFiltro =
                    document.createElement(
                        "option"
                    );

                optionFiltro.value =
                    categoria.id;

                optionFiltro.textContent =
                    categoria.nome;

                selectFiltro.appendChild(
                    optionFiltro
                );
            }
        );

    } catch (erro) {
        console.error(
            "Erro ao carregar categorias:",
            erro
        );
    }
}


function montarParametrosProdutos(
    pagina
) {
    const params =
        new URLSearchParams();

    const nome =
        document
            .getElementById("filtroNome")
            .value
            .trim();

    const sku =
        document
            .getElementById("filtroSku")
            .value
            .trim();

    const categoriaId =
        document
            .getElementById(
                "filtroCategoriaId"
            )
            .value;

    const precoMin =
        document
            .getElementById(
                "filtroPrecoMin"
            )
            .value;

    const precoMax =
        document
            .getElementById(
                "filtroPrecoMax"
            )
            .value;

    const quantidadeMin =
        document
            .getElementById(
                "filtroQuantidadeMin"
            )
            .value;

    const quantidadeMax =
        document
            .getElementById(
                "filtroQuantidadeMax"
            )
            .value;

    const sort =
        document
            .getElementById(
                "ordenacaoProduto"
            )
            .value;

    const direction =
        document
            .getElementById(
                "direcaoProduto"
            )
            .value;

    const size =
        document
            .getElementById(
                "tamanhoPaginaProduto"
            )
            .value;

    if (nome) {
        params.set(
            "nome",
            nome
        );
    }

    if (sku) {
        params.set(
            "sku",
            sku
        );
    }

    if (categoriaId) {
        params.set(
            "categoriaId",
            categoriaId
        );
    }

    if (precoMin !== "") {
        params.set(
            "precoMin",
            precoMin
        );
    }

    if (precoMax !== "") {
        params.set(
            "precoMax",
            precoMax
        );
    }

    if (quantidadeMin !== "") {
        params.set(
            "quantidadeMin",
            quantidadeMin
        );
    }

    if (quantidadeMax !== "") {
        params.set(
            "quantidadeMax",
            quantidadeMax
        );
    }

    params.set(
        "page",
        pagina
    );

    params.set(
        "size",
        size
    );

    params.set(
        "sort",
        sort
    );

    params.set(
        "direction",
        direction
    );

    return params;
}


async function carregarProdutos(
    pagina = paginaAtualProdutos
) {
    const token =
        sessionStorage.getItem("token");

    const lista =
        document.getElementById(
            "produtos"
        );

    const mensagem =
        document.getElementById(
            "filtro-mensagem"
        );

    lista.innerHTML =
        "<p>Carregando produtos...</p>";

    mensagem.textContent = "";

    const params =
        montarParametrosProdutos(
            pagina
        );

    try {
        const resposta =
            await fetch(
                "/produtos?"
                + params.toString(),
                {
                    headers: {
                        "Authorization":
                            "Bearer "
                            + token
                    }
                }
            );

        if (
            resposta.status === 401
        ) {
            logout();
            return;
        }

        let dados = {};

        try {
            dados =
                await resposta.json();
        } catch (erro) {
            dados = {};
        }

        if (!resposta.ok) {
            lista.innerHTML =
                "<p>Não foi possível carregar os produtos.</p>";

            mensagem.textContent =
                dados.mensagem ||
                dados.message ||
                dados.detail ||
                "Erro ao aplicar os filtros.";

            return;
        }

        const produtos =
            dados.content || [];

        if (
            produtos.length === 0 &&
            pagina > 0 &&
            dados.totalPages > 0
        ) {
            paginaAtualProdutos =
                dados.totalPages - 1;

            await carregarProdutos(
                paginaAtualProdutos
            );

            return;
        }

        paginaAtualProdutos =
            dados.number || 0;

        totalPaginasProdutos =
            dados.totalPages || 0;

        mostrarProdutos(
            produtos
        );

        atualizarPaginacaoProdutos(
            dados
        );

    } catch (erro) {
        console.error(
            "Erro ao carregar produtos:",
            erro
        );

        lista.innerHTML =
            "<p>Erro ao conectar com a API.</p>";
    }
}


function atualizarPaginacaoProdutos(
    dados
) {
    const info =
        document.getElementById(
            "paginaInfo"
        );

    const anterior =
        document.getElementById(
            "botaoPaginaAnterior"
        );

    const proxima =
        document.getElementById(
            "botaoPaginaProxima"
        );

    const totalElementos =
        dados.totalElements || 0;

    if (
        !dados.totalPages ||
        dados.totalPages === 0
    ) {
        info.textContent =
            "Nenhum produto encontrado";

        anterior.disabled = true;
        proxima.disabled = true;

        return;
    }

    info.textContent =
        "Página "
        + (dados.number + 1)
        + " de "
        + dados.totalPages
        + " • "
        + totalElementos
        + (
            totalElementos === 1
                ? " produto"
                : " produtos"
        );

    anterior.disabled =
        dados.first === true;

    proxima.disabled =
        dados.last === true;
}


async function mudarPaginaProdutos(
    direcao
) {
    const novaPagina =
        paginaAtualProdutos
        + direcao;

    if (
        novaPagina < 0 ||
        novaPagina >= totalPaginasProdutos
    ) {
        return;
    }

    paginaAtualProdutos =
        novaPagina;

    await carregarProdutos(
        paginaAtualProdutos
    );

    document
        .getElementById("produtos")
        .scrollIntoView({
            behavior: "smooth",
            block: "start"
        });
}


async function aplicarFiltrosProdutos() {
    const mensagem =
        document.getElementById(
            "filtro-mensagem"
        );

    const precoMin =
        document
            .getElementById(
                "filtroPrecoMin"
            )
            .value;

    const precoMax =
        document
            .getElementById(
                "filtroPrecoMax"
            )
            .value;

    const quantidadeMin =
        document
            .getElementById(
                "filtroQuantidadeMin"
            )
            .value;

    const quantidadeMax =
        document
            .getElementById(
                "filtroQuantidadeMax"
            )
            .value;

    if (
        precoMin !== "" &&
        precoMax !== "" &&
        Number(precoMin) >
        Number(precoMax)
    ) {
        mensagem.textContent =
            "O preço mínimo não pode ser maior que o preço máximo.";

        return;
    }

    if (
        quantidadeMin !== "" &&
        quantidadeMax !== "" &&
        Number(quantidadeMin) >
        Number(quantidadeMax)
    ) {
        mensagem.textContent =
            "O estoque mínimo da busca não pode ser maior que o estoque máximo.";

        return;
    }

    paginaAtualProdutos = 0;

    await carregarProdutos(
        0
    );
}


async function limparFiltrosProdutos() {
    document
        .getElementById("filtroNome")
        .value = "";

    document
        .getElementById("filtroSku")
        .value = "";

    document
        .getElementById(
            "filtroCategoriaId"
        )
        .value = "";

    document
        .getElementById(
            "filtroPrecoMin"
        )
        .value = "";

    document
        .getElementById(
            "filtroPrecoMax"
        )
        .value = "";

    document
        .getElementById(
            "filtroQuantidadeMin"
        )
        .value = "";

    document
        .getElementById(
            "filtroQuantidadeMax"
        )
        .value = "";

    document
        .getElementById(
            "ordenacaoProduto"
        )
        .value = "nome";

    document
        .getElementById(
            "direcaoProduto"
        )
        .value = "asc";

    document
        .getElementById(
            "tamanhoPaginaProduto"
        )
        .value = "10";

    document
        .getElementById(
            "filtro-mensagem"
        )
        .textContent = "";

    paginaAtualProdutos = 0;

    await carregarProdutos(
        0
    );
}


function mostrarProdutos(
    produtos
) {
    const lista =
        document.getElementById(
            "produtos"
        );

    lista.innerHTML = "";

    if (
        !produtos ||
        produtos.length === 0
    ) {
        lista.innerHTML =
            "<p>Nenhum produto encontrado.</p>";

        return;
    }

    produtos.forEach(
        produto => {
            const card =
                document.createElement(
                    "div"
                );

            card.classList.add(
                "produto"
            );

            if (
                produto.estoqueBaixo
            ) {
                card.classList.add(
                    "produto-estoque-baixo"
                );
            }

            if (produto.imagemUrl) {
                const imagem =
                    document.createElement(
                        "img"
                    );

                imagem.src =
                    produto.imagemUrl;

                imagem.alt =
                    "Imagem de "
                    + produto.nome;

                imagem.classList.add(
                    "produto-imagem"
                );

                imagem.loading =
                    "lazy";

                imagem.onerror =
                    () => {
                        imagem.style.display =
                            "none";

                        const semImagem =
                            criarPlaceholderImagem();

                        card.insertBefore(
                            semImagem,
                            card.firstChild
                        );
                    };

                card.appendChild(
                    imagem
                );

            } else {
                card.appendChild(
                    criarPlaceholderImagem()
                );
            }

            if (
                produto.estoqueBaixo
            ) {
                const alerta =
                    document.createElement(
                        "div"
                    );

                alerta.classList.add(
                    "alerta-estoque-baixo"
                );

                alerta.textContent =
                    "⚠ Estoque baixo";

                card.appendChild(
                    alerta
                );
            }

            const titulo =
                document.createElement(
                    "h3"
                );

            titulo.textContent =
                produto.nome;

            const sku =
                document.createElement(
                    "p"
                );

            sku.textContent =
                "SKU: "
                + (
                    produto.sku ||
                    "Não definido"
                );

            const identificador =
                document.createElement(
                    "p"
                );

            identificador.textContent =
                "ID: "
                + produto.id;

            const preco =
                document.createElement(
                    "p"
                );

            preco.textContent =
                "Preço: R$ "
                + Number(
                    produto.preco
                ).toFixed(2);

            const quantidade =
                document.createElement(
                    "p"
                );

            quantidade.textContent =
                "Quantidade atual: "
                + produto.quantidade;

            const estoqueMinimo =
                document.createElement(
                    "p"
                );

            estoqueMinimo.textContent =
                "Estoque mínimo: "
                + (
                    produto.estoqueMinimo
                    ?? 0
                );

            const categoria =
                document.createElement(
                    "p"
                );

            categoria.textContent =
                "Categoria: "
                + (
                    produto.categoriaNome ||
                    "Sem categoria"
                );

            const descricao =
                document.createElement(
                    "p"
                );

            descricao.textContent =
                "Descrição: "
                + (
                    produto.descricao ||
                    "Sem descrição"
                );

            card.appendChild(
                titulo
            );

            card.appendChild(
                sku
            );

            card.appendChild(
                identificador
            );

            card.appendChild(
                preco
            );

            card.appendChild(
                quantidade
            );

            card.appendChild(
                estoqueMinimo
            );

            card.appendChild(
                categoria
            );

            card.appendChild(
                descricao
            );

            if (
                podeEditarProdutos()
                || podeExcluirProdutos()
            ) {
                const acoes =
                    document.createElement(
                        "div"
                    );

                acoes.classList.add(
                    "acoes"
                );

                if (
                    podeEditarProdutos()
                ) {
                    const botaoEditar =
                        document.createElement(
                            "button"
                        );

                    botaoEditar.type =
                        "button";

                    botaoEditar.textContent =
                        "Editar";

                    botaoEditar.onclick =
                        () =>
                            prepararEdicao(
                                produto
                            );

                    acoes.appendChild(
                        botaoEditar
                    );
                }

                if (
                    podeExcluirProdutos()
                ) {
                    const botaoExcluir =
                        document.createElement(
                            "button"
                        );

                    botaoExcluir.type =
                        "button";

                    botaoExcluir.textContent =
                        "Excluir";

                    botaoExcluir.classList.add(
                        "botao-excluir"
                    );

                    botaoExcluir.onclick =
                        () =>
                            excluirProduto(
                                produto.id,
                                produto.nome
                            );

                    acoes.appendChild(
                        botaoExcluir
                    );
                }

                card.appendChild(
                    acoes
                );
            }

            lista.appendChild(
                card
            );
        }
    );
}


function criarPlaceholderImagem() {
    const semImagem =
        document.createElement(
            "div"
        );

    semImagem.classList.add(
        "produto-sem-imagem"
    );

    semImagem.textContent =
        "Sem imagem";

    return semImagem;
}


async function carregarProdutosMovimentacao() {
    if (
        !podeMovimentarEstoque()
    ) {
        return;
    }

    const token =
        sessionStorage.getItem(
            "token"
        );

    const produtos = [];

    let pagina = 0;
    let totalPaginas = 1;

    try {
        while (
            pagina < totalPaginas
            ) {
            const resposta =
                await fetch(
                    "/produtos?page="
                    + pagina
                    + "&size=100&sort=nome&direction=asc",
                    {
                        headers: {
                            "Authorization":
                                "Bearer "
                                + token
                        }
                    }
                );

            if (
                resposta.status === 401
            ) {
                logout();
                return;
            }

            if (!resposta.ok) {
                return;
            }

            const dados =
                await resposta.json();

            produtos.push(
                ...(
                    dados.content ||
                    []
                )
            );

            totalPaginas =
                dados.totalPages || 0;

            pagina++;
        }

        preencherProdutosMovimentacao(
            produtos
        );

    } catch (erro) {
        console.error(
            "Erro ao carregar produtos para movimentação:",
            erro
        );
    }
}


function preencherProdutosMovimentacao(
    produtos
) {
    const select =
        document.getElementById(
            "movimentacaoProdutoId"
        );

    const valorAtual =
        select.value;

    select.innerHTML =
        '<option value="">Selecione um produto</option>';

    produtos.forEach(
        produto => {
            const option =
                document.createElement(
                    "option"
                );

            option.value =
                produto.id;

            option.textContent =
                (
                    produto.sku
                        ? produto.sku
                        + " - "
                        : ""
                )
                + produto.nome
                + " - estoque: "
                + produto.quantidade
                + (
                    produto.estoqueBaixo
                        ? " - ⚠ baixo"
                        : ""
                );

            select.appendChild(
                option
            );
        }
    );

    if (
        valorAtual &&
        produtos.some(
            produto =>
                String(produto.id) ===
                String(valorAtual)
        )
    ) {
        select.value =
            valorAtual;
    }
}


async function salvarProduto() {
    if (
        !podeEditarProdutos()
    ) {
        alert(
            "Seu perfil não possui permissão para cadastrar ou editar produtos."
        );

        return;
    }

    const produtoId =
        document
            .getElementById(
                "produtoIdEdicao"
            )
            .value;

    if (produtoId) {
        await editarProduto(
            produtoId
        );
    } else {
        await criarProduto();
    }
}


function obterSkuFormulario() {
    return document
        .getElementById("sku")
        .value
        .trim()
        .toUpperCase();
}


function validarSku(
    sku
) {
    if (!sku) {
        return {
            valida: false,
            mensagem:
                "O SKU é obrigatório."
        };
    }

    if (
        sku.length > 50
    ) {
        return {
            valida: false,
            mensagem:
                "O SKU deve ter no máximo 50 caracteres."
        };
    }

    const padrao =
        /^[A-Za-z0-9_-]+$/;

    if (
        !padrao.test(
            sku
        )
    ) {
        return {
            valida: false,
            mensagem:
                "O SKU deve conter apenas letras, números, hífen ou underline."
        };
    }

    return {
        valida: true,
        mensagem: ""
    };
}


async function criarProduto() {
    if (
        !podeEditarProdutos()
    ) {
        return;
    }

    const token =
        sessionStorage.getItem(
            "token"
        );

    const sku =
        obterSkuFormulario();

    const nome =
        document
            .getElementById("nome")
            .value
            .trim();

    const preco =
        document
            .getElementById("preco")
            .value;

    const quantidade =
        document
            .getElementById("quantidade")
            .value;

    const estoqueMinimo =
        document
            .getElementById("estoqueMinimo")
            .value;

    const categoriaId =
        document
            .getElementById("categoriaId")
            .value;

    const descricao =
        document
            .getElementById("descricao")
            .value
            .trim();

    const imagem =
        obterImagemSelecionada();

    const mensagem =
        document.getElementById(
            "produto-mensagem"
        );

    const validacaoSku =
        validarSku(
            sku
        );

    if (
        !validacaoSku.valida
    ) {
        mensagem.textContent =
            validacaoSku.mensagem;

        return;
    }

    if (
        !nome ||
        preco === "" ||
        quantidade === "" ||
        estoqueMinimo === "" ||
        categoriaId === ""
    ) {
        mensagem.textContent =
            "Preencha os campos obrigatórios.";

        return;
    }

    if (
        Number(preco) < 0
    ) {
        mensagem.textContent =
            "O preço não pode ser negativo.";

        return;
    }

    if (
        Number(quantidade) < 0
    ) {
        mensagem.textContent =
            "A quantidade não pode ser negativa.";

        return;
    }

    if (
        Number(estoqueMinimo) < 0
    ) {
        mensagem.textContent =
            "O estoque mínimo não pode ser negativo.";

        return;
    }

    if (imagem) {
        const validacao =
            validarImagem(
                imagem
            );

        if (
            !validacao.valida
        ) {
            mensagem.textContent =
                validacao.mensagem;

            return;
        }
    }

    mensagem.textContent =
        "Cadastrando produto...";

    try {
        const resposta =
            await fetch(
                "/produtos",
                {
                    method: "POST",

                    headers: {
                        "Content-Type":
                            "application/json",

                        "Authorization":
                            "Bearer "
                            + token
                    },

                    body: JSON.stringify({
                        sku:
                        sku,

                        nome:
                        nome,

                        preco:
                            Number(
                                preco
                            ),

                        quantidade:
                            Number(
                                quantidade
                            ),

                        estoqueMinimo:
                            Number(
                                estoqueMinimo
                            ),

                        categoriaId:
                            Number(
                                categoriaId
                            ),

                        descricao:
                        descricao
                    })
                }
            );

        if (
            resposta.status === 401
        ) {
            logout();
            return;
        }

        let dados = {};

        try {
            dados =
                await resposta.json();
        } catch (erro) {
            dados = {};
        }

        if (
            resposta.status === 403
        ) {
            mensagem.textContent =
                dados.mensagem ||
                "Você não possui permissão para cadastrar produtos.";

            return;
        }

        if (!resposta.ok) {
            mensagem.textContent =
                dados.mensagem ||
                dados.message ||
                dados.detail ||
                "Erro ao cadastrar produto.";

            return;
        }

        if (imagem) {
            mensagem.textContent =
                "Produto criado. Enviando imagem...";

            try {
                await enviarImagemProduto(
                    dados.id,
                    imagem
                );

            } catch (erro) {
                console.error(
                    erro
                );

                mensagem.textContent =
                    "Produto cadastrado, mas não foi possível enviar a imagem.";

                await carregarProdutos();
                await carregarProdutosMovimentacao();

                return;
            }
        }

        limparFormularioProduto();

        mensagem.textContent =
            "Produto cadastrado com sucesso!";

        paginaAtualProdutos = 0;

        await carregarProdutos(
            0
        );

        await carregarProdutosMovimentacao();

    } catch (erro) {
        console.error(
            "Erro ao cadastrar produto:",
            erro
        );

        mensagem.textContent =
            "Erro ao conectar com o servidor.";
    }
}


function prepararEdicao(
    produto
) {
    if (
        !podeEditarProdutos()
    ) {
        alert(
            "Seu perfil não possui permissão para editar produtos."
        );

        return;
    }

    limparPreviewImagem();

    document
        .getElementById(
            "produtoIdEdicao"
        )
        .value =
        produto.id;

    document
        .getElementById("sku")
        .value =
        produto.sku || "";

    document
        .getElementById("nome")
        .value =
        produto.nome;

    document
        .getElementById("preco")
        .value =
        produto.preco;

    document
        .getElementById("quantidade")
        .value =
        produto.quantidade;

    document
        .getElementById("estoqueMinimo")
        .value =
        produto.estoqueMinimo ?? 0;

    document
        .getElementById("categoriaId")
        .value =
        produto.categoriaId;

    document
        .getElementById("descricao")
        .value =
        produto.descricao || "";

    document
        .getElementById("imagemUrlAtual")
        .value =
        produto.imagemUrl || "";

    document
        .getElementById("imagemProduto")
        .value = "";

    document
        .getElementById("botaoSalvar")
        .textContent =
        "Salvar alterações";

    document
        .getElementById("botaoCancelar")
        .classList
        .remove("hidden");

    document
        .getElementById("titulo-formulario")
        .textContent =
        "Editar Produto";

    document
        .getElementById("produto-mensagem")
        .textContent = "";

    if (
        produto.imagemUrl
    ) {
        mostrarImagemAtual(
            produto.imagemUrl
        );
    }

    document
        .getElementById(
            "produto-form-section"
        )
        .scrollIntoView({
            behavior: "smooth",
            block: "start"
        });
}


async function editarProduto(
    id
) {
    if (
        !podeEditarProdutos()
    ) {
        return;
    }

    const token =
        sessionStorage.getItem(
            "token"
        );

    const sku =
        obterSkuFormulario();

    const nome =
        document
            .getElementById("nome")
            .value
            .trim();

    const preco =
        document
            .getElementById("preco")
            .value;

    const quantidade =
        document
            .getElementById("quantidade")
            .value;

    const estoqueMinimo =
        document
            .getElementById("estoqueMinimo")
            .value;

    const categoriaId =
        document
            .getElementById("categoriaId")
            .value;

    const descricao =
        document
            .getElementById("descricao")
            .value
            .trim();

    const imagem =
        obterImagemSelecionada();

    const mensagem =
        document.getElementById(
            "produto-mensagem"
        );

    const validacaoSku =
        validarSku(
            sku
        );

    if (
        !validacaoSku.valida
    ) {
        mensagem.textContent =
            validacaoSku.mensagem;

        return;
    }

    if (
        !nome ||
        preco === "" ||
        quantidade === "" ||
        estoqueMinimo === "" ||
        categoriaId === ""
    ) {
        mensagem.textContent =
            "Preencha os campos obrigatórios.";

        return;
    }

    if (
        Number(preco) < 0
    ) {
        mensagem.textContent =
            "O preço não pode ser negativo.";

        return;
    }

    if (
        Number(quantidade) < 0
    ) {
        mensagem.textContent =
            "A quantidade não pode ser negativa.";

        return;
    }

    if (
        Number(estoqueMinimo) < 0
    ) {
        mensagem.textContent =
            "O estoque mínimo não pode ser negativo.";

        return;
    }

    if (imagem) {
        const validacao =
            validarImagem(
                imagem
            );

        if (
            !validacao.valida
        ) {
            mensagem.textContent =
                validacao.mensagem;

            return;
        }
    }

    mensagem.textContent =
        "Salvando alterações...";

    try {
        const resposta =
            await fetch(
                "/produtos/"
                + id,
                {
                    method: "PUT",

                    headers: {
                        "Content-Type":
                            "application/json",

                        "Authorization":
                            "Bearer "
                            + token
                    },

                    body: JSON.stringify({
                        sku:
                        sku,

                        nome:
                        nome,

                        preco:
                            Number(
                                preco
                            ),

                        quantidade:
                            Number(
                                quantidade
                            ),

                        estoqueMinimo:
                            Number(
                                estoqueMinimo
                            ),

                        categoriaId:
                            Number(
                                categoriaId
                            ),

                        descricao:
                        descricao
                    })
                }
            );

        if (
            resposta.status === 401
        ) {
            logout();
            return;
        }

        let dados = {};

        try {
            dados =
                await resposta.json();
        } catch (erro) {
            dados = {};
        }

        if (
            resposta.status === 403
        ) {
            mensagem.textContent =
                dados.mensagem ||
                "Você não possui permissão para editar produtos.";

            return;
        }

        if (!resposta.ok) {
            mensagem.textContent =
                dados.mensagem ||
                dados.message ||
                dados.detail ||
                "Erro ao editar produto.";

            return;
        }

        if (imagem) {
            mensagem.textContent =
                "Produto atualizado. Enviando imagem...";

            try {
                await enviarImagemProduto(
                    id,
                    imagem
                );

            } catch (erro) {
                console.error(
                    erro
                );

                mensagem.textContent =
                    "Produto atualizado, mas não foi possível enviar a nova imagem.";

                await carregarProdutos();
                await carregarProdutosMovimentacao();

                return;
            }
        }

        cancelarEdicao();

        mensagem.textContent =
            "Produto atualizado com sucesso!";

        await carregarProdutos();
        await carregarProdutosMovimentacao();

    } catch (erro) {
        console.error(
            "Erro ao editar produto:",
            erro
        );

        mensagem.textContent =
            "Erro ao conectar com o servidor.";
    }
}


async function enviarImagemProduto(
    produtoId,
    arquivo
) {
    const token =
        sessionStorage.getItem(
            "token"
        );

    const formData =
        new FormData();

    formData.append(
        "imagem",
        arquivo
    );

    const resposta =
        await fetch(
            "/produtos/"
            + produtoId
            + "/imagem",
            {
                method: "POST",

                headers: {
                    "Authorization":
                        "Bearer "
                        + token
                },

                body:
                formData
            }
        );

    if (
        resposta.status === 401
    ) {
        logout();

        throw new Error(
            "Sessão expirada"
        );
    }

    let dados = {};

    try {
        dados =
            await resposta.json();
    } catch (erro) {
        dados = {};
    }

    if (!resposta.ok) {
        throw new Error(
            dados.mensagem ||
            dados.message ||
            dados.detail ||
            "Erro ao enviar imagem"
        );
    }

    return dados;
}


async function removerImagemProduto() {
    if (
        !podeEditarProdutos()
    ) {
        return;
    }

    const inputImagem =
        document.getElementById(
            "imagemProduto"
        );

    const produtoId =
        document
            .getElementById(
                "produtoIdEdicao"
            )
            .value;

    const imagemUrlAtual =
        document
            .getElementById(
                "imagemUrlAtual"
            )
            .value;

    const mensagem =
        document.getElementById(
            "produto-mensagem"
        );

    if (
        inputImagem.files &&
        inputImagem.files.length > 0
    ) {
        inputImagem.value = "";

        if (imagemUrlAtual) {
            mostrarImagemAtual(
                imagemUrlAtual
            );
        } else {
            limparPreviewImagem();
        }

        mensagem.textContent =
            "Nova imagem removida da seleção.";

        return;
    }

    if (
        !produtoId ||
        !imagemUrlAtual
    ) {
        limparPreviewImagem();

        mensagem.textContent =
            "Nenhuma imagem para remover.";

        return;
    }

    const confirmar =
        confirm(
            "Deseja remover a imagem atual deste produto?"
        );

    if (!confirmar) {
        return;
    }

    const token =
        sessionStorage.getItem(
            "token"
        );

    try {
        const resposta =
            await fetch(
                "/produtos/"
                + produtoId
                + "/imagem",
                {
                    method: "DELETE",

                    headers: {
                        "Authorization":
                            "Bearer "
                            + token
                    }
                }
            );

        if (
            resposta.status === 401
        ) {
            logout();
            return;
        }

        if (!resposta.ok) {
            let dados = {};

            try {
                dados =
                    await resposta.json();
            } catch (erro) {
                dados = {};
            }

            mensagem.textContent =
                dados.mensagem ||
                dados.message ||
                dados.detail ||
                "Não foi possível remover a imagem.";

            return;
        }

        document
            .getElementById(
                "imagemUrlAtual"
            )
            .value = "";

        limparPreviewImagem();

        mensagem.textContent =
            "Imagem removida com sucesso.";

        await carregarProdutos();

    } catch (erro) {
        console.error(
            "Erro ao remover imagem:",
            erro
        );

        mensagem.textContent =
            "Erro ao conectar com o servidor.";
    }
}


function obterImagemSelecionada() {
    const input =
        document.getElementById(
            "imagemProduto"
        );

    if (
        !input.files ||
        input.files.length === 0
    ) {
        return null;
    }

    return input.files[0];
}


function validarImagem(
    arquivo
) {
    if (
        !TIPOS_IMAGEM_PERMITIDOS
            .includes(
                arquivo.type
            )
    ) {
        return {
            valida: false,
            mensagem:
                "Formato inválido. Use JPG, PNG ou WEBP."
        };
    }

    if (
        arquivo.size >
        TAMANHO_MAXIMO_IMAGEM
    ) {
        return {
            valida: false,
            mensagem:
                "A imagem deve ter no máximo 5 MB."
        };
    }

    return {
        valida: true,
        mensagem: ""
    };
}


function mostrarPreviewArquivo(
    arquivo
) {
    limparPreviewObjectUrl();

    previewObjectUrl =
        URL.createObjectURL(
            arquivo
        );

    const container =
        document.getElementById(
            "previewImagemContainer"
        );

    const imagem =
        document.getElementById(
            "previewImagemProduto"
        );

    const botaoRemover =
        document.getElementById(
            "botaoRemoverImagem"
        );

    imagem.src =
        previewObjectUrl;

    container.classList.remove(
        "hidden"
    );

    botaoRemover.classList.remove(
        "hidden"
    );
}


function mostrarImagemAtual(
    imagemUrl
) {
    limparPreviewObjectUrl();

    const container =
        document.getElementById(
            "previewImagemContainer"
        );

    const imagem =
        document.getElementById(
            "previewImagemProduto"
        );

    const botaoRemover =
        document.getElementById(
            "botaoRemoverImagem"
        );

    imagem.src =
        imagemUrl;

    container.classList.remove(
        "hidden"
    );

    botaoRemover.classList.remove(
        "hidden"
    );
}


function limparPreviewImagem() {
    limparPreviewObjectUrl();

    const container =
        document.getElementById(
            "previewImagemContainer"
        );

    const imagem =
        document.getElementById(
            "previewImagemProduto"
        );

    const botaoRemover =
        document.getElementById(
            "botaoRemoverImagem"
        );

    imagem.src = "";

    container.classList.add(
        "hidden"
    );

    botaoRemover.classList.add(
        "hidden"
    );
}


function limparPreviewObjectUrl() {
    if (previewObjectUrl) {
        URL.revokeObjectURL(
            previewObjectUrl
        );

        previewObjectUrl = null;
    }
}


function cancelarEdicao() {
    limparFormularioProduto();

    document
        .getElementById(
            "botaoSalvar"
        )
        .textContent =
        "Cadastrar";

    document
        .getElementById(
            "botaoCancelar"
        )
        .classList
        .add("hidden");

    document
        .getElementById(
            "titulo-formulario"
        )
        .textContent =
        "Novo Produto";
}


async function excluirProduto(
    id,
    nome
) {
    if (
        !podeExcluirProdutos()
    ) {
        alert(
            "Somente administradores podem excluir produtos."
        );

        return;
    }

    const confirmar =
        confirm(
            `Deseja realmente excluir "${nome}"?\n\nAs movimentações relacionadas a este produto também serão excluídas.`
        );

    if (!confirmar) {
        return;
    }

    const token =
        sessionStorage.getItem(
            "token"
        );

    try {
        const resposta =
            await fetch(
                "/produtos/"
                + id,
                {
                    method: "DELETE",

                    headers: {
                        "Authorization":
                            "Bearer "
                            + token
                    }
                }
            );

        if (
            resposta.status === 401
        ) {
            logout();
            return;
        }

        if (!resposta.ok) {
            let dados = {};

            try {
                dados =
                    await resposta.json();
            } catch (erro) {
                dados = {};
            }

            alert(
                dados.mensagem ||
                dados.message ||
                dados.detail ||
                "Não foi possível excluir o produto."
            );

            return;
        }

        const produtoEmEdicao =
            document
                .getElementById(
                    "produtoIdEdicao"
                )
                .value;

        if (
            String(produtoEmEdicao) ===
            String(id)
        ) {
            cancelarEdicao();
        }

        await carregarProdutos();

        if (
            podeMovimentarEstoque()
        ) {
            await carregarProdutosMovimentacao();
        }

        await carregarHistorico();

        alert(
            "Produto excluído com sucesso."
        );

    } catch (erro) {
        console.error(
            "Erro ao excluir produto:",
            erro
        );

        alert(
            "Erro ao conectar com o servidor."
        );
    }
}


function limparFormularioProduto() {
    document
        .getElementById("sku")
        .value = "";

    document
        .getElementById("nome")
        .value = "";

    document
        .getElementById("preco")
        .value = "";

    document
        .getElementById("quantidade")
        .value = "";

    document
        .getElementById("estoqueMinimo")
        .value = "";

    document
        .getElementById("categoriaId")
        .value = "";

    document
        .getElementById("descricao")
        .value = "";

    document
        .getElementById("produtoIdEdicao")
        .value = "";

    document
        .getElementById("imagemUrlAtual")
        .value = "";

    document
        .getElementById("imagemProduto")
        .value = "";

    limparPreviewImagem();
}


async function registrarMovimentacao() {
    if (
        !podeMovimentarEstoque()
    ) {
        alert(
            "Seu perfil não possui permissão para movimentar o estoque."
        );

        return;
    }

    const token =
        sessionStorage.getItem(
            "token"
        );

    const produtoId =
        document
            .getElementById(
                "movimentacaoProdutoId"
            )
            .value;

    const tipo =
        document
            .getElementById(
                "movimentacaoTipo"
            )
            .value;

    const quantidade =
        document
            .getElementById(
                "movimentacaoQuantidade"
            )
            .value;

    const observacao =
        document
            .getElementById(
                "movimentacaoObservacao"
            )
            .value
            .trim();

    const mensagem =
        document.getElementById(
            "movimentacao-mensagem"
        );

    if (
        !produtoId ||
        !tipo ||
        !quantidade
    ) {
        mensagem.textContent =
            "Preencha produto, tipo e quantidade.";

        return;
    }

    if (
        Number(quantidade) <= 0
    ) {
        mensagem.textContent =
            "A quantidade deve ser maior que zero.";

        return;
    }

    mensagem.textContent =
        "Registrando movimentação...";

    try {
        const resposta =
            await fetch(
                "/movimentacoes",
                {
                    method: "POST",

                    headers: {
                        "Content-Type":
                            "application/json",

                        "Authorization":
                            "Bearer "
                            + token
                    },

                    body: JSON.stringify({
                        produtoId:
                            Number(
                                produtoId
                            ),

                        tipo:
                        tipo,

                        quantidade:
                            Number(
                                quantidade
                            ),

                        observacao:
                        observacao
                    })
                }
            );

        if (
            resposta.status === 401
        ) {
            logout();
            return;
        }

        let dados = {};

        try {
            dados =
                await resposta.json();
        } catch (erro) {
            dados = {};
        }

        if (
            resposta.status === 403
        ) {
            mensagem.textContent =
                dados.mensagem ||
                "Você não possui permissão para movimentar o estoque.";

            return;
        }

        if (!resposta.ok) {
            mensagem.textContent =
                dados.mensagem ||
                dados.message ||
                dados.detail ||
                "Erro ao registrar movimentação.";

            return;
        }

        mensagem.textContent =
            "Movimentação registrada por "
            + obterNomeUsuario()
            + ".";

        limparFormularioMovimentacao();

        await carregarProdutos();
        await carregarProdutosMovimentacao();
        await carregarHistorico();

    } catch (erro) {
        console.error(
            "Erro ao registrar movimentação:",
            erro
        );

        mensagem.textContent =
            "Erro ao conectar com o servidor.";
    }
}


function limparFormularioMovimentacao() {
    document
        .getElementById(
            "movimentacaoProdutoId"
        )
        .value = "";

    document
        .getElementById(
            "movimentacaoTipo"
        )
        .value = "";

    document
        .getElementById(
            "movimentacaoQuantidade"
        )
        .value = "";

    document
        .getElementById(
            "movimentacaoObservacao"
        )
        .value = "";
}


async function carregarHistorico() {
    const token =
        sessionStorage.getItem(
            "token"
        );

    const historico =
        document.getElementById(
            "historico-movimentacoes"
        );

    historico.innerHTML =
        "<p>Carregando histórico...</p>";

    try {
        const resposta =
            await fetch(
                "/movimentacoes",
                {
                    headers: {
                        "Authorization":
                            "Bearer "
                            + token
                    }
                }
            );

        if (
            resposta.status === 401
        ) {
            logout();
            return;
        }

        if (!resposta.ok) {
            historico.innerHTML =
                "<p>Erro ao carregar histórico.</p>";

            return;
        }

        const movimentacoes =
            await resposta.json();

        mostrarHistorico(
            movimentacoes
        );

    } catch (erro) {
        console.error(
            "Erro ao carregar histórico:",
            erro
        );

        historico.innerHTML =
            "<p>Erro ao conectar com o servidor.</p>";
    }
}


function mostrarHistorico(
    movimentacoes
) {
    const historico =
        document.getElementById(
            "historico-movimentacoes"
        );

    historico.innerHTML = "";

    if (
        !movimentacoes ||
        movimentacoes.length === 0
    ) {
        historico.innerHTML =
            "<p>Nenhuma movimentação registrada.</p>";

        return;
    }

    movimentacoes
        .slice()
        .reverse()
        .forEach(
            movimentacao => {
                const item =
                    document.createElement(
                        "div"
                    );

                item.classList.add(
                    "movimentacao"
                );

                const titulo =
                    document.createElement(
                        "h3"
                    );

                titulo.textContent =
                    movimentacao.produtoNome;

                const tipo =
                    document.createElement(
                        "p"
                    );

                tipo.textContent =
                    "Tipo: "
                    + movimentacao.tipo;

                const quantidade =
                    document.createElement(
                        "p"
                    );

                quantidade.textContent =
                    "Quantidade: "
                    + movimentacao.quantidade;

                const estoqueAtual =
                    document.createElement(
                        "p"
                    );

                estoqueAtual.textContent =
                    "Estoque atual: "
                    + movimentacao.estoqueAtual;

                const responsavel =
                    document.createElement(
                        "p"
                    );

                responsavel.classList.add(
                    "responsavel-historico"
                );

                responsavel.textContent =
                    "Responsável: "
                    + (
                        movimentacao.responsavelNome ||
                        movimentacao.responsavelEmail ||
                        "Não registrado"
                    );

                const perfil =
                    document.createElement(
                        "p"
                    );

                perfil.textContent =
                    "Perfil: "
                    + (
                        movimentacao.responsavelPerfil ||
                        "Não registrado"
                    );

                const observacao =
                    document.createElement(
                        "p"
                    );

                observacao.textContent =
                    "Observação: "
                    + (
                        movimentacao.observacao ||
                        "Sem observação"
                    );

                const data =
                    document.createElement(
                        "p"
                    );

                if (
                    movimentacao.dataHora
                ) {
                    data.textContent =
                        "Data: "
                        + new Date(
                            movimentacao.dataHora
                        ).toLocaleString(
                            "pt-BR"
                        );

                } else {
                    data.textContent =
                        "Data não informada";
                }

                item.appendChild(
                    titulo
                );

                item.appendChild(
                    tipo
                );

                item.appendChild(
                    quantidade
                );

                item.appendChild(
                    estoqueAtual
                );

                item.appendChild(
                    responsavel
                );

                item.appendChild(
                    perfil
                );

                item.appendChild(
                    observacao
                );

                item.appendChild(
                    data
                );

                historico.appendChild(
                    item
                );
            }
        );
}


document.addEventListener(
    "DOMContentLoaded",
    async () => {
        const inputSku =
            document.getElementById(
                "sku"
            );

        if (inputSku) {
            inputSku.addEventListener(
                "input",
                () => {
                    inputSku.value =
                        inputSku.value
                            .toUpperCase();
                }
            );
        }

        const filtroSku =
            document.getElementById(
                "filtroSku"
            );

        if (filtroSku) {
            filtroSku.addEventListener(
                "input",
                () => {
                    filtroSku.value =
                        filtroSku.value
                            .toUpperCase();
                }
            );
        }

        const camposEnter = [
            "filtroNome",
            "filtroSku",
            "filtroPrecoMin",
            "filtroPrecoMax",
            "filtroQuantidadeMin",
            "filtroQuantidadeMax"
        ];

        camposEnter.forEach(
            id => {
                const elemento =
                    document.getElementById(
                        id
                    );

                if (elemento) {
                    elemento.addEventListener(
                        "keydown",
                        async evento => {
                            if (
                                evento.key ===
                                "Enter"
                            ) {
                                evento.preventDefault();

                                await aplicarFiltrosProdutos();
                            }
                        }
                    );
                }
            }
        );

        const buscaUsuario =
            document.getElementById(
                "buscaUsuario"
            );

        if (buscaUsuario) {
            buscaUsuario.addEventListener(
                "keydown",
                async evento => {
                    if (
                        evento.key ===
                        "Enter"
                    ) {
                        evento.preventDefault();

                        await aplicarFiltrosUsuarios();
                    }
                }
            );
        }

        const filtroPerfilUsuario =
            document.getElementById(
                "filtroPerfilUsuario"
            );

        if (filtroPerfilUsuario) {
            filtroPerfilUsuario.addEventListener(
                "change",
                async () => {
                    await aplicarFiltrosUsuarios();
                }
            );
        }

        const ordenacaoUsuario =
            document.getElementById(
                "ordenacaoUsuario"
            );

        if (ordenacaoUsuario) {
            ordenacaoUsuario.addEventListener(
                "change",
                async () => {
                    await aplicarFiltrosUsuarios();
                }
            );
        }

        const direcaoUsuario =
            document.getElementById(
                "direcaoUsuario"
            );

        if (direcaoUsuario) {
            direcaoUsuario.addEventListener(
                "change",
                async () => {
                    await aplicarFiltrosUsuarios();
                }
            );
        }

        const tamanhoPaginaUsuario =
            document.getElementById(
                "tamanhoPaginaUsuario"
            );

        if (tamanhoPaginaUsuario) {
            tamanhoPaginaUsuario.addEventListener(
                "change",
                async () => {
                    paginaAtualUsuarios = 0;

                    await carregarUsuariosAdmin(
                        0
                    );
                }
            );
        }

        const inputImagem =
            document.getElementById(
                "imagemProduto"
            );

        if (inputImagem) {
            inputImagem.addEventListener(
                "change",
                () => {
                    const arquivo =
                        obterImagemSelecionada();

                    const mensagem =
                        document.getElementById(
                            "produto-mensagem"
                        );

                    if (!arquivo) {
                        const imagemAtual =
                            document
                                .getElementById(
                                    "imagemUrlAtual"
                                )
                                .value;

                        if (imagemAtual) {
                            mostrarImagemAtual(
                                imagemAtual
                            );
                        } else {
                            limparPreviewImagem();
                        }

                        return;
                    }

                    const validacao =
                        validarImagem(
                            arquivo
                        );

                    if (
                        !validacao.valida
                    ) {
                        inputImagem.value =
                            "";

                        mensagem.textContent =
                            validacao.mensagem;

                        const imagemAtual =
                            document
                                .getElementById(
                                    "imagemUrlAtual"
                                )
                                .value;

                        if (imagemAtual) {
                            mostrarImagemAtual(
                                imagemAtual
                            );
                        } else {
                            limparPreviewImagem();
                        }

                        return;
                    }

                    mensagem.textContent =
                        "";

                    mostrarPreviewArquivo(
                        arquivo
                    );
                }
            );
        }

        const token =
            sessionStorage.getItem(
                "token"
            );

        const perfil =
            sessionStorage.getItem(
                "perfil"
            );

        if (
            token &&
            perfil
        ) {
            mostrarSistema();

            await carregarCategorias();
            await carregarProdutos();

            if (
                podeMovimentarEstoque()
            ) {
                await carregarProdutosMovimentacao();
            }

            await carregarHistorico();

            if (ehAdmin()) {
                paginaAtualUsuarios = 0;

                await carregarUsuariosAdmin(
                    0
                );
            }

        } else if (token) {

            sessionStorage.removeItem(
                "token"
            );

            sessionStorage.removeItem(
                "email"
            );

            sessionStorage.removeItem(
                "nomeUsuario"
            );

            sessionStorage.removeItem(
                "perfil"
            );

            mostrarLogin();

            document
                .getElementById(
                    "login-mensagem"
                )
                .textContent =
                "Faça login novamente para carregar seu nome de usuário e suas permissões.";

        } else {
            mostrarLogin();
        }
    }
);