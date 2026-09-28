param(
    [string]$BackupRoot
)

$ErrorActionPreference = "Stop"

$projectRoot =
Split-Path -Parent $PSScriptRoot

$databaseScript =
Join-Path -Path $PSScriptRoot -ChildPath "backup-database.ps1"

$uploadsScript =
Join-Path -Path $PSScriptRoot -ChildPath "backup-uploads.ps1"

if (-not (Test-Path -Path $databaseScript)) {
    throw "O script backup-database.ps1 nao foi encontrado."
}

if (-not (Test-Path -Path $uploadsScript)) {
    throw "O script backup-uploads.ps1 nao foi encontrado."
}

if ([string]::IsNullOrWhiteSpace($BackupRoot)) {

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

    $BackupRoot =
    Join-Path `
            -Path $documentos `
            -ChildPath "EstoqueBackups"
}

$dataHora =
Get-Date -Format "yyyyMMdd_HHmmss"

$pastaCompletos =
Join-Path `
        -Path $BackupRoot `
        -ChildPath "completos"

$pastaBackupAtual =
Join-Path `
        -Path $pastaCompletos `
        -ChildPath "backup_$dataHora"

New-Item `
    -ItemType Directory `
    -Force `
    -Path $pastaBackupAtual |
        Out-Null

$manifesto =
Join-Path `
        -Path $pastaBackupAtual `
        -ChildPath "backup-info.txt"

Write-Host ""
Write-Host "======================================="
Write-Host " BACKUP COMPLETO - CONTROLE DE ESTOQUE "
Write-Host "======================================="
Write-Host ""
Write-Host "Destino:"
Write-Host $pastaBackupAtual
Write-Host ""

Push-Location -Path $projectRoot

try {

    Write-Host "---------------------------------------"
    Write-Host " ETAPA 1/2 - BANCO DE DADOS"
    Write-Host "---------------------------------------"
    Write-Host ""

    & powershell.exe `
        -NoProfile `
        -ExecutionPolicy Bypass `
        -File $databaseScript `
        -BackupRoot $pastaBackupAtual

    if ($LASTEXITCODE -ne 0) {
        throw "O backup do banco de dados falhou."
    }

    Write-Host ""
    Write-Host "Backup do banco concluido."
    Write-Host ""

    Write-Host "---------------------------------------"
    Write-Host " ETAPA 2/2 - UPLOADS"
    Write-Host "---------------------------------------"
    Write-Host ""

    & powershell.exe `
        -NoProfile `
        -ExecutionPolicy Bypass `
        -File $uploadsScript `
        -BackupRoot $pastaBackupAtual

    if ($LASTEXITCODE -ne 0) {
        throw "O backup dos uploads falhou."
    }

    Write-Host ""
    Write-Host "Backup dos uploads concluido."
    Write-Host ""

    $arquivosDatabase =
    Get-ChildItem `
            -Path (
    Join-Path `
                    -Path $pastaBackupAtual `
                    -ChildPath "database"
    ) `
            -File `
            -ErrorAction SilentlyContinue

    $arquivosUploads =
    Get-ChildItem `
            -Path (
    Join-Path `
                    -Path $pastaBackupAtual `
                    -ChildPath "uploads"
    ) `
            -File `
            -ErrorAction SilentlyContinue

    if (
    $null -eq $arquivosDatabase -or
            $arquivosDatabase.Count -eq 0
    ) {
        throw "Nenhum backup de banco foi encontrado."
    }

    if (
    $null -eq $arquivosUploads -or
            $arquivosUploads.Count -eq 0
    ) {
        throw "Nenhum backup de uploads foi encontrado."
    }

    $tamanhoTotal =
    (
    $arquivosDatabase |
            Measure-Object `
                -Property Length `
                -Sum
    ).Sum +
            (
            $arquivosUploads |
                    Measure-Object `
                -Property Length `
                -Sum
            ).Sum

    $tamanhoTotalMb =
    [Math]::Round(
            $tamanhoTotal / 1MB,
            2
    )

    $dataBackup =
    Get-Date -Format "dd/MM/yyyy HH:mm:ss"

    @"
CONTROLE DE ESTOQUE
BACKUP COMPLETO

Data/Hora:
$dataBackup

Pasta:
$pastaBackupAtual

Banco de dados:
$($arquivosDatabase.Name -join ", ")

Uploads:
$($arquivosUploads.Name -join ", ")

Tamanho total:
$tamanhoTotalMb MB

Status:
BACKUP CONCLUIDO COM SUCESSO
"@ |
            Set-Content `
            -Path $manifesto `
            -Encoding UTF8

    Write-Host ""
    Write-Host "======================================="
    Write-Host " BACKUP COMPLETO CONCLUIDO "
    Write-Host "======================================="
    Write-Host ""
    Write-Host "Pasta:"
    Write-Host $pastaBackupAtual
    Write-Host ""
    Write-Host "Tamanho total: $tamanhoTotalMb MB"
    Write-Host ""
    Write-Host "Manifesto:"
    Write-Host $manifesto
    Write-Host ""
}
catch {

    Write-Host ""
    Write-Host "======================================="
    Write-Host " ERRO NO BACKUP COMPLETO "
    Write-Host "======================================="
    Write-Host ""
    Write-Host $_.Exception.Message
    Write-Host ""

    exit 1
}
finally {

    Pop-Location
}