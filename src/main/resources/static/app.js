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
            .getElementById("cadastro-confirmar-senha")
            .value;

    const mensagem =
        document.getElementById("cadastro-mensagem");


    if (!email || !senha || !confirmarSenha) {

        mensagem.textContent =
            "Preencha todos os campos.";

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

        const resposta = await fetch(
            "/auth/register",
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
            dados = await resposta.json();
        } catch (erro) {
            dados = {};
        }


        if (!resposta.ok) {

            mensagem.textContent =
                dados.mensagem ||
                "Não foi possível criar a conta.";

            return;
        }


        mensagem.textContent =
            "Conta criada com sucesso!";


        document
            .getElementById("cadastro-email")
            .value = "";

        document
            .getElementById("cadastro-senha")
            .value = "";

        document
            .getElementById("cadastro-confirmar-senha")
            .value = "";


        document
            .getElementById("email")
            .value =
            email;


        setTimeout(
            () => {

                mostrarLogin();

                document
                    .getElementById("login-mensagem")
                    .textContent =
                    "Conta criada. Faça login.";
            },
            700
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
        document.getElementById("login-mensagem");


    if (!email || !senha) {

        mensagem.textContent =
            "Preencha email e senha.";

        return;
    }


    mensagem.textContent =
        "Entrando...";


    try {

        const resposta = await fetch(
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
            dados = await resposta.json();
        } catch (erro) {
            dados = {};
        }


        if (!resposta.ok) {

            mensagem.textContent =
                dados.mensagem ||
                "Email ou senha inválidos.";

            return;
        }


        sessionStorage.setItem(
            "token",
            dados.token
        );

        sessionStorage.setItem(
            "email",
            email
        );


        mensagem.textContent = "";


        mostrarSistema();


        await carregarCategorias();

        await carregarProdutos();

        await carregarHistorico();

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


    document
        .getElementById("usuario-logado")
        .textContent =
        sessionStorage.getItem("email") || "";
}


function logout() {

    sessionStorage.removeItem("token");

    sessionStorage.removeItem("email");


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
        .getElementById("historico-movimentacoes")
        .innerHTML = "";


    limparFormularioProduto();

    limparFormularioMovimentacao();

    mostrarLogin();
}


async function carregarCategorias() {

    const token =
        sessionStorage.getItem("token");

    const select =
        document.getElementById("categoriaId");


    try {

        const resposta = await fetch(
            "/categorias",
            {
                headers: {
                    "Authorization":
                        "Bearer " + token
                }
            }
        );


        if (resposta.status === 401) {

            logout();

            return;
        }


        const categorias =
            await resposta.json();


        select.innerHTML =
            '<option value="">Selecione uma categoria</option>';


        categorias.forEach(
            categoria => {

                const option =
                    document.createElement("option");

                option.value =
                    categoria.id;

                option.textContent =
                    categoria.nome;

                select.appendChild(option);
            }
        );

    } catch (erro) {

        console.error(
            "Erro ao carregar categorias:",
            erro
        );
    }
}


async function carregarProdutos() {

    const token =
        sessionStorage.getItem("token");

    const lista =
        document.getElementById("produtos");


    lista.innerHTML =
        "<p>Carregando produtos...</p>";


    try {

        const resposta = await fetch(
            "/produtos",
            {
                headers: {
                    "Authorization":
                        "Bearer " + token
                }
            }
        );


        if (resposta.status === 401) {

            logout();

            return;
        }


        if (!resposta.ok) {

            lista.innerHTML =
                "<p>Erro ao carregar produtos.</p>";

            return;
        }


        const produtos =
            await resposta.json();


        mostrarProdutos(produtos);

        preencherProdutosMovimentacao(produtos);

    } catch (erro) {

        console.error(
            "Erro ao carregar produtos:",
            erro
        );

        lista.innerHTML =
            "<p>Erro ao conectar com a API.</p>";
    }
}


function mostrarProdutos(produtos) {

    const lista =
        document.getElementById("produtos");


    lista.innerHTML = "";


    if (!produtos || produtos.length === 0) {

        lista.innerHTML =
            "<p>Nenhum produto cadastrado.</p>";

        return;
    }


    produtos.forEach(
        produto => {

            const card =
                document.createElement("div");

            card.classList.add("produto");


            const titulo =
                document.createElement("h3");

            titulo.textContent =
                produto.nome;


            const preco =
                document.createElement("p");

            preco.textContent =
                "Preço: R$ " +
                Number(produto.preco)
                    .toFixed(2);


            const quantidade =
                document.createElement("p");

            quantidade.textContent =
                "Quantidade: " +
                produto.quantidade;


            const categoria =
                document.createElement("p");

            categoria.textContent =
                "Categoria: " +
                (
                    produto.categoriaNome ||
                    "Sem categoria"
                );


            const descricao =
                document.createElement("p");

            descricao.textContent =
                "Descrição: " +
                (
                    produto.descricao ||
                    "Sem descrição"
                );


            const acoes =
                document.createElement("div");

            acoes.classList.add("acoes");


            const botaoEditar =
                document.createElement("button");

            botaoEditar.type =
                "button";

            botaoEditar.textContent =
                "Editar";

            botaoEditar.onclick =
                () => prepararEdicao(produto);


            const botaoExcluir =
                document.createElement("button");

            botaoExcluir.type =
                "button";

            botaoExcluir.textContent =
                "Excluir";

            botaoExcluir.onclick =
                () => excluirProduto(
                    produto.id,
                    produto.nome
                );


            acoes.appendChild(botaoEditar);

            acoes.appendChild(botaoExcluir);


            card.appendChild(titulo);

            card.appendChild(preco);

            card.appendChild(quantidade);

            card.appendChild(categoria);

            card.appendChild(descricao);

            card.appendChild(acoes);


            lista.appendChild(card);
        }
    );
}


function preencherProdutosMovimentacao(
    produtos
) {

    const select =
        document.getElementById(
            "movimentacaoProdutoId"
        );


    select.innerHTML =
        '<option value="">Selecione um produto</option>';


    produtos.forEach(
        produto => {

            const option =
                document.createElement("option");

            option.value =
                produto.id;

            option.textContent =
                produto.nome +
                " - estoque: " +
                produto.quantidade;

            select.appendChild(option);
        }
    );
}


async function salvarProduto() {

    const produtoId =
        document
            .getElementById("produtoIdEdicao")
            .value;


    if (produtoId) {

        await editarProduto(produtoId);

    } else {

        await criarProduto();
    }
}


async function criarProduto() {

    const token =
        sessionStorage.getItem("token");

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

    const categoriaId =
        document
            .getElementById("categoriaId")
            .value;

    const descricao =
        document
            .getElementById("descricao")
            .value
            .trim();

    const mensagem =
        document.getElementById(
            "produto-mensagem"
        );


    if (
        !nome ||
        preco === "" ||
        quantidade === "" ||
        categoriaId === ""
    ) {

        mensagem.textContent =
            "Preencha os campos obrigatórios.";

        return;
    }


    try {

        const resposta = await fetch(
            "/produtos",
            {
                method: "POST",

                headers: {
                    "Content-Type":
                        "application/json",

                    "Authorization":
                        "Bearer " + token
                },

                body: JSON.stringify({
                    nome:
                    nome,

                    preco:
                        Number(preco),

                    quantidade:
                        Number(quantidade),

                    categoriaId:
                        Number(categoriaId),

                    descricao:
                    descricao
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
                "Erro ao cadastrar produto.";

            return;
        }


        mensagem.textContent =
            "Produto cadastrado com sucesso!";


        limparFormularioProduto();

        await carregarProdutos();

    } catch (erro) {

        mensagem.textContent =
            "Erro ao conectar com o servidor.";
    }
}


function prepararEdicao(produto) {

    document
        .getElementById("produtoIdEdicao")
        .value =
        produto.id;

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
        .getElementById("categoriaId")
        .value =
        produto.categoriaId;

    document
        .getElementById("descricao")
        .value =
        produto.descricao || "";


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
}


async function editarProduto(id) {

    const token =
        sessionStorage.getItem("token");

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

    const categoriaId =
        document
            .getElementById("categoriaId")
            .value;

    const descricao =
        document
            .getElementById("descricao")
            .value
            .trim();

    const mensagem =
        document.getElementById(
            "produto-mensagem"
        );


    const resposta = await fetch(
        "/produtos/" + id,
        {
            method: "PUT",

            headers: {
                "Content-Type":
                    "application/json",

                "Authorization":
                    "Bearer " + token
            },

            body: JSON.stringify({
                nome:
                nome,

                preco:
                    Number(preco),

                quantidade:
                    Number(quantidade),

                categoriaId:
                    Number(categoriaId),

                descricao:
                descricao
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
            "Erro ao editar produto.";

        return;
    }


    cancelarEdicao();


    mensagem.textContent =
        "Produto atualizado com sucesso!";


    await carregarProdutos();
}


function cancelarEdicao() {

    limparFormularioProduto();


    document
        .getElementById("produtoIdEdicao")
        .value = "";


    document
        .getElementById("botaoSalvar")
        .textContent =
        "Cadastrar";


    document
        .getElementById("botaoCancelar")
        .classList
        .add("hidden");


    document
        .getElementById("titulo-formulario")
        .textContent =
        "Novo Produto";
}


async function excluirProduto(
    id,
    nome
) {

    const confirmar =
        confirm(
            `Deseja realmente excluir "${nome}"?`
        );


    if (!confirmar) {

        return;
    }


    const token =
        sessionStorage.getItem("token");


    const resposta = await fetch(
        "/produtos/" + id,
        {
            method: "DELETE",

            headers: {
                "Authorization":
                    "Bearer " + token
            }
        }
    );


    if (!resposta.ok) {

        alert(
            "Não foi possível excluir o produto."
        );

        return;
    }


    await carregarProdutos();
}


function limparFormularioProduto() {

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
        .getElementById("categoriaId")
        .value = "";

    document
        .getElementById("descricao")
        .value = "";
}


async function registrarMovimentacao() {

    const token =
        sessionStorage.getItem("token");

    const produtoId =
        document
            .getElementById("movimentacaoProdutoId")
            .value;

    const tipo =
        document
            .getElementById("movimentacaoTipo")
            .value;

    const quantidade =
        document
            .getElementById("movimentacaoQuantidade")
            .value;

    const observacao =
        document
            .getElementById("movimentacaoObservacao")
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


    const resposta = await fetch(
        "/movimentacoes",
        {
            method: "POST",

            headers: {
                "Content-Type":
                    "application/json",

                "Authorization":
                    "Bearer " + token
            },

            body: JSON.stringify({
                produtoId:
                    Number(produtoId),

                tipo:
                tipo,

                quantidade:
                    Number(quantidade),

                observacao:
                observacao
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
            "Erro ao registrar movimentação.";

        return;
    }


    mensagem.textContent =
        "Movimentação registrada com sucesso!";


    limparFormularioMovimentacao();

    await carregarProdutos();

    await carregarHistorico();
}


function limparFormularioMovimentacao() {

    document
        .getElementById("movimentacaoProdutoId")
        .value = "";

    document
        .getElementById("movimentacaoTipo")
        .value = "";

    document
        .getElementById("movimentacaoQuantidade")
        .value = "";

    document
        .getElementById("movimentacaoObservacao")
        .value = "";
}


async function carregarHistorico() {

    const token =
        sessionStorage.getItem("token");

    const historico =
        document.getElementById(
            "historico-movimentacoes"
        );


    const resposta = await fetch(
        "/movimentacoes",
        {
            headers: {
                "Authorization":
                    "Bearer " + token
            }
        }
    );


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
                    document.createElement("div");

                item.classList.add(
                    "movimentacao"
                );


                const titulo =
                    document.createElement("h3");

                titulo.textContent =
                    movimentacao.produtoNome;


                const tipo =
                    document.createElement("p");

                tipo.textContent =
                    "Tipo: " +
                    movimentacao.tipo;


                const quantidade =
                    document.createElement("p");

                quantidade.textContent =
                    "Quantidade: " +
                    movimentacao.quantidade;


                const estoqueAtual =
                    document.createElement("p");

                estoqueAtual.textContent =
                    "Estoque atual: " +
                    movimentacao.estoqueAtual;


                const observacao =
                    document.createElement("p");

                observacao.textContent =
                    "Observação: " +
                    (
                        movimentacao.observacao ||
                        "Sem observação"
                    );


                const data =
                    document.createElement("p");


                if (movimentacao.dataHora) {

                    data.textContent =
                        "Data: " +
                        new Date(
                            movimentacao.dataHora
                        ).toLocaleString(
                            "pt-BR"
                        );

                } else {

                    data.textContent =
                        "Data não informada";
                }


                item.appendChild(titulo);

                item.appendChild(tipo);

                item.appendChild(quantidade);

                item.appendChild(estoqueAtual);

                item.appendChild(observacao);

                item.appendChild(data);


                historico.appendChild(item);
            }
        );
}


document.addEventListener(
    "DOMContentLoaded",
    async () => {

        const token =
            sessionStorage.getItem("token");


        if (token) {

            mostrarSistema();

            await carregarCategorias();

            await carregarProdutos();

            await carregarHistorico();

        } else {

            mostrarLogin();
        }
    }
);