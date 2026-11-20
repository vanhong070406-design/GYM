# Tao GymManagementDB, nap du lieu mau, tao login gym_app va ghi file db.properties.
# Dung Windows Authentication nen can quyen sysadmin tren instance.
#   powershell -ExecutionPolicy Bypass -File database\setup_database.ps1 [-Server localhost] [-Reset]
# -Reset: XOA database hien co (mat toan bo du lieu) roi tao lai.
param(
    [string]$Server = "localhost",
    [switch]$Reset
)
$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
$sql = $PSScriptRoot
$propsFile = Join-Path $root "db.properties"

function Invoke-Sql([string[]]$SqlArgs) {
    & sqlcmd -S $Server -E -C -b -I -f 65001 @SqlArgs
    if ($LASTEXITCODE -ne 0) { throw "sqlcmd loi (exit $LASTEXITCODE): $SqlArgs" }
}

if ($Reset) {
    Write-Host "Xoa GymManagementDB..."
    Invoke-Sql @("-Q", "IF DB_ID('GymManagementDB') IS NOT NULL BEGIN ALTER DATABASE GymManagementDB SET SINGLE_USER WITH ROLLBACK IMMEDIATE; DROP DATABASE GymManagementDB; END")
}

# Database da co thi nang cap, giu nguyen du lieu; chua co thi tao moi
$exists = (& sqlcmd -S $Server -E -C -b -h -1 -W -Q "SET NOCOUNT ON; SELECT CASE WHEN DB_ID('GymManagementDB') IS NULL THEN 0 ELSE 1 END").Trim()
if ($exists -eq "1") {
    Write-Host "Nang cap database hien co..."
    Invoke-Sql @("-i", (Join-Path $sql "04_upgrade_roles.sql"))
    Invoke-Sql @("-i", (Join-Path $sql "05_equipment_status.sql"))
    Invoke-Sql @("-i", (Join-Path $sql "06_drop_manager.sql"))
    Invoke-Sql @("-i", (Join-Path $sql "07_login_positions.sql"))
} else {
    Write-Host "Tao schema..."
    Invoke-Sql @("-i", (Join-Path $sql "01_schema.sql"))
    Write-Host "Nap du lieu mau..."
    Invoke-Sql @("-i", (Join-Path $sql "02_seed.sql"))
}

# Dung lai mat khau cu neu da co db.properties; neu khong thi sinh ngau nhien
$password = $null
if (Test-Path $propsFile) {
    $line = Get-Content $propsFile | Where-Object { $_ -like "db.password=*" } | Select-Object -First 1
    if ($line) { $password = $line.Substring("db.password=".Length) }
}
if (-not $password) {
    $bytes = New-Object byte[] 18
    [Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
    $password = "Gym1a-" + (($bytes | ForEach-Object { $_.ToString("x2") }) -join "")
    $url = "jdbc:sqlserver://${Server}:1433;databaseName=GymManagementDB;encrypt=true;trustServerCertificate=true"
    $lines = @("db.url=$url", "db.user=gym_app", "db.password=$password")
    [IO.File]::WriteAllLines($propsFile, $lines)
}

Write-Host "Tao login gym_app..."
Invoke-Sql @("-v", "GYM_APP_PASSWORD=$password", "-i", (Join-Path $sql "03_app_login.sql"))
Write-Host "Hoan tat. Cau hinh ket noi: $propsFile"
