#!/usr/bin/env bash
# =============================================================================
# codex-run.sh — Wrapper para execução de prompts do CineSystem com Codex CLI
#
# Uso:
#   ./scripts/codex-run.sh <caminho-do-prompt>
#   ./scripts/codex-run.sh --dry-run <caminho-do-prompt>   (mostra contexto sem executar)
#   ./scripts/codex-run.sh --list                          (lista todos os prompts disponíveis)
#   ./scripts/codex-run.sh --chain fase2                   (executa todos os prompts de uma fase)
#
# Exemplos:
#   ./scripts/codex-run.sh prompts/features/filme/01-domain.md
#   ./scripts/codex-run.sh --dry-run prompts/features/ingresso/02-application.md
#   ./scripts/codex-run.sh --chain scaffolding
# =============================================================================

set -euo pipefail

# ─── Cores para output ───────────────────────────────────────────────────────
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# ─── Diretório raiz do projeto ───────────────────────────────────────────────
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

log_info()    { echo -e "${BLUE}[INFO]${NC}  $*"; }
log_success() { echo -e "${GREEN}[OK]${NC}    $*"; }
log_warn()    { echo -e "${YELLOW}[WARN]${NC}  $*"; }
log_error()   { echo -e "${RED}[ERRO]${NC}  $*" >&2; }
log_step()    { echo -e "\n${CYAN}━━━ $* ━━━${NC}\n"; }

# ─── Verificações de dependência ─────────────────────────────────────────────
check_dependencies() {
    if ! command -v codex &>/dev/null; then
        log_error "Codex CLI não encontrado. Instale com: npm install -g @openai/codex"
        exit 1
    fi
}

# ─── Lê os arquivos de contexto do cabeçalho YAML do prompt ──────────────────
# Formato esperado no topo do arquivo .md:
#   ---
#   context:
#     - docs/architecture/clean-architecture.md
#     - docs/features/filme.md
#   ---
parse_context_files() {
    local prompt_file="$1"
    local context_files=()

    # Extrai o bloco frontmatter (entre os dois ---)
    local in_frontmatter=false
    local in_context=false

    while IFS= read -r line; do
        if [[ "$line" == "---" ]]; then
            if ! $in_frontmatter; then
                in_frontmatter=true
                continue
            else
                break  # fim do frontmatter
            fi
        fi

        if $in_frontmatter; then
            if [[ "$line" =~ ^context: ]]; then
                in_context=true
                continue
            fi
            if $in_context; then
                if [[ "$line" =~ ^[[:space:]]*-[[:space:]]+(.+)$ ]]; then
                    local file_path="${BASH_REMATCH[1]}"
                    context_files+=("$PROJECT_ROOT/$file_path")
                elif [[ ! "$line" =~ ^[[:space:]] ]]; then
                    in_context=false
                fi
            fi
        fi
    done < "$prompt_file"

    printf '%s\n' "${context_files[@]}"
}

# ─── Valida que os arquivos de contexto existem ───────────────────────────────
validate_context_files() {
    local -a files=("$@")
    local all_ok=true

    for f in "${files[@]}"; do
        if [[ ! -f "$f" ]]; then
            log_error "Arquivo de contexto não encontrado: $f"
            all_ok=false
        fi
    done

    $all_ok || exit 1
}

# ─── Monta os argumentos --context para o codex ──────────────────────────────
build_context_args() {
    local -a files=("$@")
    local args=()

    for f in "${files[@]}"; do
        args+=("--context" "$f")
    done

    printf '%s\n' "${args[@]}"
}

