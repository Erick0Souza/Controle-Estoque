let previewObjectUrl = null;

const TIPOS_IMAGEM_PERMITIDOS = [
    "image/jpeg",
    "image/png",
    "image/webp"
];

const TAMANHO_MAXIMO_IMAGEM =
    5 * 1024 * 1024;


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
        document.getElementById(
            "cadastro-mensagem"
        );

    if (
        !email ||
        !senha ||
        !confirmarSenha
    ) {
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
                "Não foi possível criar a conta.";

            return;
        }

        mensagem.textContent =
            "Conta criada com sucesso!";

        document
            .getElementById(
                "cadastro-email"
            )
            .value = "";

        document
            .getElementById(
                "cadastro-senha"
            )
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
        .getElementById(
            "historico-movimentacoes"
        )
        .innerHTML = "";

    limparFormularioProduto();
    limparFormularioMovimentacao();
    mostrarLogin();
}


async function carregarCategorias() {
    const token =
        sessionStorage.getItem("token");

    const select =
        document.getElementById(
            "categoriaId"
        );

    try {
        const resposta =
            await fetch(
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

        if (!resposta.ok) {
            return;
        }

        const categorias =
            await resposta.json();

        select.innerHTML =
            '<option value="">Selecione uma categoria</option>';

        categorias.forEach(
            categoria => {
                const option =
                    document.createElement(
                        "option"
                    );

                option.value =
                    categoria.id;

                option.textContent =
                    categoria.nome;

                select.appendChild(
                    option
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


async function carregarProdutos() {
    const token =
        sessionStorage.getItem("token");

    const lista =
        document.getElementById(
            "produtos"
        );

    lista.innerHTML =
        "<p>Carregando produtos...</p>";

    try {
        const resposta =
            await fetch(
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

        mostrarProdutos(
            produtos
        );

        preencherProdutosMovimentacao(
            produtos
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


function mostrarProdutos(produtos) {
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
            "<p>Nenhum produto cadastrado.</p>";

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

            if (produto.imagemUrl) {
                const imagem =
                    document.createElement(
                        "img"
                    );

                imagem.src =
                    produto.imagemUrl;

                imagem.alt =
                    "Imagem de " +
                    produto.nome;

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
                "SKU: " +
                (
                    produto.sku ||
                    "Não definido"
                );

            const identificador =
                document.createElement(
                    "p"
                );

            identificador.textContent =
                "ID: " +
                produto.id;

            const preco =
                document.createElement(
                    "p"
                );

            preco.textContent =
                "Preço: R$ " +
                Number(
                    produto.preco
                ).toFixed(2);

            const quantidade =
                document.createElement(
                    "p"
                );

            quantidade.textContent =
                "Quantidade: " +
                produto.quantidade;

            const categoria =
                document.createElement(
                    "p"
                );

            categoria.textContent =
                "Categoria: " +
                (
                    produto.categoriaNome ||
                    "Sem categoria"
                );

            const descricao =
                document.createElement(
                    "p"
                );

            descricao.textContent =
                "Descrição: " +
                (
                    produto.descricao ||
                    "Sem descrição"
                );

            const acoes =
                document.createElement(
                    "div"
                );

            acoes.classList.add(
                "acoes"
            );

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

            const botaoExcluir =
                document.createElement(
                    "button"
                );

            botaoExcluir.type =
                "button";

            botaoExcluir.textContent =
                "Excluir";

            botaoExcluir.onclick =
                () =>
                    excluirProduto(
                        produto.id,
                        produto.nome
                    );

            acoes.appendChild(
                botaoEditar
            );

            acoes.appendChild(
                botaoExcluir
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
                categoria
            );

            card.appendChild(
                descricao
            );

            card.appendChild(
                acoes
            );

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
                document.createElement(
                    "option"
                );

            option.value =
                produto.id;

            option.textContent =
                (
                    produto.sku
                        ? produto.sku + " - "
                        : ""
                ) +
                produto.nome +
                " - estoque: " +
                produto.quantidade;

            select.appendChild(
                option
            );
        }
    );
}


async function salvarProduto() {
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


function validarSku(sku) {
    if (!sku) {
        return {
            valida: false,
            mensagem:
                "O SKU é obrigatório."
        };
    }

    if (sku.length > 50) {
        return {
            valida: false,
            mensagem:
                "O SKU deve ter no máximo 50 caracteres."
        };
    }

    const padrao =
        /^[A-Za-z0-9_-]+$/;

    if (!padrao.test(sku)) {
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
    const token =
        sessionStorage.getItem("token");

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

    const categoriaId =
        document
            .getElementById(
                "categoriaId"
            )
            .value;

    const descricao =
        document
            .getElementById(
                "descricao"
            )
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

    if (!validacaoSku.valida) {
        mensagem.textContent =
            validacaoSku.mensagem;

        return;
    }

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

    if (imagem) {
        const validacao =
            validarImagem(
                imagem
            );

        if (!validacao.valida) {
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
                            "Bearer " + token
                    },

                    body: JSON.stringify({
                        sku:
                        sku,

                        nome:
                        nome,

                        preco:
                            Number(preco),

                        quantidade:
                            Number(
                                quantidade
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

                return;
            }
        }

        limparFormularioProduto();

        mensagem.textContent =
            "Produto cadastrado com sucesso!";

        await carregarProdutos();

    } catch (erro) {
        console.error(
            "Erro ao cadastrar produto:",
            erro
        );

        mensagem.textContent =
            "Erro ao conectar com o servidor.";
    }
}


function prepararEdicao(produto) {
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
        .getElementById(
            "quantidade"
        )
        .value =
        produto.quantidade;

    document
        .getElementById(
            "categoriaId"
        )
        .value =
        produto.categoriaId;

    document
        .getElementById(
            "descricao"
        )
        .value =
        produto.descricao || "";

    document
        .getElementById(
            "imagemUrlAtual"
        )
        .value =
        produto.imagemUrl || "";

    document
        .getElementById(
            "imagemProduto"
        )
        .value = "";

    document
        .getElementById(
            "botaoSalvar"
        )
        .textContent =
        "Salvar alterações";

    document
        .getElementById(
            "botaoCancelar"
        )
        .classList
        .remove("hidden");

    document
        .getElementById(
            "titulo-formulario"
        )
        .textContent =
        "Editar Produto";

    document
        .getElementById(
            "produto-mensagem"
        )
        .textContent = "";

    if (produto.imagemUrl) {
        mostrarImagemAtual(
            produto.imagemUrl
        );
    }

    window.scrollTo({
        top: 0,
        behavior: "smooth"
    });
}


async function editarProduto(id) {
    const token =
        sessionStorage.getItem("token");

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
            .getElementById(
                "quantidade"
            )
            .value;

    const categoriaId =
        document
            .getElementById(
                "categoriaId"
            )
            .value;

    const descricao =
        document
            .getElementById(
                "descricao"
            )
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

    if (!validacaoSku.valida) {
        mensagem.textContent =
            validacaoSku.mensagem;

        return;
    }

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

    if (imagem) {
        const validacao =
            validarImagem(
                imagem
            );

        if (!validacao.valida) {
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
                        sku:
                        sku,

                        nome:
                        nome,

                        preco:
                            Number(preco),

                        quantidade:
                            Number(
                                quantidade
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

                return;
            }
        }

        cancelarEdicao();

        mensagem.textContent =
            "Produto atualizado com sucesso!";

        await carregarProdutos();

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
        sessionStorage.getItem("token");

    const formData =
        new FormData();

    formData.append(
        "imagem",
        arquivo
    );

    const resposta =
        await fetch(
            "/produtos/" +
            produtoId +
            "/imagem",
            {
                method: "POST",

                headers: {
                    "Authorization":
                        "Bearer " + token
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
        sessionStorage.getItem("token");

    try {
        const resposta =
            await fetch(
                "/produtos/" +
                produtoId +
                "/imagem",
                {
                    method: "DELETE",

                    headers: {
                        "Authorization":
                            "Bearer " + token
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


function validarImagem(arquivo) {
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

        previewObjectUrl =
            null;
    }
}


function cancelarEdicao() {
    limparFormularioProduto();

    document
        .getElementById(
            "produtoIdEdicao"
        )
        .value = "";

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
    const confirmar =
        confirm(
            `Deseja realmente excluir "${nome}"?`
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
                "/produtos/" + id,
                {
                    method: "DELETE",

                    headers: {
                        "Authorization":
                            "Bearer " + token
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

            if (
                resposta.status === 409
            ) {
                alert(
                    dados.mensagem ||
                    dados.message ||
                    dados.detail ||
                    "Este produto possui movimentações de estoque e não pode ser excluído."
                );

                return;
            }

            if (
                resposta.status === 404
            ) {
                alert(
                    dados.mensagem ||
                    dados.message ||
                    dados.detail ||
                    "Produto não encontrado."
                );

                await carregarProdutos();

                return;
            }

            if (
                resposta.status === 403
            ) {
                alert(
                    dados.mensagem ||
                    dados.message ||
                    dados.detail ||
                    "Você não possui permissão para excluir este produto."
                );

                return;
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
        .getElementById(
            "quantidade"
        )
        .value = "";

    document
        .getElementById(
            "categoriaId"
        )
        .value = "";

    document
        .getElementById(
            "descricao"
        )
        .value = "";

    document
        .getElementById(
            "produtoIdEdicao"
        )
        .value = "";

    document
        .getElementById(
            "imagemUrlAtual"
        )
        .value = "";

    document
        .getElementById(
            "imagemProduto"
        )
        .value = "";

    limparPreviewImagem();
}


async function registrarMovimentacao() {
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
                            "Bearer " + token
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

        if (!resposta.ok) {
            mensagem.textContent =
                dados.mensagem ||
                dados.message ||
                dados.detail ||
                "Erro ao registrar movimentação.";

            return;
        }

        mensagem.textContent =
            "Movimentação registrada com sucesso!";

        limparFormularioMovimentacao();

        await carregarProdutos();
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
                            "Bearer " + token
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
                    "Tipo: " +
                    movimentacao.tipo;

                const quantidade =
                    document.createElement(
                        "p"
                    );

                quantidade.textContent =
                    "Quantidade: " +
                    movimentacao.quantidade;

                const estoqueAtual =
                    document.createElement(
                        "p"
                    );

                estoqueAtual.textContent =
                    "Estoque atual: " +
                    movimentacao.estoqueAtual;

                const observacao =
                    document.createElement(
                        "p"
                    );

                observacao.textContent =
                    "Observação: " +
                    (
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