#!/usr/bin/env bash
# -------------------------------------------------------------------
# purge-alarms.sh — Local/operator DB maintenance for alarm rows
#
# Dry-run by default. Deletes require --yes.
#
# Common usage:
#   ./scripts/purge-alarms.sh
#   ./scripts/purge-alarms.sh --yes
#   ./scripts/purge-alarms.sh --status RESOLVED --yes
#   ./scripts/purge-alarms.sh --device 00000000-0000-4000-8000-000000000001 --yes
# -------------------------------------------------------------------

set -Eeuo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

DB_PATH=""
DB_CLIENT="${AVISOS_DB_CLIENT:-sqlite3}"
DRY_RUN=true
VACUUM_AFTER=false
TARGET_STATUSES=()
TARGET_DEVICES=()

log()  { echo "[purge-alarms] $*"; }
fail() { echo "[purge-alarms] ERROR: $*" >&2; exit 1; }

usage() {
    cat <<'EOF'
Usage: ./scripts/purge-alarms.sh [options]

Dry-run by default. Add --yes to delete.

Default target:
  All rows in the alarms table.

Options:
  --yes                  Execute deletion. Without this, only prints counts.
  --status <status>      Target a status, e.g. ACTIVE or RESOLVED. Can be repeated.
  --device <uuid>        Target alarms for one device UUID. Can be repeated.
  --vacuum               Run VACUUM after deleting rows.
  --db <path>            Override DB path. Default resolves from env or ./data/avisos.db.
  --client <command>     Override DB client. Default: sqlite3.
  -h, --help             Show this help.

Examples:
  ./scripts/purge-alarms.sh
  ./scripts/purge-alarms.sh --yes
  ./scripts/purge-alarms.sh --status RESOLVED --yes
  ./scripts/purge-alarms.sh --device 00000000-0000-4000-8000-000000000001 --yes
EOF
}

sql_quote() {
    printf "'%s'" "$(printf "%s" "$1" | sed "s/'/''/g")"
}

csv_quote_array() {
    local first=true
    local value
    for value in "$@"; do
        if [ "$first" = true ]; then
            first=false
        else
            printf ", "
        fi
        sql_quote "$value"
    done
}

