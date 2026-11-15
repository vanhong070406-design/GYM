#!/usr/bin/env bash
# Ban macOS/Linux cua setup_database.ps1: chay SQL Server trong Docker, tao GymManagementDB,
# nap du lieu mau, tao login gym_app va ghi file db.properties.
#   ./database/setup_database.sh           # tao moi, hoac nang cap neu database da co
#   ./database/setup_database.sh --reset   # XOA database hien co (mat toan bo du lieu) roi tao lai
set -euo pipefail

CONTAINER="gym-sql"
IMAGE="mcr.microsoft.com/mssql/server:2022-latest"
VOLUME="gym-sql-data"
PORT=1433

SQL_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT="$(dirname "$SQL_DIR")"
PROPS="$ROOT/db.properties"
SA_FILE="$ROOT/.mssql_sa_password"   # mat khau sa cua container, da them vao .gitignore

RESET=0
[[ "${1:-}" == "--reset" ]] && RESET=1

if ! docker info >/dev/null 2>&1; then
    echo "Docker chua chay. Mo Docker Desktop, doi bieu tuong ca voi dung yen roi chay lai." >&2
    exit 1
fi

# Mat khau sa: dung lai neu da co, neu khong thi sinh ngau nhien (du do phuc tap SQL Server yeu cau)
if [[ -f "$SA_FILE" ]]; then
    SA_PASSWORD="$(cat "$SA_FILE")"
else
    SA_PASSWORD="Sa1a-$(openssl rand -hex 16)"
    printf '%s' "$SA_PASSWORD" > "$SA_FILE"
    chmod 600 "$SA_FILE"
fi

# Tao container neu chua co, bat lai neu dang tat
if ! docker container inspect "$CONTAINER" >/dev/null 2>&1; then
    echo "Tao container $CONTAINER (lan dau phai tai image khoang 1.5GB)..."
    docker run -d --name "$CONTAINER" --platform linux/amd64 \
        -e ACCEPT_EULA=Y -e MSSQL_SA_PASSWORD="$SA_PASSWORD" \
        -p "127.0.0.1:$PORT:1433" -v "$VOLUME:/var/opt/mssql" \
        "$IMAGE" >/dev/null
elif [[ "$(docker container inspect -f '{{.State.Running}}' "$CONTAINER")" != "true" ]]; then
    echo "Bat container $CONTAINER..."
    docker start "$CONTAINER" >/dev/null
fi

# Ban 2022 moi dung mssql-tools18, ban cu dung mssql-tools
SQLCMD="/opt/mssql-tools18/bin/sqlcmd"
docker exec "$CONTAINER" test -x "$SQLCMD" 2>/dev/null || SQLCMD="/opt/mssql-tools/bin/sqlcmd"

sql() { docker exec "$CONTAINER" "$SQLCMD" -S localhost -U sa -P "$SA_PASSWORD" -C -b -I -f 65001 "$@"; }

echo -n "Doi SQL Server khoi dong"
for i in $(seq 1 60); do
    if sql -Q "SELECT 1" >/dev/null 2>&1; then echo " xong."; break; fi
    if [[ $i -eq 60 ]]; then
        echo; echo "SQL Server khong khoi dong duoc. Xem log: docker logs $CONTAINER" >&2
        exit 1
    fi
    echo -n "."; sleep 2
done

# Chep script vao container de sqlcmd doc
docker exec "$CONTAINER" mkdir -p /tmp/gymdb
docker cp "$SQL_DIR/." "$CONTAINER:/tmp/gymdb/" >/dev/null

if [[ $RESET -eq 1 ]]; then
    echo "Xoa GymManagementDB..."
    sql -Q "IF DB_ID('GymManagementDB') IS NOT NULL BEGIN ALTER DATABASE GymManagementDB SET SINGLE_USER WITH ROLLBACK IMMEDIATE; DROP DATABASE GymManagementDB; END"
fi

# Database da co thi nang cap, giu nguyen du lieu; chua co thi tao moi
EXISTS="$(sql -h -1 -W -Q "SET NOCOUNT ON; SELECT CASE WHEN DB_ID('GymManagementDB') IS NULL THEN 0 ELSE 1 END" | tr -d '[:space:]')"
if [[ "$EXISTS" == "1" ]]; then
    echo "Nang cap database hien co..."
    sql -i /tmp/gymdb/04_upgrade_roles.sql
    sql -i /tmp/gymdb/05_equipment_status.sql
else
    echo "Tao schema..."
    sql -i /tmp/gymdb/01_schema.sql
    echo "Nap du lieu mau..."
    sql -i /tmp/gymdb/02_seed.sql
fi

# Dung lai mat khau gym_app cu neu da co db.properties; neu khong thi sinh ngau nhien
APP_PASSWORD=""
if [[ -f "$PROPS" ]]; then
    APP_PASSWORD="$(grep '^db.password=' "$PROPS" | head -1 | cut -d= -f2- || true)"
fi
if [[ -z "$APP_PASSWORD" ]]; then
    APP_PASSWORD="Gym1a-$(openssl rand -hex 18)"
    cat > "$PROPS" <<EOF
db.url=jdbc:sqlserver://localhost:$PORT;databaseName=GymManagementDB;encrypt=true;trustServerCertificate=true
db.user=gym_app
db.password=$APP_PASSWORD
EOF
fi

echo "Tao login gym_app..."
sql -v GYM_APP_PASSWORD="$APP_PASSWORD" -i /tmp/gymdb/03_app_login.sql
echo "Hoan tat. Cau hinh ket noi: $PROPS"
