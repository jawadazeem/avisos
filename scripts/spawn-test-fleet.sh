#!/usr/bin/env bash
# -------------------------------------------------------------------
# spawn-test-fleet.sh — Build, spawn, and manage simulator+node pairs
#
# Usage:
#   ./scripts/spawn-test-fleet.sh build           # build node + simulator images
#   ./scripts/spawn-test-fleet.sh                  # spawn 10 pairs (default)
#   ./scripts/spawn-test-fleet.sh 1                # single pair for dev
#   ./scripts/spawn-test-fleet.sh 100              # load test
#   ./scripts/spawn-test-fleet.sh teardown         # remove all pairs
#   ./scripts/spawn-test-fleet.sh status           # show running pairs
#
# Prerequisites:
#   docker compose up -d                           # core infra (controller, mosquitto, etc.)
# -------------------------------------------------------------------

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

NETWORK="${AVISOS_DOCKER_NETWORK:-}"
COMPOSE_NETWORK_KEY="avisos-net"
NODE_IMAGE="avisos-node:latest"
SIM_IMAGE="avisos-hardware-simulator:latest"
LABEL="avisos.fleet=test"

MQTT_BROKER="tcp://mosquitto:1883"
MQTT_TOPIC="avisos/telemetry"

# -- helpers --

log()  { echo "[fleet] $*"; }
fail() { echo "[fleet] ERROR: $*" >&2; exit 1; }

ensure_network() {
    resolve_network

    if ! docker network inspect "$NETWORK" &>/dev/null; then
        fail "Docker network '$NETWORK' does not exist. Start the core stack first: docker compose up -d, or set AVISOS_DOCKER_NETWORK explicitly."
    fi
}

resolve_network() {
    if [ -n "$NETWORK" ]; then
        return
    fi

    local resolved
    resolved=$(
        docker compose -f "$PROJECT_ROOT/docker-compose.yml" config 2>/dev/null \
            | awk -v key="$COMPOSE_NETWORK_KEY" '
                /^networks:/ { in_networks = 1; next }
                in_networks && $1 == key ":" { in_target = 1; next }
                in_target && $1 == "name:" { print $2; exit }
            '
    )

    NETWORK="${resolved:-$COMPOSE_NETWORK_KEY}"
}

ensure_images() {
    local missing=()
    for img in "$NODE_IMAGE" "$SIM_IMAGE"; do
        if ! docker image inspect "$img" &>/dev/null; then
            missing+=("$img")
        fi
    done
    if [ ${#missing[@]} -gt 0 ]; then
        fail "Missing images: ${missing[*]}. Run: ./scripts/spawn-test-fleet.sh build"
    fi
}

# -- commands --

cmd_build() {
    log "Building node image..."
    docker build -t "$NODE_IMAGE" -f "$PROJECT_ROOT/node.Dockerfile" "$PROJECT_ROOT"

    log "Building simulator image..."
    docker build -t "$SIM_IMAGE" -f "$PROJECT_ROOT/hardware-simulator.Dockerfile" "$PROJECT_ROOT/avisos-hardware-simulator"

    log "Images built."
}

cmd_spawn() {
    local count="${1:-10}"
    ensure_network
    ensure_images

    log "Spawning $count simulator+node pair(s)..."

    local spawned=0
    for i in $(seq 1 "$count"); do
        local node_id
        local id
        node_id=$(uuidgen | tr '[:upper:]' '[:lower:]')
        id=$(echo "$node_id" | tr -d '-' | cut -c 1-6)
        local sim_name="sim-${id}"
        local node_name="node-${id}"

        # Start simulator (serves GET /readings on :5000)
        docker run -d \
            --name "$sim_name" \
            --network "$NETWORK" \
            --label "$LABEL" \
            --restart unless-stopped \
            "$SIM_IMAGE" > /dev/null

        # Start node (polls simulator, publishes telemetry over MQTT)
        docker run -d \
            --name "$node_name" \
            --network "$NETWORK" \
            --label "$LABEL" \
            --restart unless-stopped \
            -e HARDWARE_PROVIDER=simulator-rest \
            -e HARDWARE_SIMULATOR_BASE_URL="http://${sim_name}:5000" \
            -e MQTT_BROKER_URL="$MQTT_BROKER" \
            -e MQTT_TOPIC="$MQTT_TOPIC" \
            -e NODE_ID="$node_id" \
            -e NODE_NAME="$node_name" \
            -e NODE_TYPE=data-acquisition-device \
            "$NODE_IMAGE" > /dev/null

        spawned=$((spawned + 1))
        log "  [$spawned/$count] $node_name <-> $sim_name"
    done

    log "Done. $spawned pair(s) running."
}

cmd_teardown() {
    log "Tearing down fleet containers..."

    local containers
    containers=$(docker ps -aq --filter "label=$LABEL" || true)

    if [ -z "$containers" ]; then
        log "No fleet containers found."
        return
    fi

    local count
    count=$(echo "$containers" | wc -l | tr -d ' ')

    docker rm -f $containers > /dev/null
    log "Removed $count containers."
}

cmd_status() {
    local count
    count=$(docker ps -q --filter "label=$LABEL" 2>/dev/null | wc -l | tr -d ' ')

    if [ "$count" -eq 0 ]; then
        log "No fleet containers running."
        return
    fi

    docker ps --filter "label=$LABEL" --format "table {{.Names}}\t{{.Status}}\t{{.Image}}"
    log "$((count / 2)) pair(s) running."
}

# -- main --

case "${1:-}" in
    build)    cmd_build ;;
    teardown) cmd_teardown ;;
    status)   cmd_status ;;
    -h|--help)
        echo "Usage: $0 [build|count|teardown|status]"
        echo ""
        echo "  build        Build node + simulator Docker images"
        echo "  (no args)    Spawn 10 simulator+node pairs"
        echo "  <count>      Spawn N pairs (e.g. 1, 100, 1000)"
        echo "  teardown     Remove all fleet containers"
        echo "  status       Show running fleet containers"
        ;;
    *)        cmd_spawn "${1:-10}" ;;
esac
