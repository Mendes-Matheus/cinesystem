#!/usr/bin/env bash
# =============================================================================
# codex-run.sh — Wrapper para execução de prompts do AmenicSystem com Codex CLI
#
# Mecanismo: concatena AGENT.md + arquivos de contexto + prompt em um único
# bloco de texto enviado via stdin para `codex exec`. O Codex recebe todo o
# contexto inline e opera diretamente no repositório.
#
# Uso:
#   ./sdd-kit/scripts/codex-run.sh <caminho-do-prompt>
#   ./sdd-kit/scripts/codex-run.sh --dry-run <caminho-do-prompt>
#   ./sdd-kit/scripts/codex-run.sh --list
#   ./sdd-kit/scripts/codex-run.sh --chain <fase>
# =============================================================================

set -euo pipefail

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

log_info()    { echo -e "${BLUE}[INFO]${NC}  $*"; }
log_success() { echo -e "${GREEN}[OK]${NC}    $*"; }
log_warn()    { echo -e "${YELLOW}[WARN]${NC}  $*"; }
log_error()   { echo -e "${RED}[ERRO]${NC}  $*" >&2; }
log_step()    { echo -e "\n${CYAN}━━━ $* ━━━${NC}\n"; }

check_dependencies() {
    if ! command -v codex &>/dev/null; then
        log_error "Codex CLI não encontrado. Instale com: npm install -g @openai/codex"
        exit 1
    fi
}

