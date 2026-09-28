param(
    [Parameter(Mandatory = $true)]
    [string]$BackupFile,

    [switch]$ValidarSomente
)

$ErrorActionPreference = "Stop"

$projectRoot =
Split-Path -Parent $PSScriptRoot

$backupDatabaseScript =
Join-Path `
        -Path $PSScriptRoot `
        -ChildPath "backup-database.ps1"

$containerName =
"estoque-postgres"

$arquivoTemporario =
"/tmp/estoque_restore.backup"

Write-Host ""
Write-Host "======================================="
Write-Host " RESTAURACAO DO BANCO - CONTROLE ESTOQUE"
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

    Write-Host "Arquivo selecionado:"
    Write-Host $backupResolvido
    Write-Host ""

    Write-Host "Verificando PostgreSQL..."

    $containerId =
    docker compose ps -q postgres

    if ([string]::IsNullOrWhiteSpace($containerId)) {

        Write-Host "PostgreSQL nao esta em execucao."
        Write-Host "Iniciando container..."

        docker compose up -d postgres

        if ($LASTEXITCODE -ne 0) {
            throw "Nao foi possivel iniciar o PostgreSQL."
        }

        $containerId =
        docker compose ps -q postgres
    }

    $postgresSaudavel =
    $false

    Write-Host "Aguardando PostgreSQL ficar saudavel..."

    for (
            $tentativa = 1;
            $tentativa -le 30;
            $tentativa++
    ) {

        $status =
        docker inspect `
                -f "{{if .State.Health}}{{.State.Health.Status}}{{else}}unknown{{end}}" `
                $containerId

        if ($status -eq "healthy") {

            $postgresSaudavel =
            $true

            break
        }

        Start-Sleep -Seconds 2
    }

    if (-not $postgresSaudavel) {
        throw "O PostgreSQL nao ficou saudavel."
    }

    Write-Host ""
    Write-Host "Copiando arquivo para o container..."

    docker cp `
        "$backupResolvido" `
        "${containerName}:${arquivoTemporario}"

    if ($LASTEXITCODE -ne 0) {
        throw "Nao foi possivel copiar o backup para o container."
    }

    Write-Host ""
    Write-Host "Validando estrutura do backup..."

    docker compose exec -T postgres `
        pg_restore `
        --list `
        $arquivoTemporario `
        *> $null

    if ($LASTEXITCODE -ne 0) {
        throw "O arquivo nao e um backup PostgreSQL valido ou esta corrompido."
    }

    Write-Host ""
    Write-Host "Backup valido."

    if ($ValidarSomente) {

        Write-Host ""
        Write-Host "======================================="
        Write-Host " VALIDACAO CONCLUIDA COM SUCESSO "
        Write-Host "======================================="
        Write-Host ""
        Write-Host "Nenhum dado foi alterado."
        Write-Host ""

        docker compose exec -T postgres `
            rm -f $arquivoTemporario

        exit 0
    }

    if (-not (Test-Path -Path $backupDatabaseScript)) {
        throw "O script backup-database.ps1 nao foi encontrado."
    }

    Write-Host ""
    Write-Host "ATENCAO"
    Write-Host "---------------------------------------"
    Write-Host "A restauracao vai substituir o banco"
    Write-Host "atual pelo conteudo deste backup."
    Write-Host ""
    Write-Host "Antes disso sera criado um backup"
    Write-Host "automatico de seguranca."
    Write-Host "---------------------------------------"
    Write-Host ""

    $confirmacao =
    Read-Host "Digite RESTAURAR para continuar"

    if ($confirmacao -cne "RESTAURAR") {

        Write-Host ""
        Write-Host "Restauracao cancelada."
        Write-Host ""

        docker compose exec -T postgres `
            rm -f $arquivoTemporario

        exit 0
    }

    Write-Host ""
    Write-Host "Criando backup de seguranca do banco atual..."

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

    & powershell.exe `
        -NoProfile `
        -ExecutionPolicy Bypass `
        -File $backupDatabaseScript `
        -BackupRoot $pastaSeguranca

    if ($LASTEXITCODE -ne 0) {
        throw "O backup de seguranca falhou. A restauracao foi cancelada."
    }

    Write-Host ""
    Write-Host "Backup de seguranca concluido."
    Write-Host ""

    $apiContainerId =
    docker compose ps -q api

    if (-not [string]::IsNullOrWhiteSpace($apiContainerId)) {

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
        }
    }

    Write-Host ""
    Write-Host "Encerrando conexoes com o banco..."

    docker compose exec -T postgres `
        sh -c 'psql -U "$POSTGRES_USER" -d postgres -v ON_ERROR_STOP=1 -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = '\''$POSTGRES_DB'\'' AND pid <> pg_backend_pid();"'

    if ($LASTEXITCODE -ne 0) {
        throw "Nao foi possivel encerrar as conexoes com o banco."
    }

    Write-Host "Recriando banco de dados..."

    docker compose exec -T postgres `
        sh -c 'dropdb -U "$POSTGRES_USER" --if-exists "$POSTGRES_DB"'

    if ($LASTEXITCODE -ne 0) {
        throw "Nao foi possivel remover o banco atual."
    }

    docker compose exec -T postgres `
        sh -c 'createdb -U "$POSTGRES_USER" "$POSTGRES_DB"'

    if ($LASTEXITCODE -ne 0) {
        throw "Nao foi possivel recriar o banco."
    }

    Write-Host ""
    Write-Host "Restaurando backup..."

    docker compose exec -T postgres `
        sh -c 'pg_restore -U "$POSTGRES_USER" -d "$POSTGRES_DB" --no-owner --no-privileges /tmp/estoque_restore.backup'

    if ($LASTEXITCODE -ne 0) {
        throw "O pg_restore encontrou um erro durante a restauracao."
    }

    Write-Host ""
    Write-Host "Removendo arquivo temporario..."

    docker compose exec -T postgres `
        rm -f $arquivoTemporario

    Write-Host ""
    Write-Host "======================================="
    Write-Host " BANCO RESTAURADO COM SUCESSO "
    Write-Host "======================================="
    Write-Host ""
    Write-Host "Origem:"
    Write-Host $backupResolvido
    Write-Host ""
    Write-Host "Reinicie a aplicacao antes de acessar."
    Write-Host ""
}
catch {

    Write-Host ""
    Write-Host "======================================="
    Write-Host " ERRO NA RESTAURACAO "
    Write-Host "======================================="
    Write-Host ""
    Write-Host $_.Exception.Message
    Write-Host ""

    exit 1
}
finally {

    try {

        docker compose exec -T postgres `
            rm -f $arquivoTemporario `
            2>$null |
                Out-Null

    } catch {
    }

    Pop-Location
}