#!/usr/bin/env bash
# =============================================================================
# agent-run.sh — Wrapper agnóstico para execução de prompts com agentes de IA CLI
#
# Suporta múltiplos agentes via sistema de drivers. Adicionar um novo agente
# é só registrar uma função agent_exec_<nome>() e adicioná-lo a AGENT_REGISTRY.
#
# Uso:
#   ./sdd-kit/scripts/agent-run.sh [--agent <agente>] <caminho-do-prompt>
#   ./sdd-kit/scripts/agent-run.sh [--agent <agente>] --dry-run <caminho-do-prompt>
#   ./sdd-kit/scripts/agent-run.sh [--agent <agente>] --chain <fase>
#   ./sdd-kit/scripts/agent-run.sh --list
#   ./sdd-kit/scripts/agent-run.sh --agents
#
# Agentes suportados (out-of-the-box):
#   codex   — OpenAI Codex CLI   (npm install -g @openai/codex)
#   gemini  — Google Gemini CLI  (npm install -g @google/gemini-cli)
#   aider   — Aider              (pip install aider-chat)
#   claude  — Claude Code CLI    (npm install -g @anthropic-ai/claude-code)
#
# Se --agent não for informado, o primeiro agente disponível no PATH é usado.
#
# Exemplos:
#   ./sdd-kit/scripts/agent-run.sh sdd-kit/prompts/features/filme/01-domain.md
#   ./sdd-kit/scripts/agent-run.sh --agent gemini --chain fase2
#   ./sdd-kit/scripts/agent-run.sh --agent codex --dry-run sdd-kit/prompts/features/ingresso/02-application.md
# =============================================================================

set -euo pipefail

# ─── Cores ───────────────────────────────────────────────────────────────────
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'
BLUE='\033[0;34m'; CYAN='\033[0;36m'; MAGENTA='\033[0;35m'; NC='\033[0m'

log_info()    { echo -e "${BLUE}[INFO]${NC}  $*"; }
log_success() { echo -e "${GREEN}[OK]${NC}    $*"; }
log_warn()    { echo -e "${YELLOW}[WARN]${NC}  $*"; }
log_error()   { echo -e "${RED}[ERRO]${NC}  $*" >&2; }
log_step()    { echo -e "\n${CYAN}━━━ $* ━━━${NC}\n"; }
log_agent()   { echo -e "${MAGENTA}[AGENT: ${SELECTED_AGENT}]${NC} $*"; }

# ─── Caminhos ────────────────────────────────────────────────────────────────
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

# ─── Estado do agente selecionado ────────────────────────────────────────────
SELECTED_AGENT=""

# =============================================================================
# SISTEMA DE DRIVERS
# Cada agente precisa de:
#   1. Uma entrada em AGENT_REGISTRY="nome:binário_no_PATH"
#   2. Uma função agent_exec_<nome>(exec_input_file)
#
# A função recebe o caminho de um arquivo temporário com o input completo
# (AGENT.md + contextos + prompt) já montado. Cabe ao driver definir como
# passar esse conteúdo para o agente.
# =============================================================================

# Registro: "nome:binário". Ordem define prioridade na auto-detecção.
AGENT_REGISTRY=(
    "codex:codex"
    "gemini:gemini"
    "claude:claude"
    "aider:aider"
)

# ─── Driver: Codex CLI ───────────────────────────────────────────────────────
agent_exec_codex() {
    local exec_input="$1"
    codex exec \
        --dangerously-bypass-approvals-and-sandbox \
        --skip-git-repo-check \
        --cd "$PROJECT_ROOT" \
        - < "$exec_input"
}

agent_dryrun_codex() {
    echo "  codex exec --dangerously-bypass-approvals-and-sandbox \\"
    echo "    --skip-git-repo-check --cd \"$PROJECT_ROOT\" -"
}

# ─── Driver: Gemini CLI ──────────────────────────────────────────────────────
agent_exec_gemini() {
    local exec_input="$1"
    # Gemini CLI recebe o input via stdin
    cd "$PROJECT_ROOT" || exit 1
    cat "$exec_input" | gemini
}

agent_dryrun_gemini() {
    echo "  cat <input> | gemini --cwd \"$PROJECT_ROOT\""
}

# ─── Driver: Claude Code CLI ─────────────────────────────────────────────────
agent_exec_claude() {
    local exec_input="$1"
    cd "$PROJECT_ROOT" || exit 1
    claude --print < "$exec_input"
}

agent_dryrun_claude() {
    echo "  claude --print < <input>  # cwd: $PROJECT_ROOT"
}

# ─── Driver: Aider ───────────────────────────────────────────────────────────
# Aider não aceita stdin como prompt direto; usa --message para input não-interativo.
agent_exec_aider() {
    local exec_input="$1"
    local message
    message="$(cat "$exec_input")"
    cd "$PROJECT_ROOT" || exit 1
    aider --no-git --yes --message "$message"
}

agent_dryrun_aider() {
    echo "  aider --no-git --yes --message \"\$(cat <input>)\""
    echo "  # cwd: $PROJECT_ROOT"
}