# Lê o frontmatter YAML e extrai os caminhos declarados em 'context:'
parse_context_files() {
    local prompt_file="$1"
    local context_files=()
    local in_frontmatter=false
    local in_context=false
    local file_path=""

    while IFS= read -r line; do
        if [[ "$line" == "---" ]]; then
            if ! $in_frontmatter; then in_frontmatter=true; continue
            else break; fi
        fi
        if $in_frontmatter; then
            if [[ "$line" =~ ^context: ]]; then in_context=true; continue; fi
            if $in_context; then
                if [[ "$line" =~ ^[[:space:]]*-[[:space:]]+(.+)$ ]]; then
                    file_path="${BASH_REMATCH[1]}"
                    file_path="${file_path#"${file_path%%[![:space:]]*}"}"
                    file_path="${file_path%"${file_path##*[![:space:]]}"}"
                    [[ -n "$file_path" ]] && context_files+=("$PROJECT_ROOT/$file_path")
                elif [[ ! "$line" =~ ^[[:space:]] ]]; then
                    in_context=false
                fi
            fi
        fi
    done < "$prompt_file"

    [[ ${#context_files[@]} -gt 0 ]] && printf '%s\n' "${context_files[@]}"
}

validate_context_files() {
    local all_ok=true
    for f in "$@"; do
        [[ ! -f "$f" ]] && { log_error "Contexto não encontrado: $f"; all_ok=false; }
    done
    $all_ok || exit 1
}

# Monta o input composto: AGENT.md + contextos + prompt principal
build_exec_input() {
    local prompt_path="$1"; shift
    local -a files=("$@")
    local rel_path=""

    cat <<EOF
Você está executando um prompt automatizado do projeto AmenicSystem.
Diretório raiz do repositório: $PROJECT_ROOT

Leia o AGENT.md e todos os arquivos de contexto abaixo antes de executar a tarefa.
Use esses documentos como fonte de verdade para arquitetura, convenções e regras.

EOF

    for f in "${files[@]}"; do
        rel_path=$(realpath --relative-to="$PROJECT_ROOT" "$f")
        printf '\n===== CONTEXTO: %s =====\n\n' "$rel_path"
        cat "$f"
        printf '\n'
    done

    rel_path=$(realpath --relative-to="$PROJECT_ROOT" "$prompt_path")
    printf '\n===== PROMPT PRINCIPAL: %s =====\n\n' "$rel_path"
    cat "$prompt_path"
    printf '\n'
}

run_prompt() {
    local prompt_file="$1"
    local dry_run="${2:-false}"
    local full_prompt_path="$PROJECT_ROOT/$prompt_file"

    [[ ! -f "$full_prompt_path" ]] && { log_error "Prompt não encontrado: $prompt_file"; exit 1; }

    log_step "Prompt: $prompt_file"

    local context_files=()
    mapfile -t context_files < <(parse_context_files "$full_prompt_path")

    if [[ ${#context_files[@]} -eq 0 ]]; then
        log_warn "Nenhum arquivo de contexto declarado no frontmatter."
    else
        log_info "Contexto (${#context_files[@]} arquivo(s)):"
        for f in "${context_files[@]}"; do echo "       $(basename "$f")"; done
        validate_context_files "${context_files[@]}"
    fi

    local agent_md="$PROJECT_ROOT/AGENT.md"
    [[ -f "$agent_md" ]] && context_files=("$agent_md" "${context_files[@]}")

    if [[ "$dry_run" == "true" ]]; then
        echo ""
        log_warn "DRY-RUN — comando que seria executado:"
        echo ""
        echo "  codex exec --dangerously-bypass-approvals-and-sandbox \\"
        echo "    --skip-git-repo-check --cd \"$PROJECT_ROOT\" -"
        echo ""
        log_info "Arquivos incluídos no input composto:"
        for f in "${context_files[@]}"; do
            echo "  + $(realpath --relative-to="$PROJECT_ROOT" "$f")"
        done
        echo "  + $prompt_file  ← prompt principal"
        echo ""
        log_info "Conteúdo do prompt:"
        echo "────────────────────────────────────────"
        cat "$full_prompt_path"
        echo "────────────────────────────────────────"
        return 0
    fi

    local exec_input
    exec_input="$(mktemp)"
    build_exec_input "$full_prompt_path" "${context_files[@]}" > "$exec_input"

    log_info "Executando codex exec..."
    echo ""

    if codex exec \
        --dangerously-bypass-approvals-and-sandbox \
        --skip-git-repo-check \
        --cd "$PROJECT_ROOT" \
        - < "$exec_input"; then
        rm -f "$exec_input"
        log_success "Concluído: $prompt_file"
    else
        rm -f "$exec_input"
        log_error "Falha ao executar: $prompt_file"
        exit 1
    fi
}

run_chain() {
    local fase="$1"

    declare -A chains
    chains["scaffolding"]="
        sdd-kit/prompts/scaffolding/01-project-structure.md
        sdd-kit/prompts/scaffolding/02-domain-shared.md
        sdd-kit/prompts/scaffolding/03-security-config.md
        sdd-kit/prompts/database/01-migrations-base.md
    "
    chains["fase2"]="
        sdd-kit/prompts/features/filme/01-domain.md
        sdd-kit/prompts/features/filme/02-application.md
        sdd-kit/prompts/features/filme/03-infrastructure.md
        sdd-kit/prompts/features/filme/04-interface.md
        sdd-kit/prompts/validation/validate-filme.md
    "
    chains["fase3"]="
        sdd-kit/prompts/features/sessao/01-domain.md
        sdd-kit/prompts/features/sessao/02-application.md
        sdd-kit/prompts/features/sessao/03-infrastructure.md
        sdd-kit/prompts/features/sessao/04-interface.md
    "
    chains["fase4"]="
        sdd-kit/prompts/features/ingresso/01-domain.md
        sdd-kit/prompts/features/ingresso/02-application.md
        sdd-kit/prompts/features/ingresso/03-infrastructure.md
        sdd-kit/prompts/features/ingresso/04-outbox-scheduler.md
        sdd-kit/prompts/features/ingresso/05-interface.md
        sdd-kit/prompts/database/02-migrations-outbox.md
    "
    chains["fase5"]="
        sdd-kit/prompts/features/auth/01-domain.md
        sdd-kit/prompts/features/auth/02-application.md
        sdd-kit/prompts/features/auth/03-infrastructure.md
        sdd-kit/prompts/features/auth/04-interface.md
    "
    chains["admin"]="
        sdd-kit/prompts/features/admin/01-admin-controller.md
    "
    chains["validacao"]="
        sdd-kit/prompts/validation/validate-dependency-rule.md
        sdd-kit/prompts/validation/generate-unit-tests.md
        sdd-kit/prompts/validation/generate-integration-tests.md
    "

    if [[ -z "${chains[$fase]:-}" ]]; then
        log_error "Fase desconhecida: '$fase'"
        log_info "Fases disponíveis: ${!chains[*]}"
        exit 1
    fi

    log_step "Chain: $fase"

    local prompts=()
    local p=""
    mapfile -t raw_lines < <(printf '%s\n' "${chains[$fase]}")
    for p in "${raw_lines[@]}"; do
        p="$(echo "$p" | tr -d '[:space:]')"
        [[ -n "$p" ]] && prompts+=("$p")
    done

    local total=${#prompts[@]}
    local atual=0

    for p in "${prompts[@]}"; do
        atual=$((atual + 1))
        echo ""
        log_info "[$atual/$total] $p"
        run_prompt "$p"
        echo ""
        log_success "[$atual/$total] Concluído"
        [[ $atual -lt $total ]] && sleep 2
    done

    log_step "Chain '$fase' concluída — $total prompt(s)"
}

list_prompts() {
    log_step "Prompts disponíveis"
    find "$PROJECT_ROOT/sdd-kit/prompts" -name "*.md" | sort | while read -r f; do
        local rel title
        rel=$(realpath --relative-to="$PROJECT_ROOT" "$f")
        title=$(grep -m1 "^# " "$f" 2>/dev/null | sed 's/^# //' || echo "(sem título)")
        printf "  %-58s  %s\n" "$rel" "$title"
    done
}

show_help() {
    cat <<EOF

${CYAN}codex-run.sh${NC} — Wrapper para Codex CLI · AmenicSystem

${YELLOW}Mecanismo:${NC}
  Concatena AGENT.md + contexto + prompt em um único bloco de texto
  enviado via stdin para ${CYAN}codex exec${NC}. O Codex opera diretamente
  no repositório sem precisar de flags --context separadas.

${YELLOW}Uso:${NC}
  ./sdd-kit/scripts/codex-run.sh <prompt>              Executa um prompt individual
  ./sdd-kit/scripts/codex-run.sh --dry-run <prompt>    Mostra o input sem executar
  ./sdd-kit/scripts/codex-run.sh --chain <fase>        Executa todos os prompts da fase
  ./sdd-kit/scripts/codex-run.sh --list                Lista todos os prompts
  ./sdd-kit/scripts/codex-run.sh --help                Esta ajuda

${YELLOW}Exemplos:${NC}
  ./sdd-kit/scripts/codex-run.sh sdd-kit/prompts/features/filme/01-domain.md
  ./sdd-kit/scripts/codex-run.sh --dry-run sdd-kit/prompts/features/ingresso/02-application.md
  ./sdd-kit/scripts/codex-run.sh --chain scaffolding
  ./sdd-kit/scripts/codex-run.sh --chain fase2

${YELLOW}Chains disponíveis (execute nesta ordem):${NC}
  scaffolding   Fundação: pacotes, shared domain, configs, migrations V1–V3
  fase2         Filme: domain → application → infra → interface + validação
  fase3         Sessão: sessões, salas e assentos
  fase4         Ingresso: compra, Outbox Pattern, Redis, seed V5
  fase5         Auth: JWT, login, cadastro, blacklist Redis
  admin         Painel admin: use cases + AdminController
  validacao     Auditoria de dependências + testes unitários e E2E

EOF
}

main() {
    check_dependencies
    case "${1:-}" in
        --help|-h)   show_help ;;
        --list|-l)   list_prompts ;;
        --dry-run)
            [[ -z "${2:-}" ]] && { log_error "--dry-run requer o caminho do prompt"; exit 1; }
            run_prompt "$2" true ;;
        --chain)
            [[ -z "${2:-}" ]] && { log_error "--chain requer o nome da fase"; exit 1; }
            run_chain "$2" ;;
        "") show_help; exit 1 ;;
        *)  run_prompt "$1" ;;
    esac
}

main "$@"
