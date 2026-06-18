#!/usr/bin/env bash
# -------------------------------------------------------------------
# purge-test-nodes.sh — Local/operator DB maintenance for demo fleets
#
# Dry-run by default. Deletes require --yes.
#
# Common usage:
#   ./scripts/purge-test-nodes.sh
#   ./scripts/purge-test-nodes.sh --yes
#   ./scripts/purge-test-nodes.sh --include-demo --yes
#   ./scripts/purge-test-nodes.sh --name A-A2-ENV-01 --yes
#   ./scripts/purge-test-nodes.sh --include-related --yes
# -------------------------------------------------------------------

set -Eeuo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

DB_PATH=""
DB_KEY=""
DB_KEY_SOURCE=""
DB_CLIENT="${AVISOS_DB_CLIENT:-}"
DRY_RUN=true
INCLUDE_DEMO=false
INCLUDE_RELATED=false
OFFLINE_ONLY=false
TARGET_UUIDS=()
TARGET_NAMES=()

DEMO_NODE_IDS=(
    "00000000-0000-4000-8000-000000000001"
    "00000000-0000-4000-8000-000000000002"
    "00000000-0000-4000-8000-000000000003"
    "00000000-0000-4000-8000-000000000004"
    "00000000-0000-4000-8000-000000000005"
    "00000000-0000-4000-8000-000000000006"
    "00000000-0000-4000-8000-000000000007"
    "00000000-0000-4000-8000-000000000008"
    "00000000-0000-4000-8000-000000000009"
    "00000000-0000-4000-8000-000000000010"
    "00000000-0000-4000-8000-000000000011"
    "00000000-0000-4000-8000-000000000012"
    "00000000-0000-4000-8000-000000000013"
    "00000000-0000-4000-8000-000000000014"
    "00000000-0000-4000-8000-000000000015"
    "00000000-0000-4000-8000-000000000016"
    "00000000-0000-4000-8000-000000000017"
    "00000000-0000-4000-8000-000000000018"
    "00000000-0000-4000-8000-000000000019"
    "00000000-0000-4000-8000-000000000020"
)

log()  { echo "[purge-nodes] $*"; }
fail() { echo "[purge-nodes] ERROR: $*" >&2; exit 1; }