# =============================================================================
# FIM DOS DRIVERS
# =============================================================================

# ─── Resolve o binário de um agente registrado ───────────────────────────────
agent_binary() {
    local name="$1"
    for entry in "${AGENT_REGISTRY[@]}"; do
        if [[ "${entry%%:*}" == "$name" ]]; then
            echo "${entry##*:}"
            return 0
        fi
    done
    echo ""
}

# ─── Auto-detecta o primeiro agente disponível no PATH ───────────────────────
autodetect_agent() {
    for entry in "${AGENT_REGISTRY[@]}"; do
        local name="${entry%%:*}"
        local bin="${entry##*:}"
        if command -v "$bin" &>/dev/null; then
            echo "$name"
            return 0
        fi
    done
    echo ""
}

# ─── Valida e resolve o agente selecionado ───────────────────────────────────
resolve_agent() {
    local requested="$1"  # vazio = auto-detectar

    if [[ -n "$requested" ]]; then
        local bin
        bin="$(agent_binary "$requested")"
        if [[ -z "$bin" ]]; then
            log_error "Agente desconhecido: '$requested'"
            log_info  "Agentes disponíveis: $(list_agent_names)"
            exit 1
        fi
        if ! command -v "$bin" &>/dev/null; then
            log_error "Binário '$bin' do agente '$requested' não encontrado no PATH."
            exit 1
        fi
        SELECTED_AGENT="$requested"
    else
        SELECTED_AGENT="$(autodetect_agent)"
        if [[ -z "$SELECTED_AGENT" ]]; then
            log_error "Nenhum agente de IA encontrado no PATH."
            log_info  "Instale um dos agentes suportados e tente novamente:"
            list_agents_detail
            exit 1
        fi
        log_info "Agente detectado automaticamente: ${MAGENTA}${SELECTED_AGENT}${NC}"
    fi
}

# ─── Executa o agente selecionado ────────────────────────────────────────────
dispatch_exec() {
    local exec_input="$1"
    "agent_exec_${SELECTED_AGENT}" "$exec_input"
}

dispatch_dryrun() {
    "agent_dryrun_${SELECTED_AGENT}"
}

# ─── Lista nomes dos agentes registrados ─────────────────────────────────────
list_agent_names() {
    local names=()
    for entry in "${AGENT_REGISTRY[@]}"; do names+=("${entry%%:*}"); done
    echo "${names[*]}"
}

# ─── Lista agentes com status de disponibilidade ─────────────────────────────
list_agents_detail() {
    echo ""
    for entry in "${AGENT_REGISTRY[@]}"; do
        local name="${entry%%:*}"
        local bin="${entry##*:}"
        if command -v "$bin" &>/dev/null; then
            echo -e "  ${GREEN}✔${NC}  ${CYAN}$name${NC} ($bin) — disponível"
        else
            echo -e "  ${RED}✘${NC}  ${CYAN}$name${NC} ($bin) — não encontrado no PATH"
        fi
    done
    echo ""
}

# =============================================================================
# NÚCLEO COMPARTILHADO (agnóstico de agente)
# =============================================================================

# ─── Extrai arquivos de contexto do frontmatter YAML do prompt ───────────────
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
                    # Trim whitespace
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

# ─── Monta o bloco de input completo (AGENT.md + contextos + prompt) ─────────
build_exec_input() {
    local prompt_path="$1"; shift
    local -a files=("$@")
    local rel_path=""

    cat <<EOF
Você está executando um prompt automatizado do projeto CineSystem.
Diretório raiz do repositório: $PROJECT_ROOT

Leia o AGENT.md e todos os arquivos de contexto abaixo antes de executar a tarefa.
Use esses documentos como fonte de verdade para arquitetura, convenções e regras.

EOF

    for f in "${files[@]}"; do
        rel_path="$(realpath --relative-to="$PROJECT_ROOT" "$f")"
        printf '\n===== CONTEXTO: %s =====\n\n' "$rel_path"
        cat "$f"
        printf '\n'
    done

    rel_path="$(realpath --relative-to="$PROJECT_ROOT" "$prompt_path")"
    printf '\n===== PROMPT PRINCIPAL: %s =====\n\n' "$rel_path"
    cat "$prompt_path"
    printf '\n'
}

