param(
    [Parameter(Mandatory = $true)]
    [string]$BackupFile,

    [switch]$ValidarSomente
)

$ErrorActionPreference = "Stop"

$projectRoot =
Split-Path -Parent $PSScriptRoot

$backupUploadsScript =
Join-Path `
        -Path $PSScriptRoot `
        -ChildPath "backup-uploads.ps1"

$apiFoiParada =
$false

Write-Host ""
Write-Host "======================================="
Write-Host " RESTAURACAO DOS UPLOADS - ESTOQUE "
Write-Host "======================================="
Write-Host ""

Push-Location -Path $projectRoot

try {

    if (-not (Test-Path -Path $BackupFile)) {
        throw "O arquivo de backup informado nao existe."
    }

    $backupResolvido =
    (Resolve-Path -Path $BackupFile).Path

    $arquivo =
    Get-Item -Path $backupResolvido

    if ($arquivo.Length -le 0) {
        throw "O arquivo de backup esta vazio."
    }

    if (
    $arquivo.Extension -ne ".gz" -and
            $arquivo.Name -notlike "*.tar.gz"
    ) {
        throw "O arquivo informado nao possui o formato esperado .tar.gz."
    }

    $backupDirectory =
    Split-Path -Parent $backupResolvido

    $nomeArquivo =
    Split-Path -Leaf $backupResolvido

    Write-Host "Arquivo selecionado:"
    Write-Host $backupResolvido
    Write-Host ""

    Write-Host "Localizando container da API..."

    $apiContainerId =
    docker compose ps -aq api

    if ([string]::IsNullOrWhiteSpace($apiContainerId)) {

        Write-Host ""
        Write-Host "Container da API ainda nao existe."
        Write-Host "Criando container..."
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

    Write-Host ""
    Write-Host "Validando arquivo de backup..."

    docker run `
        --rm `
        -v "${backupDirectory}:/backup:ro" `
        alpine:3.20 `
        sh -c 'tar -tzf "/backup/$1" > /dev/null' `
        -- `
        $nomeArquivo

    if ($LASTEXITCODE -ne 0) {
        throw "O arquivo de backup esta corrompido ou nao e um TAR.GZ valido."
    }

    Write-Host ""
    Write-Host "Backup de uploads valido."

    if ($ValidarSomente) {

        Write-Host ""
        Write-Host "======================================="
        Write-Host " VALIDACAO CONCLUIDA COM SUCESSO "
        Write-Host "======================================="
        Write-Host ""
        Write-Host "Nenhum arquivo foi alterado."
        Write-Host ""

        exit 0
    }

    if (-not (Test-Path -Path $backupUploadsScript)) {
        throw "O script backup-uploads.ps1 nao foi encontrado."
    }

    Write-Host ""
    Write-Host "ATENCAO"
    Write-Host "---------------------------------------"
    Write-Host "Os uploads atuais serao substituidos"
    Write-Host "pelos arquivos presentes no backup."
    Write-Host ""
    Write-Host "Antes da restauracao sera criado um"
    Write-Host "backup de seguranca dos uploads atuais."
    Write-Host "---------------------------------------"
    Write-Host ""

    $confirmacao =
    Read-Host "Digite RESTAURAR para continuar"

    if ($confirmacao -cne "RESTAURAR") {

        Write-Host ""
        Write-Host "Restauracao cancelada."
        Write-Host ""

        exit 0
    }

    $documentos =
    [Environment]::GetFolderPath(
            "MyDocuments"
    )

    if ([string]::IsNullOrWhiteSpace($documentos)) {

        $documentos =
        Join-Path `
                -Path $HOME `
                -ChildPath "Documents"
    }

    $pastaSeguranca =
    Join-Path `
            -Path $documentos `
            -ChildPath "EstoqueBackups\pre-restauracao"

    Write-Host ""
    Write-Host "Criando backup de seguranca dos uploads atuais..."
    Write-Host ""

    & powershell.exe `
        -NoProfile `
        -ExecutionPolicy Bypass `
        -File $backupUploadsScript `
        -BackupRoot $pastaSeguranca

    if ($LASTEXITCODE -ne 0) {
        throw "O backup de seguranca dos uploads falhou. Restauracao cancelada."
    }

    Write-Host ""
    Write-Host "Backup de seguranca concluido."
    Write-Host ""

    $apiRunning =
    docker inspect `
            -f "{{.State.Running}}" `
            $apiContainerId

    if ($apiRunning -eq "true") {

        Write-Host "Parando API durante a restauracao..."

        docker compose stop api

        if ($LASTEXITCODE -ne 0) {
            throw "Nao foi possivel parar a API."
        }

        $apiFoiParada =
        $true
    }

    Write-Host ""
    Write-Host "Restaurando uploads..."
    Write-Host ""

    docker run `
        --rm `
        --volumes-from $apiContainerId `
        -v "${backupDirectory}:/backup:ro" `
        alpine:3.20 `
        sh -c 'set -e; rm -rf /tmp/uploads-restore; mkdir -p /tmp/uploads-restore; tar -xzf "/backup/$1" -C /tmp/uploads-restore; test -d /app/uploads; find /app/uploads -mindepth 1 -maxdepth 1 -exec rm -rf {} +; cp -a /tmp/uploads-restore/. /app/uploads/' `
        -- `
        $nomeArquivo

    if ($LASTEXITCODE -ne 0) {
        throw "Ocorreu um erro durante a restauracao dos uploads."
    }

    Write-Host ""
    Write-Host "Validando arquivos restaurados..."

    docker run `
        --rm `
        --volumes-from $apiContainerId `
        alpine:3.20 `
        sh -c 'test -d /app/uploads && find /app/uploads -maxdepth 1 -type f -print > /dev/null'

    if ($LASTEXITCODE -ne 0) {
        throw "Nao foi possivel validar a pasta de uploads restaurada."
    }

    if ($apiFoiParada) {

        Write-Host ""
        Write-Host "Iniciando novamente a API..."

        docker compose start api

        if ($LASTEXITCODE -ne 0) {
            throw "Os uploads foram restaurados, mas a API nao conseguiu reiniciar."
        }

        $apiFoiParada =
        $false
    }

    Write-Host ""
    Write-Host "======================================="
    Write-Host " UPLOADS RESTAURADOS COM SUCESSO "
    Write-Host "======================================="
    Write-Host ""
    Write-Host "Origem:"
    Write-Host $backupResolvido
    Write-Host ""
}
catch {

    Write-Host ""
    Write-Host "======================================="
    Write-Host " ERRO NA RESTAURACAO DOS UPLOADS "
    Write-Host "======================================="
    Write-Host ""
    Write-Host $_.Exception.Message
    Write-Host ""

    if ($apiFoiParada) {

        Write-Host "A API permaneceu parada por seguranca."
        Write-Host "Verifique o erro antes de inicia-la novamente."
        Write-Host ""
    }

    exit 1
}
finally {

    Pop-Location
}