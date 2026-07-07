# MovieFlux

App Android nativo que lista filmes populares usando a TMDB API v3, com login + biometria e favoritos offline-first. Construído como desafio técnico — ver requisitos originais em [`README-CHALLENGE.md`](README-CHALLENGE.md).

> Status: infraestrutura do projeto definida (arquitetura, skills, hooks, CI). Implementação em andamento — seções abaixo serão completadas conforme o código avança.

## Sumário

- [Configurar a API_KEY do TMDB](#configurar-a-api_key-do-tmdb)
- [Stack e arquitetura](#stack-e-arquitetura)
- [Como rodar](#como-rodar)
- [Testes](#testes)
- [Como testar o fluxo de biometria](#como-testar-o-fluxo-de-biometria)
- [Decisões de arquitetura e bibliotecas](#decisões-de-arquitetura-e-bibliotecas)
- [Uso de IA (Claude Code) no desenvolvimento](#uso-de-ia-claude-code-no-desenvolvimento)

## Configurar a API_KEY do TMDB

1. Crie uma conta e uma API key v3 em https://www.themoviedb.org/settings/api.
2. Na raiz do projeto, crie (ou edite) o arquivo `local.properties` (já está no `.gitignore` — nunca é commitado) e adicione:
   ```properties
   TMDB_API_KEY=sua_chave_aqui
   ```
3. A chave é lida em build time via `buildConfigField` e injetada num interceptor OkHttp — nunca fica hardcoded em código-fonte.

## Stack e arquitetura

- Kotlin, Jetpack Compose (Material3), MVVM + Clean Architecture modularizada.
- Coroutines/Flow, Hilt, Retrofit + OkHttp, Room, AndroidX Biometric, EncryptedSharedPreferences, Coil.
- Estrutura de módulos e grafo de dependências: ver [`CLAUDE.md`](CLAUDE.md).

_(Diagrama/resumo detalhado da arquitetura a ser expandido aqui durante a implementação.)_

## Como rodar

```bash
./gradlew installDebug
```

_(A preencher: requisitos de SDK/emulador, versão mínima do Android Studio, se necessário.)_

## Testes

```bash
./gradlew testDebugUnitTest   # testes unitários (ViewModels, Repositories)
./gradlew ktlintCheck detekt  # lint / análise estática
```

_(A preencher: cobertura alcançada, como rodar testes instrumentados/Compose UI Test se aplicável.)_

## Como testar o fluxo de biometria

1. Faça login pela primeira vez com o usuário mockado (`admin` / `1234`).
2. Ao ser perguntado, ative a autenticação biométrica.
3. Feche e reabra o app — a tela de biometria deve aparecer no lugar do login.
4. Em emulador, configure uma digitais de teste antes de rodar o app:
   ```bash
   adb -e emu finger touch 1
   ```
   (Configurar biometria no emulador primeiro em Settings > Security > Fingerprint, seguindo o fluxo do próprio emulador.)
5. Para testar o fallback: cancele o prompt biométrico ou simule falhas sucessivas — o app deve permitir cair para a senha mockada.

_(A preencher com passos exatos e prints/gif conforme a tela for implementada.)_

## Decisões de arquitetura e bibliotecas

_(A preencher durante a implementação — resumo do porquê de cada escolha: módulos, paginação manual vs Paging3, fonte única de verdade dos favoritos via Room/Flow, etc. Detalhamento completo está em [`CLAUDE.md`](CLAUDE.md).)_

## Uso de IA (Claude Code) no desenvolvimento

Este projeto foi planejado e implementado com apoio do Claude Code. Documentando como, conforme requisito do desafio:

- **Planejamento de arquitetura**: antes de qualquer código, um agente de design (Plan mode) foi usado para validar a estrutura de módulos, o grafo de dependências, e decisões como paginação manual vs Paging3 e a estratégia de sincronização de favoritos via Room/Flow.
- **Memória de projeto (`CLAUDE.md`)**: registra convenções de código, stack, estrutura de módulos e o lembrete de confidencialidade (sem nomes de empresas/instituições no repositório).
- **Skills do projeto** (`.claude/skills/`): `tmdb-api` (endpoints, paginação, mapeamento de gênero, erros), `android-testing` (convenções de teste, Fakes vs MockK, Turbine), `biometric-auth` (AndroidX Biometric, fallback), `compose-ui` (paleta Teal Green, componentes reutilizáveis) — usadas para manter consistência entre partes do código implementadas em momentos diferentes.
- **Hooks** (`.claude/settings.json`): lint automático (`ktlintFormat`) após editar arquivos `.kt`; gate de testes unitários antes de qualquer commit; bloqueio automático de commits que contenham a API key do TMDB em texto puro (`scripts/check-no-api-key.sh`).
- **Subagents** (`.claude/agents/`): `code-reviewer` (revisão focada em segurança e aderência à arquitetura modular) e `test-writer` (geração de testes unitários seguindo as convenções do projeto).

_(A preencher com exemplos concretos de prompts/decisões relevantes conforme a implementação avança.)_