load_dotenv_value() {
    local key="$1"
    local file
    local value
    for file in "$PROJECT_ROOT/.env.example"; do
        if [ -f "$file" ]; then
            value="$(awk -v key="$key" '
                /^[[:space:]]*#/ || /^[[:space:]]*$/ { next }
                {
                    line = $0
                    sub(/^[[:space:]]*export[[:space:]]+/, "", line)
                    split(line, parts, "=")
                    name = parts[1]
                    gsub(/^[[:space:]]+|[[:space:]]+$/, "", name)
                    if (name != key) {
                        next
                    }
                    sub(/^[^=]*=/, "", line)
                    sub(/[[:space:]]+#.*$/, "", line)
                    gsub(/^[[:space:]]+|[[:space:]]+$/, "", line)
                    if ((line ~ /^".*"$/) || (line ~ /^'\''.*'\''$/)) {
                        line = substr(line, 2, length(line) - 2)
                    }
                    print line
                    exit
                }
            ' "$file")"
            if [ -n "$value" ]; then
                printf "%s" "$value"
                return
            fi
        fi
    done
}

resolve_db_path() {
    if [ -n "$DB_PATH" ]; then
        return
    fi

    local url
    url="$(load_dotenv_value AVISOS_DATABASE_URL || true)"
    if [ -z "$url" ]; then
        url="${AVISOS_DATABASE_URL:-}"
    fi

    case "$url" in
        jdbc:sqlite:/app/data/*)
            DB_PATH="$PROJECT_ROOT/data/${url#jdbc:sqlite:/app/data/}"
            ;;
        jdbc:sqlite:/*)
            DB_PATH="${url#jdbc:sqlite:}"
            ;;
        jdbc:sqlite:*)
            DB_PATH="$PROJECT_ROOT/${url#jdbc:sqlite:}"
            ;;
        *)
            DB_PATH="$PROJECT_ROOT/data/avisos.db"
            ;;
    esac
}

resolve_client() {
    if [ -n "$DB_CLIENT" ]; then
        command -v "$DB_CLIENT" >/dev/null 2>&1 || fail "Configured DB client '$DB_CLIENT' was not found."
        return
    fi
    if command -v sqlite3 >/dev/null 2>&1; then
        DB_CLIENT="sqlite3"
        return
    fi
    fail "sqlite3 was not found. Install sqlite3 for Avisos DB maintenance."
}

build_target_where() {
    local clauses=()

    if [ "${#TARGET_STATUSES[@]}" -gt 0 ]; then
        clauses+=("status IN ($(csv_quote_array "${TARGET_STATUSES[@]}"))")
    fi

    if [ "${#TARGET_DEVICES[@]}" -gt 0 ]; then
        clauses+=("device_uuid IN ($(csv_quote_array "${TARGET_DEVICES[@]}"))")
    fi

    if [ "${#clauses[@]}" -eq 0 ]; then
        printf "1 = 1"
        return
    fi

    local first=true
    local clause
    for clause in "${clauses[@]}"; do
        if [ "$first" = true ]; then
            first=false
        else
            printf " AND "
        fi
        printf "(%s)" "$clause"
    done
}

run_sql() {
    local sql_file="$1"
    "$DB_CLIENT" "$DB_PATH" < "$sql_file"
}

parse_args() {
    while [ "$#" -gt 0 ]; do
        case "$1" in
            --yes)
                DRY_RUN=false
                shift
                ;;
            --status)
                [ "${2:-}" ] || fail "--status requires a value."
                TARGET_STATUSES+=("$(printf "%s" "$2" | tr '[:lower:]' '[:upper:]')")
                shift 2
                ;;
            --device)
                [ "${2:-}" ] || fail "--device requires a value."
                TARGET_DEVICES+=("$2")
                shift 2
                ;;
            --vacuum)
                VACUUM_AFTER=true
                shift
                ;;
            --db)
                [ "${2:-}" ] || fail "--db requires a value."
                DB_PATH="$2"
                shift 2
                ;;
            --client)
                [ "${2:-}" ] || fail "--client requires a value."
                DB_CLIENT="$2"
                shift 2
                ;;
            -h|--help)
                usage
                exit 0
                ;;
            *)
                fail "Unknown argument: $1"
                ;;
        esac
    done
}

main() {
    parse_args "$@"
    resolve_db_path
    resolve_client

    [ -f "$DB_PATH" ] || fail "Database file not found: $DB_PATH"

    local where_clause
    where_clause="$(build_target_where)"

    local sql_file
    sql_file="$(mktemp)"
    trap "rm -f $(sql_quote "$sql_file")" EXIT

    {
        cat <<SQL
.headers on
.mode column

SELECT status, COUNT(*) AS alarm_count
FROM alarms
GROUP BY status
ORDER BY status;

SELECT COUNT(*) AS alarms_targeted
FROM alarms
WHERE $where_clause;

SELECT id, device_uuid, severity, reason, status, triggered_at, s3_image_key
FROM alarms
WHERE $where_clause
ORDER BY triggered_at DESC
LIMIT 20;
SQL
        if [ "$DRY_RUN" = false ]; then
            cat <<SQL

DELETE FROM alarms
WHERE $where_clause;

SELECT changes() AS alarms_deleted;
SQL
            if [ "$VACUUM_AFTER" = true ]; then
                cat <<SQL

VACUUM;
SQL
            fi
        fi
    } > "$sql_file"

    log "DB: $DB_PATH"
    log "Mode: $([ "$DRY_RUN" = true ] && echo dry-run || echo delete)"
    log "DB client: $DB_CLIENT"
    log "Target: $where_clause"
    log "Vacuum: $([ "$VACUUM_AFTER" = true ] && echo yes || echo no)"

    run_sql "$sql_file"

    if [ "$DRY_RUN" = true ]; then
        log "Dry run only. Re-run with --yes to delete these alarm rows."
    else
        log "Deletion complete."
    fi
}

main "$@"
