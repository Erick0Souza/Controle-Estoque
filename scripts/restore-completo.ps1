param(
    [Parameter(Mandatory = $true)]
    [string]$BackupFolder,

    [switch]$ValidarSomente
)

$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot

$backupCompletoScript =
Join-Path -Path $PSScriptRoot -ChildPath "backup-completo.ps1"

$containerPostgres = "estoque-postgres"

$arquivoTemporarioBanco =
"/tmp/estoque_restore_completo.backup"

$apiFoiParada = $false

Write-Host ""
Write-Host "======================================="
Write-Host " RECUPERACAO COMPLETA - ESTOQUE "
Write-Host "======================================="
Write-Host ""

Push-Location -Path $projectRoot

try {

    #
    # VALIDAR PASTA INFORMADA
    #

    if (-not (Test-Path -Path $BackupFolder)) {
        throw "A pasta de backup informada nao existe."
    }

    $backupFolderResolvido =
    (Resolve-Path -Path $BackupFolder).Path

    Write-Host "Backup selecionado:"
    Write-Host $backupFolderResolvido
    Write-Host ""

    #
    # LOCALIZAR BANCO
    #

    $databaseFolder =
    Join-Path -Path $backupFolderResolvido -ChildPath "database"

    if (-not (Test-Path -Path $databaseFolder)) {
        throw "A pasta database nao foi encontrada dentro do backup."
    }

    $arquivosBanco =
    Get-ChildItem `
            -Path $databaseFolder `
            -Filter "*.backup" `
            -File

    if ($arquivosBanco.Count -eq 0) {
        throw "Nenhum arquivo .backup foi encontrado."
    }

    if ($arquivosBanco.Count -gt 1) {
        throw "Mais de um arquivo .backup foi encontrado. O backup completo deveria possuir apenas um."
    }

    $arquivoBanco =
    $arquivosBanco[0]

    #
    # LOCALIZAR UPLOADS
    #

    $uploadsFolder =
    Join-Path -Path $backupFolderResolvido -ChildPath "uploads"

    if (-not (Test-Path -Path $uploadsFolder)) {
        throw "A pasta uploads nao foi encontrada dentro do backup."
    }

    $arquivosUploads =
    Get-ChildItem `
            -Path $uploadsFolder `
            -Filter "*.tar.gz" `
            -File

    if ($arquivosUploads.Count -eq 0) {
        throw "Nenhum arquivo .tar.gz foi encontrado."
    }

    if ($arquivosUploads.Count -gt 1) {
        throw "Mais de um arquivo .tar.gz foi encontrado. O backup completo deveria possuir apenas um."
    }

    $arquivoUploads =
    $arquivosUploads[0]

    Write-Host "Banco:"
    Write-Host $arquivoBanco.FullName
    Write-Host ""

    Write-Host "Uploads:"
    Write-Host $arquivoUploads.FullName
    Write-Host ""

    #
    # GARANTIR POSTGRESQL
    #

    Write-Host "Verificando PostgreSQL..."

    $postgresContainerId =
    docker compose ps -q postgres

    if ([string]::IsNullOrWhiteSpace($postgresContainerId)) {

        Write-Host "Iniciando PostgreSQL..."

        docker compose up -d postgres

        if ($LASTEXITCODE -ne 0) {
            throw "Nao foi possivel iniciar o PostgreSQL."
        }

        $postgresContainerId =
        docker compose ps -q postgres
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
                $postgresContainerId

        if ($status -eq "healthy") {

            $postgresSaudavel = $true

            break
        }

        Start-Sleep -Seconds 2
    }

    if (-not $postgresSaudavel) {
        throw "O PostgreSQL nao ficou saudavel."
    }

    #
    # VALIDAR BACKUP DO BANCO
    #

    Write-Host ""
    Write-Host "Validando backup do banco..."

    docker cp `
        "$($arquivoBanco.FullName)" `
        "${containerPostgres}:${arquivoTemporarioBanco}"

    if ($LASTEXITCODE -ne 0) {
        throw "Nao foi possivel copiar o backup do banco para o container."
    }

    docker compose exec -T postgres `
        pg_restore `
        --list `
        $arquivoTemporarioBanco `
        *> $null

    if ($LASTEXITCODE -ne 0) {
        throw "O backup PostgreSQL e invalido ou esta corrompido."
    }

    Write-Host "Backup do banco valido."

    #
    # GARANTIR CONTAINER API PARA ACESSAR O VOLUME
    #

    $apiContainerId =
    docker compose ps -aq api

    if ([string]::IsNullOrWhiteSpace($apiContainerId)) {

        Write-Host ""
        Write-Host "Criando container da API para acesso aos uploads..."

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

    #
    # VALIDAR BACKUP DOS UPLOADS
    #

    Write-Host ""
    Write-Host "Validando backup dos uploads..."

    docker run `
        --rm `
        -v "${uploadsFolder}:/backup:ro" `
        alpine:3.20 `
        sh -c 'tar -tzf "/backup/$1" > /dev/null' `
        -- `
        $arquivoUploads.Name

    if ($LASTEXITCODE -ne 0) {
        throw "O backup dos uploads e invalido ou esta corrompido."
    }

    Write-Host "Backup dos uploads valido."

    #
    # MODO VALIDACAO
    #

    if ($ValidarSomente) {

        docker compose exec -T postgres `
            rm -f $arquivoTemporarioBanco `
            2>$null |
                Out-Null

        Write-Host ""
        Write-Host "======================================="
        Write-Host " BACKUP COMPLETO VALIDADO "
        Write-Host "======================================="
        Write-Host ""
        Write-Host "Banco: OK"
        Write-Host "Uploads: OK"
        Write-Host ""
        Write-Host "Nenhum dado foi alterado."
        Write-Host ""

        exit 0
    }

    #
    # CONFIRMACAO
    #

    Write-Host ""
    Write-Host "======================================="
    Write-Host " ATENCAO "
    Write-Host "======================================="
    Write-Host ""
    Write-Host "Esta operacao ira substituir:"
    Write-Host ""
    Write-Host "- o banco de dados atual"
    Write-Host "- todas as imagens/uploads atuais"
    Write-Host ""
    Write-Host "Antes disso sera criado um backup"
    Write-Host "completo de seguranca."
    Write-Host ""
    Write-Host "IMPORTANTE:"
    Write-Host "Se a aplicacao estiver rodando pelo"
    Write-Host "IntelliJ, pare o Run antes de continuar."
    Write-Host ""

    $confirmacao =
    Read-Host "Digite RESTAURAR para continuar"

    if ($confirmacao -cne "RESTAURAR") {

        Write-Host ""
        Write-Host "Recuperacao cancelada."
        Write-Host ""

        exit 0
    }

    #
    # BACKUP DE SEGURANCA
    #

    if (-not (Test-Path -Path $backupCompletoScript)) {
        throw "O script backup-completo.ps1 nao foi encontrado."
    }

    $documentos =
    [Environment]::GetFolderPath("MyDocuments")

    if ([string]::IsNullOrWhiteSpace($documentos)) {

        $documentos =
        Join-Path -Path $HOME -ChildPath "Documents"
    }

    $pastaSeguranca =
    Join-Path `
            -Path $documentos `
            -ChildPath "EstoqueBackups\pre-restauracao-completa"

    Write-Host ""
    Write-Host "Criando backup completo de seguranca..."
    Write-Host ""

    & powershell.exe `
        -NoProfile `
        -ExecutionPolicy Bypass `
        -File $backupCompletoScript `
        -BackupRoot $pastaSeguranca

    if ($LASTEXITCODE -ne 0) {
        throw "O backup de seguranca falhou. Recuperacao cancelada."
    }

    Write-Host ""
    Write-Host "Backup de seguranca concluido."

    #
    # PARAR API DO DOCKER
    #

    $apiRunning =
    docker inspect `
            -f "{{.State.Running}}" `
            $apiContainerId

    if ($apiRunning -eq "true") {

        Write-Host ""
        Write-Host "Parando API..."

        docker compose stop api

        if ($LASTEXITCODE -ne 0) {
            throw "Nao foi possivel parar a API."
        }

        $apiFoiParada = $true
    }

    #
    # RESTAURAR BANCO
    #

    Write-Host ""
    Write-Host "---------------------------------------"
    Write-Host " RESTAURANDO BANCO DE DADOS "
    Write-Host "---------------------------------------"
    Write-Host ""

    Write-Host "Encerrando conexoes existentes..."

    docker compose exec -T postgres `
        sh -c 'psql -U "$POSTGRES_USER" -d postgres -v ON_ERROR_STOP=1 -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = '\''$POSTGRES_DB'\'' AND pid <> pg_backend_pid();"'

    if ($LASTEXITCODE -ne 0) {
        throw "Nao foi possivel encerrar as conexoes com o banco."
    }

    Write-Host "Removendo banco atual..."

    docker compose exec -T postgres `
        sh -c 'dropdb -U "$POSTGRES_USER" --if-exists "$POSTGRES_DB"'

    if ($LASTEXITCODE -ne 0) {
        throw "Nao foi possivel remover o banco atual."
    }

    Write-Host "Criando banco vazio..."

    docker compose exec -T postgres `
        sh -c 'createdb -U "$POSTGRES_USER" "$POSTGRES_DB"'

    if ($LASTEXITCODE -ne 0) {
        throw "Nao foi possivel criar o banco vazio."
    }

    Write-Host "Aplicando backup..."

    docker compose exec -T postgres `
        sh -c 'pg_restore -U "$POSTGRES_USER" -d "$POSTGRES_DB" --no-owner --no-privileges /tmp/estoque_restore_completo.backup'

    if ($LASTEXITCODE -ne 0) {
        throw "Ocorreu um erro durante a restauracao do banco."
    }

    Write-Host ""
    Write-Host "Banco restaurado."

    #
    # RESTAURAR UPLOADS
    #

    Write-Host ""
    Write-Host "---------------------------------------"
    Write-Host " RESTAURANDO UPLOADS "
    Write-Host "---------------------------------------"
    Write-Host ""

    docker run `
        --rm `
        --volumes-from $apiContainerId `
        -v "${uploadsFolder}:/backup:ro" `
        alpine:3.20 `
        sh -c 'set -e; rm -rf /tmp/uploads-restore; mkdir -p /tmp/uploads-restore; tar -xzf "/backup/$1" -C /tmp/uploads-restore; find /app/uploads -mindepth 1 -maxdepth 1 -exec rm -rf {} +; cp -a /tmp/uploads-restore/. /app/uploads/' `
        -- `
        $arquivoUploads.Name

    if ($LASTEXITCODE -ne 0) {
        throw "Ocorreu um erro durante a restauracao dos uploads."
    }

    Write-Host "Uploads restaurados."

    #
    # LIMPEZA
    #

    docker compose exec -T postgres `
        rm -f $arquivoTemporarioBanco

    #
    # REINICIAR API SE ELA ESTAVA RODANDO
    #

    if ($apiFoiParada) {

        Write-Host ""
        Write-Host "Iniciando novamente a API..."

        docker compose start api

        if ($LASTEXITCODE -ne 0) {
            throw "A recuperacao terminou, mas a API nao conseguiu reiniciar."
        }

        $apiFoiParada = $false
    }

    Write-Host ""
    Write-Host "======================================="
    Write-Host " RECUPERACAO COMPLETA CONCLUIDA "
    Write-Host "======================================="
    Write-Host ""
    Write-Host "Banco restaurado: OK"
    Write-Host "Uploads restaurados: OK"
    Write-Host ""
    Write-Host "Origem:"
    Write-Host $backupFolderResolvido
    Write-Host ""
}
catch {

    Write-Host ""
    Write-Host "======================================="
    Write-Host " ERRO NA RECUPERACAO COMPLETA "
    Write-Host "======================================="
    Write-Host ""
    Write-Host $_.Exception.Message
    Write-Host ""

    if ($apiFoiParada) {

        Write-Host "A API permaneceu parada por seguranca."
        Write-Host ""
    }

    exit 1
}
finally {

    try {

        docker compose exec -T postgres `
            rm -f $arquivoTemporarioBanco `
            2>$null |
                Out-Null

    } catch {
    }

    Pop-Location
}