#!/usr/bin/env bash
# -------------------------------------------------------------------
# seed-staff.sh — Seed fictional Avisos staff records for demos/RAG
#
# Usage:
#   ./scripts/seed-staff.sh              # dry-run preview
#   ./scripts/seed-staff.sh --yes        # upsert staff rows
#   ./scripts/seed-staff.sh --db ./data/avisos.db --yes
# -------------------------------------------------------------------

set -Eeuo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

DB_PATH=""
DB_KEY=""
DB_KEY_SOURCE=""
DB_CLIENT="${AVISOS_DB_CLIENT:-}"
DRY_RUN=true

log() { echo "[seed-staff] $*"; }
fail() {
    echo "[seed-staff] ERROR: $*" >&2
    exit 1
}

usage() {
    cat <<'EOF'
Usage: ./scripts/seed-staff.sh [options]

Dry-run by default. Add --yes to write staff records.

Options:
  --yes                  Execute seed/upsert.
  --db <path>             Override DB path. Default resolves from .env.example.
  --key <key>             Override SQLCipher key. Default reads .env.example.
  --client <command>      Override DB client. Default prefers sqlcipher, then sqlite3.
  -h, --help              Show this help.
EOF
}

sql_quote() {
    printf "'%s'" "$(printf "%s" "$1" | sed "s/'/''/g")"
}

load_dotenv_value() {
    local key="$1"
    local file="$PROJECT_ROOT/.env.example"
    local value=""

    if [ ! -f "$file" ]; then
        return
    fi

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
    fi
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
        DB_KEY_SOURCE="$PROJECT_ROOT/.env.example"
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
    fail "Neither sqlcipher nor sqlite3 was found. Install sqlcipher for encrypted DB writes."
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

write_seed_sql() {
    local sql_file="$1"

    {
        if uses_sqlcipher && [ -n "$DB_KEY" ]; then
            printf "PRAGMA key = %s;\n" "$(sql_quote "$DB_KEY")"
        fi
        cat <<'SQL'
CREATE TABLE IF NOT EXISTS staff (
    staff_id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    phone TEXT NOT NULL,
    role TEXT,
    jurisdiction TEXT,
    primary_zone TEXT,
    shift_name TEXT,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO staff (staff_id, name, email, phone, role, jurisdiction, primary_zone, shift_name) VALUES
('staff-ops-001', 'Jawad Azeem', 'jawad.azeem@outlook.com', '+1-555-0001', 'Operations Supervisor', 'All operations and facility oversight', 'All zones', 'Day'),
('staff-sec-001', 'Maya Chen', 'maya.chen@avisos.example', '+1-555-0101', 'Security Shift Lead', 'Physical security and access control', 'Lobby, exterior fence, mantrap, loading corridor', 'Day'),
('staff-sec-002', 'Omar Patel', 'omar.patel@avisos.example', '+1-555-0102', 'Security Responder', 'Floor security patrol', 'Rack halls A and B, service floor, emergency exits', 'Swing'),
('staff-sec-003', 'Lena Brooks', 'lena.brooks@avisos.example', '+1-555-0103', 'Night Security Lead', 'After-hours security response', 'All zones, exterior, electrical rooms', 'Night'),
('staff-fac-001', 'Andre Wallace', 'andre.wallace@avisos.example', '+1-555-0111', 'Facilities Lead', 'Mechanical, cooling, and water ingress', 'Mechanical room B, service floor, chilled water corridors', 'Day'),
('staff-fac-002', 'Priya Nair', 'priya.nair@avisos.example', '+1-555-0112', 'Facilities Technician', 'Environmental sensor response', 'Rack halls A and B, HVAC return aisles, underfloor sensors', 'Swing'),
('staff-net-001', 'Sofia Martinez', 'sofia.martinez@avisos.example', '+1-555-0121', 'Network Operations Lead', 'Network rooms and patching areas', 'Network room G, patch panels, transit corridor D', 'Day'),
('staff-net-002', 'Ethan Reed', 'ethan.reed@avisos.example', '+1-555-0122', 'Network Technician', 'Node communications and telemetry triage', 'Network room G, rack rows A1-A6, controller uplink', 'Night'),
('staff-elec-001', 'Hannah Kim', 'hannah.kim@avisos.example', '+1-555-0131', 'Electrical Safety Lead', 'Electrical rooms and power events', 'Electrical room E, UPS area, generator transfer corridor', 'Day')
ON CONFLICT(staff_id) DO UPDATE SET
    name = excluded.name,
    email = excluded.email,
    phone = excluded.phone,
    role = excluded.role,
    jurisdiction = excluded.jurisdiction,
    primary_zone = excluded.primary_zone,
    shift_name = excluded.shift_name,
    updated_at = CURRENT_TIMESTAMP;

.headers on
.mode column
SELECT staff_id, name, email, phone, role, shift_name
FROM staff
ORDER BY name ASC;
SQL
    } > "$sql_file"
}

main() {
    parse_args "$@"
    resolve_db_path
    resolve_key
    resolve_client

    mkdir -p "$(dirname "$DB_PATH")"

    log "DB: $DB_PATH"
    log "Mode: $([ "$DRY_RUN" = true ] && echo dry-run || echo seed)"
    log "Encryption key source: ${DB_KEY_SOURCE:-not configured}$([ -n "$DB_KEY" ] && ! uses_sqlcipher && echo ' (not applied by sqlite3)')"

    if [ "$DRY_RUN" = true ]; then
        log "Staff records to seed: 9"
        log "Dry run only. Re-run with --yes to write staff rows."
        return
    fi

    local sql_file
    sql_file="$(mktemp)"
    trap 'rm -f "$sql_file"' EXIT
    write_seed_sql "$sql_file"

    "$DB_CLIENT" "$DB_PATH" < "$sql_file"
    log "Staff seed complete."
}

main "$@"
