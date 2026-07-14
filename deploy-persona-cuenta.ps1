<#
.SYNOPSIS
    Descarga (clona o actualiza) persona-service + cuenta-service desde el
    monorepo y los despliega con Docker Compose en este equipo.

.PARAMETER RepoUrl
    URL del monorepo (https://... o git@...).

.PARAMETER Branch
    Rama a desplegar. Por defecto "main".

.PARAMETER CloneDir
    Carpeta local donde clonar/actualizar el repo.
    Por defecto "$HOME\proyectos\persona-cuenta".

.EXAMPLE
    .\deploy-persona-cuenta.ps1

.EXAMPLE
    .\deploy-persona-cuenta.ps1 -Branch develop -CloneDir "D:\dev\persona-cuenta"
#>

param(
    [string]$RepoUrl = "https://github.com/msmorales/test-monorepo.git",

    [string]$Branch = "main",

    [string]$CloneDir = "$HOME\proyectos\persona-cuenta"
)

$ErrorActionPreference = "Stop"

function Test-Comando {
    param([string]$Nombre)
    if (-not (Get-Command $Nombre -ErrorAction SilentlyContinue)) {
        Write-Host "ERROR: '$Nombre' no esta instalado o no esta en el PATH." -ForegroundColor Red
        exit 1
    }
}

Write-Host "== Verificando herramientas necesarias (git, mvn, docker) ==" -ForegroundColor Cyan
Test-Comando git
Test-Comando mvn
Test-Comando docker

# ------------------------------------------------------------------
# 1. Descargar / actualizar el codigo
# ------------------------------------------------------------------
if (Test-Path (Join-Path $CloneDir ".git")) {
    Write-Host "== Repositorio existente en $CloneDir, actualizando (git pull) ==" -ForegroundColor Cyan
    Push-Location $CloneDir
    git fetch origin
    git checkout $Branch
    git pull origin $Branch
    Pop-Location
} else {
    Write-Host "== Clonando repositorio en $CloneDir ==" -ForegroundColor Cyan
    git clone --branch $Branch $RepoUrl $CloneDir
}

# Busca docker-compose.yml dentro del repo, sin asumir en que nivel quedo
# (por si el clon crea una carpeta extra, ej. CloneDir\test-monorepo\docker-compose.yml).
# Se usan patrones por nivel (en vez de -Recurse) para NO entrar a .git,
# que tiene miles de archivos y puede hacer fallar o colgar la busqueda.
$ComposeFile = $null
$profundidadMaxima = 4
for ($nivel = 0; $nivel -le $profundidadMaxima -and -not $ComposeFile; $nivel++) {
    $comodines = @("*") * $nivel
    $patron = Join-Path $CloneDir ((($comodines + "docker-compose.yml") -join "\"))
    $ComposeFile = Get-ChildItem -Path $patron -File -ErrorAction SilentlyContinue | Select-Object -First 1
}

if (-not $ComposeFile) {
    Write-Host "ERROR: no se encontro docker-compose.yml dentro de $CloneDir (buscando hasta $profundidadMaxima niveles)." -ForegroundColor Red
    Write-Host "Contenido actual de $CloneDir :" -ForegroundColor Yellow
    Get-ChildItem -Path $CloneDir -Force | Select-Object Name, Mode | Format-Table | Out-String | Write-Host
    Write-Host "Revisa que RepoUrl/Branch apunten al monorepo correcto y que docker-compose.yml este commiteado." -ForegroundColor Red
    exit 1
}

$ProjectDir = $ComposeFile.DirectoryName
Write-Host "Usando docker-compose.yml en: $ProjectDir" -ForegroundColor DarkGray

if (-not (Test-Path "$ProjectDir\persona-service") -or -not (Test-Path "$ProjectDir\cuenta-service")) {
    Write-Host "ERROR: no se encontraron las carpetas persona-service/cuenta-service junto a $ComposeFile." -ForegroundColor Red
    Write-Host "Revisa que RepoUrl/Branch apunten al monorepo correcto." -ForegroundColor Red
    exit 1
}

# ------------------------------------------------------------------
# 2. Compilar ambos servicios
# ------------------------------------------------------------------
Write-Host "== Compilando persona-service ==" -ForegroundColor Cyan
Set-Location "$ProjectDir\persona-service"
mvn clean package -DskipTests
if ($LASTEXITCODE -ne 0) { Write-Host "FALLO el build de persona-service" -ForegroundColor Red; exit 1 }

Write-Host "== Compilando cuenta-service ==" -ForegroundColor Cyan
Set-Location "$ProjectDir\cuenta-service"
mvn clean package -DskipTests
if ($LASTEXITCODE -ne 0) { Write-Host "FALLO el build de cuenta-service" -ForegroundColor Red; exit 1 }

Set-Location $ProjectDir

# ------------------------------------------------------------------
# 3. Construir y levantar los contenedores
# ------------------------------------------------------------------
Write-Host "== Deteniendo contenedores previos (si existen) ==" -ForegroundColor Cyan
docker compose down

Write-Host "== Construyendo imagenes Docker ==" -ForegroundColor Cyan
docker compose build
if ($LASTEXITCODE -ne 0) { Write-Host "FALLO el build de las imagenes Docker" -ForegroundColor Red; exit 1 }

Write-Host "== Levantando los contenedores ==" -ForegroundColor Cyan
docker compose up -d
if ($LASTEXITCODE -ne 0) { Write-Host "FALLO al levantar los contenedores" -ForegroundColor Red; exit 1 }

# ------------------------------------------------------------------
# 4. Verificar que persona-service responde (health check)
# ------------------------------------------------------------------
Write-Host "== Esperando a que persona-service este listo ==" -ForegroundColor Cyan
$maxIntentos = 20
$intento = 0
$listo = $false

while ($intento -lt $maxIntentos -and -not $listo) {
    Start-Sleep -Seconds 3
    try {
        $resp = Invoke-WebRequest -Uri "http://localhost:8080/q/health/ready" -UseBasicParsing -TimeoutSec 3
        if ($resp.StatusCode -eq 200) { $listo = $true }
    } catch {
        # todavia no responde, reintentar
    }
    $intento++
}

if ($listo) {
    Write-Host ""
    Write-Host "Listo. persona-service respondiendo en http://localhost:8080" -ForegroundColor Green
    docker compose ps
} else {
    Write-Host ""
    Write-Host "Los contenedores estan arriba pero el health check no respondio a tiempo." -ForegroundColor Yellow
    Write-Host "Revisa los logs con: docker compose logs -f"
}
