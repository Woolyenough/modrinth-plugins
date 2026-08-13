#!/usr/bin/env bash
# Build and launch the local Paper test server with one or more repository plugins.
set -euo pipefail

PROJECT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
SERVER_DIR="${RUNSERVER_DIR:-$PROJECT_DIR/run}"
PLUGINS_DIR="$SERVER_DIR/plugins"
DEFAULT_MINECRAFT_VERSION="1.21.11"
MINECRAFT_VERSION="${RUNSERVER_MINECRAFT_VERSION:-$DEFAULT_MINECRAFT_VERSION}"
MINECRAFT_VERSION_CONFIGURED="${RUNSERVER_MINECRAFT_VERSION:+true}"
EULA_ANSWER="${RUNSERVER_ACCEPT_EULA:-}"

usage() {
    cat <<'EOF'
Usage: ./runserver.sh [OPTIONS] [all|PLUGIN ...]

Builds the requested plugin modules and starts a local Paper test server.

Plugins: CombatTag, CrystalDamageModifier, CustomMOTD, FlySpeed, PlayerFreeze

Examples:
  ./runserver.sh                         # choose interactively
  ./runserver.sh all                     # all plugins
  ./runserver.sh FlySpeed                # only FlySpeed
  ./runserver.sh FlySpeed PlayerFreeze   # the two named plugins
  ./runserver.sh --minecraft-version 1.21.8 FlySpeed
  ./runserver.sh --accept-eula all

Options:
  --accept-eula[=yes|no]          Accept the Minecraft EULA without prompting.
  --minecraft-version VERSION     Paper Minecraft version (default: 1.21.11).
  -h, --help                       Show this help.

Environment variables:
  RUNSERVER_ACCEPT_EULA=yes|no
  RUNSERVER_MINECRAFT_VERSION=VERSION
  RUNSERVER_PLUGINS=all|PLUGIN[,PLUGIN...]
  RUNSERVER_DIR=PATH
EOF
}

POSITIONAL_ARGUMENTS=()
while [[ $# -gt 0 ]]; do
    case "$1" in
        -h|--help)
            usage
            exit 0
            ;;
        --accept-eula)
            EULA_ANSWER=yes
            shift
            ;;
        --accept-eula=*)
            EULA_ANSWER="${1#*=}"
            shift
            ;;
        --minecraft-version)
            if [[ $# -lt 2 || "$2" == --* ]]; then
                echo "--minecraft-version requires a version." >&2
                exit 2
            fi
            MINECRAFT_VERSION="$2"
            MINECRAFT_VERSION_CONFIGURED=true
            shift 2
            ;;
        --minecraft-version=*)
            MINECRAFT_VERSION="${1#*=}"
            MINECRAFT_VERSION_CONFIGURED=true
            shift
            ;;
        --)
            shift
            POSITIONAL_ARGUMENTS+=("$@")
            break
            ;;
        -*)
            echo "Unknown option: $1" >&2
            usage >&2
            exit 2
            ;;
        *)
            POSITIONAL_ARGUMENTS+=("$1")
            shift
            ;;
    esac
done
set -- "${POSITIONAL_ARGUMENTS[@]}"

