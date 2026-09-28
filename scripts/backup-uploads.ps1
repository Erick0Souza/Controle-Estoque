param(
    [string]$BackupRoot
)

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($BackupRoot)) {

    $documentos = [Environment]::GetFolderPath("MyDocuments")

    if ([string]::IsNullOrWhiteSpace($documentos)) {
        $documentos = Join-Path -Path $HOME -ChildPath "Documents"
    }

    $BackupRoot = Join-Path -Path $documentos -ChildPath "EstoqueBackups"
}

$projectRoot = Split-Path -Parent $PSScriptRoot

$backupUploadsDir = Join-Path -Path $BackupRoot -ChildPath "uploads"

New-Item `
    -ItemType Directory `
    -Force `
    -Path $backupUploadsDir |
        Out-Null

$dataHora = Get-Date -Format "yyyyMMdd_HHmmss"

$nomeArquivo = "uploads_$dataHora.tar.gz"

$caminhoBackup = Join-Path -Path $backupUploadsDir -ChildPath $nomeArquivo

Write-Host ""
Write-Host "======================================="
Write-Host " BACKUP DOS UPLOADS - CONTROLE ESTOQUE "
Write-Host "======================================="
Write-Host ""

Push-Location -Path $projectRoot

try {

    Write-Host "Localizando o container da API..."

    $apiContainerId =
    docker compose ps -aq api

    if ([string]::IsNullOrWhiteSpace($apiContainerId)) {

        Write-Host ""
        Write-Host "Container da API ainda nao existe."
        Write-Host "Criando container sem iniciar a aplicacao..."
        Write-Host ""

        docker compose create api

        if ($LASTEXITCODE -ne 0) {
            throw "Nao foi possivel criar o container da API."
        }

        $apiContainerId =
        docker compose ps -aq api
    }

    if ([string]::IsNullOrWhiteSpace($apiContainerId)) {
        throw "Nao foi possivel localizar o container da API."
    }

    Write-Host "Container encontrado."
    Write-Host ""
    Write-Host "Gerando arquivo compactado dos uploads..."
    Write-Host ""

    docker run `
        --rm `
        --volumes-from $apiContainerId `
        -v "${backupUploadsDir}:/backup" `
        alpine:3.20 `
        sh -c "tar -czf /backup/$nomeArquivo -C /app/uploads ."

    if ($LASTEXITCODE -ne 0) {
        throw "Erro ao gerar o backup dos uploads."
    }

    if (-not (Test-Path -Path $caminhoBackup)) {
        throw "O arquivo de backup nao foi encontrado."
    }

    $arquivo = Get-Item -Path $caminhoBackup

    if ($arquivo.Length -le 0) {
        throw "O arquivo de backup foi criado vazio."
    }

    Write-Host "Validando arquivo..."

    docker run `
        --rm `
        -v "${backupUploadsDir}:/backup" `
        alpine:3.20 `
        sh -c "tar -tzf /backup/$nomeArquivo > /dev/null"

    if ($LASTEXITCODE -ne 0) {
        throw "O arquivo de backup foi criado, mas nao passou na validacao."
    }

    $tamanhoKb =
    [Math]::Round(
            $arquivo.Length / 1KB,
            2
    )

    Write-Host ""
    Write-Host "======================================="
    Write-Host " BACKUP DOS UPLOADS CONCLUIDO "
    Write-Host "======================================="
    Write-Host ""
    Write-Host "Arquivo:"
    Write-Host $caminhoBackup
    Write-Host ""
    Write-Host "Tamanho: $tamanhoKb KB"
    Write-Host ""
}
catch {

    Write-Host ""
    Write-Host "======================================="
    Write-Host " ERRO AO REALIZAR BACKUP DOS UPLOADS "
    Write-Host "======================================="
    Write-Host ""
    Write-Host $_.Exception.Message
    Write-Host ""

    exit 1
}
finally {

    Pop-Location
}