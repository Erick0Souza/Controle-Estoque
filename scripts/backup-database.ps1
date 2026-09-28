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

$backupDatabaseDir = Join-Path -Path $BackupRoot -ChildPath "database"

New-Item `
    -ItemType Directory `
    -Force `
    -Path $backupDatabaseDir |
        Out-Null

$dataHora = Get-Date -Format "yyyyMMdd_HHmmss"

$nomeArquivo = "estoque_$dataHora.backup"

$caminhoBackup = Join-Path -Path $backupDatabaseDir -ChildPath $nomeArquivo

$containerName = "estoque-postgres"

$arquivoTemporario = "/tmp/estoque_backup.dump"

Write-Host ""
Write-Host "======================================="
Write-Host " BACKUP DO BANCO - CONTROLE DE ESTOQUE "
Write-Host "======================================="
Write-Host ""

Push-Location -Path $projectRoot

try {

    Write-Host "Verificando PostgreSQL..."

    $containerId =
    docker compose ps -q postgres

    if ([string]::IsNullOrWhiteSpace($containerId)) {

        Write-Host ""
        Write-Host "Container PostgreSQL ainda nao existe."
        Write-Host "Criando e iniciando container..."
        Write-Host ""

        docker compose up -d postgres

        if ($LASTEXITCODE -ne 0) {
            throw "Nao foi possivel criar ou iniciar o PostgreSQL."
        }

    } else {

        $containerRunning =
        docker inspect `
            -f "{{.State.Running}}" `
            $containerId

        if ($containerRunning -ne "true") {

            Write-Host ""
            Write-Host "PostgreSQL esta parado."
            Write-Host "Iniciando container..."
            Write-Host ""

            docker compose up -d postgres

            if ($LASTEXITCODE -ne 0) {
                throw "Nao foi possivel iniciar o PostgreSQL."
            }
        }
    }

    Write-Host "Aguardando PostgreSQL ficar saudavel..."

    $postgresSaudavel = $false

    for (
            $tentativa = 1;
            $tentativa -le 30;
            $tentativa++
    ) {

        $status =
        docker inspect `
                -f "{{if .State.Health}}{{.State.Health.Status}}{{else}}unknown{{end}}" `
                $containerName `
                2>$null

        if ($status -eq "healthy") {

            $postgresSaudavel = $true

            break
        }

        Write-Host "Tentativa $tentativa/30 - status: $status"

        Start-Sleep -Seconds 2
    }

    if (-not $postgresSaudavel) {
        throw "O PostgreSQL nao ficou saudavel dentro do tempo esperado."
    }

    Write-Host ""
    Write-Host "PostgreSQL pronto."
    Write-Host "Gerando backup..."
    Write-Host ""

    docker compose exec -T postgres `
        sh -c 'pg_dump -U "$POSTGRES_USER" -d "$POSTGRES_DB" -Fc -f /tmp/estoque_backup.dump'

    if ($LASTEXITCODE -ne 0) {
        throw "Erro ao executar pg_dump."
    }

    Write-Host "Backup criado dentro do container."
    Write-Host "Copiando backup para o computador..."

    docker cp `
        "${containerName}:${arquivoTemporario}" `
        "$caminhoBackup"

    if ($LASTEXITCODE -ne 0) {
        throw "Erro ao copiar o arquivo de backup para o computador."
    }

    Write-Host "Removendo arquivo temporario do container..."

    docker compose exec -T postgres `
        rm -f $arquivoTemporario

    if (-not (Test-Path -Path $caminhoBackup)) {
        throw "O arquivo de backup nao foi encontrado no computador."
    }

    $arquivo = Get-Item -Path $caminhoBackup

    if ($arquivo.Length -le 0) {
        throw "O arquivo de backup foi criado vazio."
    }

    $tamanhoKb =
    [Math]::Round(
            $arquivo.Length / 1KB,
            2
    )

    Write-Host ""
    Write-Host "======================================="
    Write-Host " BACKUP CONCLUIDO COM SUCESSO "
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
    Write-Host " ERRO AO REALIZAR BACKUP "
    Write-Host "======================================="
    Write-Host ""
    Write-Host $_.Exception.Message
    Write-Host ""

    exit 1
}
finally {

    Pop-Location
}