if [[ $# -eq 0 && -n "${RUNSERVER_PLUGINS:-}" ]]; then
    IFS=', ' read -r -a environment_plugins <<<"$RUNSERVER_PLUGINS"
    set -- "${environment_plugins[@]}"
fi

declare -A MODULES=(
    [combattag]=CombatTag
    [combat-tag]=CombatTag
    [crystaldamagemodifier]=CrystalDamageModifier
    [crystal-damage-modifier]=CrystalDamageModifier
    [custommotd]=CustomMOTD
    [custom-motd]=CustomMOTD
    [flyspeed]=FlySpeed
    [fly-speed]=FlySpeed
    [playerfreeze]=PlayerFreeze
    [player-freeze]=PlayerFreeze
)
declare -A JAR_NAMES=(
    [CombatTag]=combat-tag.jar
    [CrystalDamageModifier]=crystal-damage-modifier.jar
    [CustomMOTD]=custom-motd.jar
    [FlySpeed]=fly-speed.jar
    [PlayerFreeze]=player-freeze.jar
)
ALL_MODULES=(CombatTag CrystalDamageModifier CustomMOTD FlySpeed PlayerFreeze)

SELECTED_MODULES=()
if [[ $# -eq 0 ]]; then
    if [[ ! -t 0 ]]; then
        echo "Choose a plugin explicitly when running without an interactive terminal." >&2
        usage >&2
        exit 2
    fi

    echo "What plugin do you want to runserver with?"
    select selected in "All plugins" "${ALL_MODULES[@]}"; do
        case "$selected" in
            "All plugins")
                SELECTED_MODULES=("${ALL_MODULES[@]}")
                break
                ;;
            CombatTag|CrystalDamageModifier|CustomMOTD|FlySpeed|PlayerFreeze)
                SELECTED_MODULES=("$selected")
                break
                ;;
            *)
                echo "Please choose a number from the list." >&2
                ;;
        esac
    done
elif [[ "$1" == "all" ]]; then
    if [[ $# -gt 1 ]]; then
        echo "'all' cannot be combined with individual plugin names." >&2
        usage >&2
        exit 2
    fi
    SELECTED_MODULES=("${ALL_MODULES[@]}")
else
    for requested in "$@"; do
        key="$(tr '[:upper:]' '[:lower:]' <<<"$requested")"
        module="${MODULES[$key]:-}"
        if [[ -z "$module" ]]; then
            echo "Unknown plugin: $requested" >&2
            usage >&2
            exit 2
        fi
        if [[ ! " ${SELECTED_MODULES[*]} " =~ " $module " ]]; then
            SELECTED_MODULES+=("$module")
        fi
    done
fi

if [[ -z "$MINECRAFT_VERSION_CONFIGURED" && -t 0 ]]; then
    read -r -p "Minecraft server version [$DEFAULT_MINECRAFT_VERSION] " selected_version || selected_version=""
    MINECRAFT_VERSION="${selected_version:-$DEFAULT_MINECRAFT_VERSION}"
fi

if [[ ! "$MINECRAFT_VERSION" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    echo "Invalid Minecraft version: $MINECRAFT_VERSION (expected e.g. 1.21.11)." >&2
    exit 2
fi

java_version="$(java -version 2>&1 | awk -F '[\".]' '/version/ {print $2; exit}')"
if [[ "$java_version" != "21" ]]; then
    echo "JDK 21 is required (found Java ${java_version:-unknown}). Set JAVA_HOME/PATH to JDK 21." >&2
    exit 1
fi

if [[ ! -f "$SERVER_DIR/eula.txt" ]] || ! grep -qx 'eula=true' "$SERVER_DIR/eula.txt"; then
    if [[ -z "$EULA_ANSWER" ]]; then
        if [[ ! -t 0 ]]; then
            echo "Set RUNSERVER_ACCEPT_EULA=yes or pass --accept-eula when running non-interactively." >&2
            exit 2
        fi
        read -r -p "Do you accept the Minecraft EULA (https://aka.ms/MinecraftEULA)? [Y/n] " EULA_ANSWER || EULA_ANSWER=""
        EULA_ANSWER="${EULA_ANSWER:-yes}"
    fi

    case "$(tr '[:upper:]' '[:lower:]' <<<"$EULA_ANSWER")" in
        y|yes|true|1)
            ;;
        n|no|false|0)
            echo "The Minecraft EULA was not accepted; the test server was not started." >&2
            exit 1
            ;;
        *)
            echo "Invalid EULA answer: $EULA_ANSWER (use yes or no)." >&2
            exit 2
            ;;
    esac

    mkdir -p "$SERVER_DIR"
    printf 'eula=true\n' > "$SERVER_DIR/eula.txt"
fi

for required_command in curl jq mvn; do
    if ! command -v "$required_command" >/dev/null; then
        echo "Required command not found: $required_command" >&2
        exit 1
    fi
done

if [[ ${#SELECTED_MODULES[@]} -eq ${#ALL_MODULES[@]} ]]; then
    mvn package
else
    mvn -pl "$(IFS=,; echo "${SELECTED_MODULES[*]}")" -am package
fi

mkdir -p "$PLUGINS_DIR"
for jar_name in "${JAR_NAMES[@]}"; do
    rm -f "$PLUGINS_DIR/$jar_name"
done

for module in "${SELECTED_MODULES[@]}"; do
    artifact="$(find "$PROJECT_DIR/$module/target" -maxdepth 1 -type f -name '*.jar' ! -name 'original-*.jar' -print -quit)"
    if [[ -z "$artifact" ]]; then
        echo "No packaged JAR found for $module." >&2
        exit 1
    fi
    cp "$artifact" "$PLUGINS_DIR/${JAR_NAMES[$module]}"
done

paper_jar="$SERVER_DIR/paper-$MINECRAFT_VERSION.jar"
if [[ ! -f "$paper_jar" ]]; then
    echo "Downloading Paper $MINECRAFT_VERSION..."
    download_url="$(curl --fail --silent --show-error -H 'User-Agent: modrinth-plugins-runserver.sh/1.0 (https://github.com/Woolyenough/modrinth-plugins)' "https://fill.papermc.io/v3/projects/paper/versions/$MINECRAFT_VERSION/builds" | jq -r 'first(.[] | select(.channel == "STABLE") | .downloads."server:default".url) // "null"')"
    if [[ -z "$download_url" || "$download_url" == "null" ]]; then
        echo "Could not find a stable Paper build for $MINECRAFT_VERSION." >&2
        exit 1
    fi
    curl --fail --location --show-error -H 'User-Agent: modrinth-plugins-runserver.sh/1.0 (https://github.com/Woolyenough/modrinth-plugins)' --output "$paper_jar" "$download_url"
fi

echo "Starting Paper $MINECRAFT_VERSION with: ${SELECTED_MODULES[*]}"
cd "$SERVER_DIR"
exec java -Xms1G -Xmx1G -jar "$paper_jar" --nogui
