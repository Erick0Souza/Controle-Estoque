package com.erick.estoque.produto;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

@Service
public class ProdutoImagemService {

    private static final long TAMANHO_MAXIMO = 5 * 1024 * 1024;

    private static final Path DIRETORIO_IMAGENS =
            Paths.get("uploads", "produtos")
                    .toAbsolutePath()
                    .normalize();

    private static final Map<String, String> TIPOS_PERMITIDOS =
            Map.of(
                    "image/jpeg", "jpg",
                    "image/png", "png",
                    "image/webp", "webp"
            );

    public String salvarImagem(MultipartFile arquivo) {

        if (arquivo == null || arquivo.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Selecione uma imagem"
            );
        }

        if (arquivo.getSize() > TAMANHO_MAXIMO) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A imagem deve ter no máximo 5 MB"
            );
        }

        String tipoConteudo =
                arquivo.getContentType();

        String extensao =
                TIPOS_PERMITIDOS.get(tipoConteudo);

        if (extensao == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Formato de imagem não permitido"
            );
        }

        try {

            Files.createDirectories(
                    DIRETORIO_IMAGENS
            );

            String nomeArquivo =
                    UUID.randomUUID()
                            + "."
                            + extensao;

            Path destino =
                    DIRETORIO_IMAGENS
                            .resolve(nomeArquivo)
                            .normalize();

            if (!destino.startsWith(DIRETORIO_IMAGENS)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Nome de arquivo inválido"
                );
            }

            try (var inputStream =
                         arquivo.getInputStream()) {

                Files.copy(
                        inputStream,
                        destino,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

            return "/uploads/produtos/"
                    + nomeArquivo;

        } catch (IOException exception) {

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Não foi possível salvar a imagem"
            );
        }
    }

    public void excluirImagem(String imagemUrl) {

        if (
                imagemUrl == null
                        || imagemUrl.isBlank()
        ) {
            return;
        }

        String prefixo =
                "/uploads/produtos/";

        if (!imagemUrl.startsWith(prefixo)) {
            return;
        }

        String nomeArquivo =
                imagemUrl.substring(
                        prefixo.length()
                );

        Path arquivo =
                DIRETORIO_IMAGENS
                        .resolve(nomeArquivo)
                        .normalize();

        if (!arquivo.startsWith(DIRETORIO_IMAGENS)) {
            return;
        }

        try {

            Files.deleteIfExists(
                    arquivo
            );

        } catch (IOException exception) {

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Não foi possível excluir a imagem"
            );
        }
    }
}