usage() {
    cat <<'EOF'
Usage: ./scripts/purge-test-nodes.sh [options]

Dry-run by default. Add --yes to delete.

Default target:
  All non-deterministic nodes, meaning load-test/random fleet nodes whose UUID
  is not one of the canonical 20 demo fleet UUIDs.

Options:
  --yes                  Execute deletion. Without this, only prints matches.
  --include-demo          Include deterministic 20-node demo fleet in broad purge.
  --include-related       Also delete matching rows from alarms and telemetry_audit.
  --offline-only          Only target nodes currently marked OFFLINE.
  --uuid <uuid>           Target a specific UUID. Can be repeated.
  --name <name>           Target a specific node name. Can be repeated.
  --db <path>             Override DB path. Default resolves from env or ./data/avisos.db.
  --key <key>             Override SQLCipher key. Default reads .env.example, then env.
  --client <command>      Override DB client. Default prefers sqlcipher, then sqlite3.
  -h, --help              Show this help.

Examples:
  ./scripts/purge-test-nodes.sh
  ./scripts/purge-test-nodes.sh --yes
  ./scripts/purge-test-nodes.sh --offline-only --yes
  ./scripts/purge-test-nodes.sh --include-demo --yes
  ./scripts/purge-test-nodes.sh --name A-A2-ENV-01 --yes
  ./scripts/purge-test-nodes.sh --uuid 00000000-0000-4000-8000-000000000001 --yes
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

dotenv_source_for() {
    local key="$1"
    local file
    for file in "$PROJECT_ROOT/.env.example"; do
        if [ -f "$file" ] && awk -v key="$key" '
            /^[[:space:]]*#/ || /^[[:space:]]*$/ { next }
            {
                line = $0
                sub(/^[[:space:]]*export[[:space:]]+/, "", line)
                split(line, parts, "=")
                name = parts[1]
                gsub(/^[[:space:]]+|[[:space:]]+$/, "", name)
                if (name == key) {
                    found = 1
                    exit
                }
            }
            END { exit found ? 0 : 1 }
        ' "$file"; then
            printf "%s" "$file"
            return
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

resolve_key() {
    if [ -n "$DB_KEY" ]; then
        return
    fi
    DB_KEY="$(load_dotenv_value DATABASE_ENCRYPTION_KEY || true)"
    if [ -n "$DB_KEY" ]; then
        DB_KEY_SOURCE="$(dotenv_source_for DATABASE_ENCRYPTION_KEY)"
        return
    fi

    DB_KEY="${DATABASE_ENCRYPTION_KEY:-}"
    if [ -n "$DB_KEY" ]; then
        DB_KEY_SOURCE="environment:DATABASE_ENCRYPTION_KEY"
    fi
}

resolve_client() {
    if [ -n "$DB_CLIENT" ]; then
        command -v "$DB_CLIENT" >/dev/null 2>&1 || fail "Configured DB client '$DB_CLIENT' was not found."
        return
    fi
    if command -v sqlcipher >/dev/null 2>&1; then
        DB_CLIENT="sqlcipher"
        return
    fi
    if command -v sqlite3 >/dev/null 2>&1; then
        DB_CLIENT="sqlite3"
        return
    fi
    fail "Neither sqlcipher nor sqlite3 was found. Install sqlcipher for the encrypted Avisos DB."
}

build_target_where() {
    local clauses=()

    if [ "${#TARGET_UUIDS[@]}" -gt 0 ]; then
        clauses+=("uuid IN ($(csv_quote_array "${TARGET_UUIDS[@]}"))")
    fi

    if [ "${#TARGET_NAMES[@]}" -gt 0 ]; then
        clauses+=("name IN ($(csv_quote_array "${TARGET_NAMES[@]}"))")
    fi

    if [ "${#clauses[@]}" -eq 0 ]; then
        if [ "$INCLUDE_DEMO" = true ]; then
            clauses+=("1 = 1")
        else
            clauses+=("uuid NOT IN ($(csv_quote_array "${DEMO_NODE_IDS[@]}"))")
        fi
    fi

    if [ "$OFFLINE_ONLY" = true ]; then
        clauses+=("status = 'OFFLINE'")
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

uses_sqlcipher() {
    [ "$(basename "$DB_CLIENT")" = "sqlcipher" ]
}

parse_args() {
    while [ "$#" -gt 0 ]; do
        case "$1" in
            --yes)
                DRY_RUN=false
                shift
                ;;
            --include-demo)
                INCLUDE_DEMO=true
                shift
                ;;
            --include-related)
                INCLUDE_RELATED=true
                shift
                ;;
            --offline-only)
                OFFLINE_ONLY=true
                shift
                ;;
            --uuid)
                [ "${2:-}" ] || fail "--uuid requires a value."
                TARGET_UUIDS+=("$2")
                shift 2
                ;;
            --name)
                [ "${2:-}" ] || fail "--name requires a value."
                TARGET_NAMES+=("$2")
                shift 2
                ;;
            --db)
                [ "${2:-}" ] || fail "--db requires a value."
                DB_PATH="$2"
                shift 2
                ;;
            --key)
                [ "${2:-}" ] || fail "--key requires a value."
                DB_KEY="$2"
                DB_KEY_SOURCE="--key"
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
    resolve_key
    resolve_client

    [ -f "$DB_PATH" ] || fail "Database file not found: $DB_PATH"

    local where_clause
    where_clause="$(build_target_where)"

    local sql_file
    sql_file="$(mktemp)"
    trap "rm -f $(sql_quote "$sql_file")" EXIT

    {
        if uses_sqlcipher && [ -n "$DB_KEY" ]; then
            if [[ "$DB_KEY" =~ ^[0-9a-fA-F]{64}$ ]]; then
                printf "PRAGMA key = 'x%s';\n" "$DB_KEY"
            else
                printf "PRAGMA key = %s;\n" "$(sql_quote "$DB_KEY")"
            fi
        fi
        cat <<SQL
.headers on
.mode column

SELECT uuid, name, status, battery_level, last_seen
FROM nodes
WHERE $where_clause
ORDER BY last_seen DESC, name ASC;

SELECT COUNT(*) AS nodes_targeted
FROM nodes
WHERE $where_clause;
SQL
        if [ "$INCLUDE_RELATED" = true ]; then
            cat <<SQL

SELECT COUNT(*) AS alarms_targeted
FROM alarms
WHERE device_uuid IN (SELECT uuid FROM nodes WHERE $where_clause);

SELECT COUNT(*) AS telemetry_rows_targeted
FROM telemetry_audit
WHERE device_uuid IN (SELECT uuid FROM nodes WHERE $where_clause);
SQL
        fi
        if [ "$DRY_RUN" = false ]; then
            if [ "$INCLUDE_RELATED" = true ]; then
                cat <<SQL

DELETE FROM alarms
WHERE device_uuid IN (SELECT uuid FROM nodes WHERE $where_clause);

DELETE FROM telemetry_audit
WHERE device_uuid IN (SELECT uuid FROM nodes WHERE $where_clause);
SQL
            fi
            cat <<SQL

DELETE FROM nodes
WHERE $where_clause;

SELECT changes() AS nodes_deleted;
SQL
        fi
    } > "$sql_file"

    log "DB: $DB_PATH"
    log "Mode: $([ "$DRY_RUN" = true ] && echo dry-run || echo delete)"
    log "Target: $([ "$INCLUDE_DEMO" = true ] && echo all matching nodes || echo non-demo/load-test nodes by default)"
    log "Related rows: $([ "$INCLUDE_RELATED" = true ] && echo included || echo left intact)"
    log "Encryption key source: ${DB_KEY_SOURCE:-not configured}$([ -n "$DB_KEY" ] && ! uses_sqlcipher && echo ' (not applied by sqlite3)')"

    run_sql "$sql_file"

    if [ "$DRY_RUN" = true ]; then
        log "Dry run only. Re-run with --yes to delete these node rows."
    else
        log "Deletion complete."
    fi
}

main "$@"