# ─── Executa um único prompt ──────────────────────────────────────────────────
run_prompt() {
    local prompt_file="$1"
    local dry_run="${2:-false}"

    if [[ ! -f "$PROJECT_ROOT/$prompt_file" ]]; then
        log_error "Prompt não encontrado: $prompt_file"
        exit 1
    fi

    local full_prompt_path="$PROJECT_ROOT/$prompt_file"

    log_step "Prompt: $prompt_file"

    # Lê contexto do cabeçalho YAML
    local context_files
    mapfile -t context_files < <(parse_context_files "$full_prompt_path")

    if [[ ${#context_files[@]} -eq 0 ]]; then
        log_warn "Nenhum arquivo de contexto declarado no cabeçalho do prompt."
        log_warn "Adicione um bloco 'context:' no frontmatter YAML do arquivo."
    else
        log_info "Contexto carregado (${#context_files[@]} arquivo(s)):"
        for f in "${context_files[@]}"; do
            echo "       $(basename "$f")"
        done
        validate_context_files "${context_files[@]}"
    fi

    # Adiciona AGENT.md como contexto global obrigatório
    local agent_md="$PROJECT_ROOT/AGENT.md"
    if [[ -f "$agent_md" ]]; then
        context_files=("$agent_md" "${context_files[@]}")
    fi

    if [[ "$dry_run" == "true" ]]; then
        echo ""
        log_warn "DRY-RUN — comando que seria executado:"
        echo ""
        echo "  codex \\"
        for f in "${context_files[@]}"; do
            echo "    --context $(realpath --relative-to="$PROJECT_ROOT" "$f") \\"
        done
        echo "    < $prompt_file"
        echo ""
        log_info "Conteúdo do prompt:"
        echo "────────────────────────────────────────"
        cat "$full_prompt_path"
        echo "────────────────────────────────────────"
        return 0
    fi

    # Monta array de args --context
    local codex_args=()
    for f in "${context_files[@]}"; do
        codex_args+=("--context" "$f")
    done

    # Executa o codex
    log_info "Executando Codex CLI..."
    echo ""

    if codex "${codex_args[@]}" < "$full_prompt_path"; then
        log_success "Prompt executado com sucesso: $prompt_file"
    else
        log_error "Codex CLI retornou erro para: $prompt_file"
        exit 1
    fi
}

# ─── Executa uma chain de prompts (todos os de uma fase) ─────────────────────
run_chain() {
    local fase="$1"

    # Mapeamento fase → lista de prompts (ordem de execução)
    declare -A chains
    chains["scaffolding"]="
        prompts/scaffolding/01-project-structure.md
        prompts/scaffolding/02-domain-shared.md
        prompts/scaffolding/03-security-config.md
        prompts/database/01-migrations-base.md
    "
    chains["fase2"]="
        prompts/features/filme/01-domain.md
        prompts/features/filme/02-application.md
        prompts/features/filme/03-infrastructure.md
        prompts/features/filme/04-interface.md
        prompts/validation/validate-filme.md
    "
    chains["fase3"]="
        prompts/features/sessao/01-domain.md
        prompts/features/sessao/02-application.md
        prompts/features/sessao/03-infrastructure.md
        prompts/features/sessao/04-interface.md
    "
    chains["fase4"]="
        prompts/features/ingresso/01-domain.md
        prompts/features/ingresso/02-application.md
        prompts/features/ingresso/03-infrastructure.md
        prompts/features/ingresso/04-outbox-scheduler.md
        prompts/features/ingresso/05-interface.md
        prompts/database/02-migrations-outbox.md
    "
    chains["fase5"]="
        prompts/features/auth/01-domain.md
        prompts/features/auth/02-application.md
        prompts/features/auth/03-infrastructure.md
        prompts/features/auth/04-interface.md
    "
    chains["admin"]="
        prompts/features/admin/01-admin-controller.md
    "
    chains["validacao"]="
        prompts/validation/validate-dependency-rule.md
        prompts/validation/generate-unit-tests.md
        prompts/validation/generate-integration-tests.md
    "

    if [[ -z "${chains[$fase]:-}" ]]; then
        log_error "Fase desconhecida: '$fase'"
        log_info "Fases disponíveis: ${!chains[*]}"
        exit 1
    fi

    log_step "Executando chain: $fase"

    local prompts=()
    read -ra prompts <<< "${chains[$fase]}"

    local total=${#prompts[@]}
    local atual=0

    for prompt in "${prompts[@]}"; do
        prompt=$(echo "$prompt" | tr -d '[:space:]')
        [[ -z "$prompt" ]] && continue
        atual=$((atual + 1))
        echo ""
        log_info "[$atual/$total] $prompt"
        run_prompt "$prompt"
        echo ""
        log_success "[$atual/$total] Concluído"
        # Pausa de 2s entre prompts para não sobrecarregar a API
        [[ $atual -lt $total ]] && sleep 2
    done

    log_step "Chain '$fase' concluída ($total prompts)"
}

# ─── Lista todos os prompts disponíveis ──────────────────────────────────────
list_prompts() {
    log_step "Prompts disponíveis"
    find "$PROJECT_ROOT/prompts" -name "*.md" | sort | while read -r f; do
        local rel
        rel=$(realpath --relative-to="$PROJECT_ROOT" "$f")
        # Extrai o título H1 do arquivo
        local title
        title=$(grep -m1 "^# " "$f" | sed 's/^# //' || echo "(sem título)")
        printf "  %-60s %s\n" "$rel" "$title"
    done
}

# ─── Ajuda ───────────────────────────────────────────────────────────────────
show_help() {
    cat <<EOF

${CYAN}codex-run.sh${NC} — Wrapper para Codex CLI do projeto CineSystem

${YELLOW}Uso:${NC}
  ./scripts/codex-run.sh <prompt>                    Executa um prompt
  ./scripts/codex-run.sh --dry-run <prompt>          Mostra o que seria executado
  ./scripts/codex-run.sh --chain <fase>              Executa todos os prompts de uma fase
  ./scripts/codex-run.sh --list                      Lista todos os prompts disponíveis
  ./scripts/codex-run.sh --help                      Mostra esta ajuda

${YELLOW}Exemplos:${NC}
  ./scripts/codex-run.sh prompts/features/filme/01-domain.md
  ./scripts/codex-run.sh --dry-run prompts/features/ingresso/02-application.md
  ./scripts/codex-run.sh --chain scaffolding
  ./scripts/codex-run.sh --chain fase2

${YELLOW}Chains disponíveis:${NC}
  scaffolding   Estrutura inicial, shared domain, security, migrations
  fase2         Módulo Filme completo (domínio → infra → interface → testes)
  fase3         Módulo Sessão completo
  fase4         Módulo Ingresso completo (com Outbox + Redis)
  fase5         Auth completo (JWT, login, cadastro)
  admin         Painel administrativo (use cases + controller Admin)
  validacao     Auditoria de dependências + geração de testes

EOF
}

# ─── Entry point ─────────────────────────────────────────────────────────────
main() {
    check_dependencies

    case "${1:-}" in
        --help|-h)
            show_help
            ;;
        --list|-l)
            list_prompts
            ;;
        --dry-run)
            if [[ -z "${2:-}" ]]; then
                log_error "--dry-run requer o caminho do prompt como segundo argumento"
                exit 1
            fi
            run_prompt "$2" true
            ;;
        --chain)
            if [[ -z "${2:-}" ]]; then
                log_error "--chain requer o nome da fase como segundo argumento"
                exit 1
            fi
            run_chain "$2"
            ;;
        "")
            show_help
            exit 1
            ;;
        *)
            run_prompt "$1"
            ;;
    esac
}

main "$@"