# ─── Executa um único prompt ──────────────────────────────────────────────────
run_prompt() {
    local prompt_file="$1"
    local dry_run="${2:-false}"
    local full_prompt_path="$PROJECT_ROOT/$prompt_file"

    [[ ! -f "$full_prompt_path" ]] && { log_error "Prompt não encontrado: $prompt_file"; exit 1; }

    log_step "Prompt: $prompt_file"
    log_agent "Agente: $SELECTED_AGENT"

    local context_files=()
    mapfile -t context_files < <(parse_context_files "$full_prompt_path")

    if [[ ${#context_files[@]} -eq 0 ]]; then
        log_warn "Nenhum arquivo de contexto declarado no frontmatter YAML do prompt."
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
        dispatch_dryrun
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

    log_info "Executando $SELECTED_AGENT..."
    echo ""

    if dispatch_exec "$exec_input"; then
        rm -f "$exec_input"
        log_success "Concluído: $prompt_file"
    else
        rm -f "$exec_input"
        log_error "Falha ao executar: $prompt_file"
        exit 1
    fi
}

# ─── Executa uma chain de prompts ────────────────────────────────────────────
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
        log_info  "Fases disponíveis: ${!chains[*]}"
        exit 1
    fi

    log_step "Chain: $fase | Agente: $SELECTED_AGENT"

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

# ─── Lista todos os prompts disponíveis ──────────────────────────────────────
list_prompts() {
    log_step "Prompts disponíveis"
    find "$PROJECT_ROOT/sdd-kit/prompts" -name "*.md" | sort | while read -r f; do
        local rel title
        rel="$(realpath --relative-to="$PROJECT_ROOT" "$f")"
        title="$(grep -m1 "^# " "$f" 2>/dev/null | sed 's/^# //' || echo "(sem título)")"
        printf "  %-58s  %s\n" "$rel" "$title"
    done
}

# ─── Ajuda ───────────────────────────────────────────────────────────────────
show_help() {
    cat <<EOF

${CYAN}agent-run.sh${NC} — Wrapper agnóstico para agentes de IA CLI · CineSystem

${YELLOW}Mecanismo:${NC}
  Concatena AGENT.md + arquivos de contexto + prompt em um único bloco
  de texto e despacha para o agente selecionado via seu driver.
  Agentes suportados: $(list_agent_names)

${YELLOW}Uso:${NC}
  ./sdd-kit/scripts/agent-run.sh [--agent <agente>] <prompt>
  ./sdd-kit/scripts/agent-run.sh [--agent <agente>] --dry-run <prompt>
  ./sdd-kit/scripts/agent-run.sh [--agent <agente>] --chain <fase>
  ./sdd-kit/scripts/agent-run.sh --list
  ./sdd-kit/scripts/agent-run.sh --agents
  ./sdd-kit/scripts/agent-run.sh --help

${YELLOW}Flags:${NC}
  --agent <nome>    Força o uso de um agente específico (padrão: auto-detecta)
  --dry-run         Exibe o input e o comando sem executar
  --chain <fase>    Executa todos os prompts de uma fase em sequência
  --list            Lista todos os prompts disponíveis
  --agents          Lista os agentes suportados e seu status no PATH

${YELLOW}Exemplos:${NC}
  ./sdd-kit/scripts/agent-run.sh sdd-kit/prompts/features/filme/01-domain.md
  ./sdd-kit/scripts/agent-run.sh --agent gemini sdd-kit/prompts/features/filme/01-domain.md
  ./sdd-kit/scripts/agent-run.sh --agent codex --chain fase2
  ./sdd-kit/scripts/agent-run.sh --dry-run sdd-kit/prompts/features/ingresso/02-application.md

${YELLOW}Chains disponíveis (execute nesta ordem):${NC}
  scaffolding   Fundação: pacotes, shared domain, configs, migrations V1–V3
  fase2         Filme: domain → application → infra → interface + validação
  fase3         Sessão: sessões, salas e assentos
  fase4         Ingresso: compra, Outbox Pattern, Redis, seed V5
  fase5         Auth: JWT, login, cadastro, blacklist Redis
  admin         Painel admin: use cases + AdminController
  validacao     Auditoria de dependências + testes unitários e E2E

${YELLOW}Adicionando um novo agente:${NC}
  1. Registre em AGENT_REGISTRY: "nome:binário"
  2. Implemente agent_exec_<nome>(exec_input_file) com a lógica de execução
  3. Implemente agent_dryrun_<nome>() exibindo o comando equivalente

EOF
}

# =============================================================================
# ENTRY POINT
# Parsing manual para suportar --agent antes ou depois dos outros flags.
# =============================================================================
main() {
    local requested_agent=""
    local args=()

    # Extrai --agent <nome> de qualquer posição nos argumentos
    while [[ $# -gt 0 ]]; do
        case "$1" in
            --agent)
                [[ -z "${2:-}" ]] && { log_error "--agent requer um nome de agente"; exit 1; }
                requested_agent="$2"
                shift 2
                ;;
            *) args+=("$1"); shift ;;
        esac
    done

    # Restaura os argumentos sem --agent
    set -- "${args[@]:-}"

    case "${1:-}" in
        --help|-h)
            show_help
            ;;
        --agents)
            log_step "Agentes suportados"
            list_agents_detail
            ;;
        --list|-l)
            list_prompts
            ;;
        --dry-run)
            resolve_agent "$requested_agent"
            [[ -z "${2:-}" ]] && { log_error "--dry-run requer o caminho do prompt"; exit 1; }
            run_prompt "$2" true
            ;;
        --chain)
            resolve_agent "$requested_agent"
            [[ -z "${2:-}" ]] && { log_error "--chain requer o nome da fase"; exit 1; }
            run_chain "$2"
            ;;
        "")
            show_help
            exit 1
            ;;
        *)
            resolve_agent "$requested_agent"
            run_prompt "$1"
            ;;
    esac
}

main "$@"
