# MovieFlux

App Android nativo que lista filmes populares usando a TMDB API v3, com login + biometria e favoritos offline-first. Construído como desafio técnico — ver requisitos originais em [`README-CHALLENGE.md`](README-CHALLENGE.md).

> Status: arquitetura, telas e testes implementados; validado manualmente em dispositivo físico (ASUS_A001D, Android 9 / API 28) com dados reais da TMDB.

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
- Estrutura de módulos e grafo de dependências: ver [`CLAUDE.md`](CLAUDE.md) e a seção [Decisões de arquitetura e bibliotecas](#decisões-de-arquitetura-e-bibliotecas) abaixo.

## Como rodar

Requisitos: JDK 17+, Android SDK com `compileSdk`/`targetSdk` 37 instalado, e uma `TMDB_API_KEY` configurada (ver seção anterior).

```bash
./gradlew installDebug   # compila e instala no dispositivo/emulador conectado (adb devices)
```

Testado em dispositivo físico (ASUS_A001D, Android 9 / API 28). `minSdk` do projeto é 26.

## Testes

```bash
./gradlew testDebugUnitTest   # testes unitários (ViewModels, Repositories) de todos os módulos
./gradlew ktlintCheck detekt  # lint / análise estática — build falha se algum dos dois reportar problema
```

Cobertura atual: 37 testes unitários, cobrindo `LoginUseCase`, `AuthViewModel`, `HomeViewModel` (paginação/busca/sync de favoritos), `DetailsViewModel`, `BiometricGateViewModel`, `MoviesRepositoryImpl` e `FavoritesRepositoryImpl`. `feature-details` usa Robolectric (`SavedStateHandle.toRoute<T>()` toca `android.os.Bundle` real em runtime).

## Como testar o fluxo de biometria

1. Faça login pela primeira vez com o usuário mockado (`admin` / `1234`).
2. Se o dispositivo tiver biometria disponível (`BiometricManager.canAuthenticate()` retornando sucesso para `BIOMETRIC_STRONG` ou, em aparelhos mais antigos, `BIOMETRIC_WEAK`), o app pergunta "Ativar biometria?" logo após o login.
3. Feche e reabra o app — a tela de biometria (`BiometricGateScreen`) deve aparecer no lugar do login e abrir o `BiometricPrompt` do sistema.
4. Para testar o fallback: cancele o prompt biométrico ou simule falhas sucessivas — o app deve permitir cair para a senha mockada.
5. Em emulador, configure uma digital de teste antes de rodar o app:
   ```bash
   adb -e emu finger touch 1
   ```
   (Configurar biometria no emulador primeiro em Settings > Security > Fingerprint, seguindo o fluxo do próprio emulador.)

**Pré-requisito do Android, não do app**: biometria só fica disponível para apps de terceiros se o aparelho tiver uma tela de bloqueio segura (PIN/padrão/senha) configurada — mesmo com uma digital cadastrada, `BiometricManager.canAuthenticate()` recusa sem isso. Validado em dispositivo físico Android 9 (API 28): sensores mais antigos não são certificados como `BIOMETRIC_STRONG` pelo framework, por isso o app tenta `BIOMETRIC_STRONG` e cai para `BIOMETRIC_WEAK` (ver `BiometricCapabilityChecker`) — mas o teste de ponta a ponta com o prompt real ainda depende de um PIN configurado no aparelho de teste.

## Decisões de arquitetura e bibliotecas

Resumo das decisões principais — detalhamento completo com justificativas está em [`CLAUDE.md`](CLAUDE.md):

- **Modularização por camada/feature** (`core-*`, `domain`, `data`, `feature-*`) com grafo de dependências sem ciclos — `domain` é Kotlin puro (`kotlin("jvm")`), zero dependência Android, testável sem Robolectric.
- **Paginação manual** na Home (contador de página no ViewModel), não Paging3 — mantém `domain` livre de dependências androidx.
- **Favoritos offline-first**: fonte única de verdade é `FavoriteMovieDao.observeFavoriteIds(): Flow<Set<Int>>` no Room; Home/Details/Favoritos combinam esse `Flow` — nunca duplicam estado em memória.
- **Gêneros**: cache local em Room, populado uma única vez via `GET /genre/movie/list`, combinado com a lista de filmes antes de chegar à UI (a UI nunca vê IDs crus).
- **API key da TMDB**: v3 (`api_key` como query param), nunca hardcoded, injetada via `buildConfigField` a partir de `local.properties`.
- **Biometria**: `BiometricCapabilityChecker` tenta `BIOMETRIC_STRONG` (com fallback nativo `DEVICE_CREDENTIAL`) e cai para `BIOMETRIC_WEAK` se o sensor não for certificado Class 3 — decisão validada em dispositivo físico real, não só em emulador.

## Uso de IA (Claude Code) no desenvolvimento

Este projeto foi planejado e implementado com apoio do Claude Code. Documentando como, conforme requisito do desafio:

- **Planejamento de arquitetura**: antes de qualquer código, um agente de design (Plan mode) foi usado para validar a estrutura de módulos, o grafo de dependências, e decisões como paginação manual vs Paging3 e a estratégia de sincronização de favoritos via Room/Flow.
- **Memória de projeto (`CLAUDE.md`)**: registra convenções de código, stack, estrutura de módulos e o lembrete de confidencialidade (sem nomes de empresas/instituições no repositório).
- **Skills do projeto** (`.claude/skills/`): `tmdb-api` (endpoints, paginação, mapeamento de gênero, erros), `android-testing` (convenções de teste, Fakes vs MockK, Turbine), `biometric-auth` (AndroidX Biometric, fallback), `compose-ui` (paleta Teal Green, componentes reutilizáveis) — usadas para manter consistência entre partes do código implementadas em momentos diferentes.
- **Hooks** (`.claude/settings.json`): lint automático (`ktlintFormat`) após editar arquivos `.kt`; gate de testes unitários antes de qualquer commit; bloqueio automático de commits que contenham a API key do TMDB em texto puro (`scripts/check-no-api-key.sh`).
- **Subagents** (`.claude/agents/`): `code-reviewer` (revisão focada em segurança e aderência à arquitetura modular) e `test-writer` (geração de testes unitários seguindo as convenções do projeto).

Exemplos concretos de bugs reais encontrados e corrigidos com apoio do Claude Code durante testes manuais em dispositivo físico:

- `MovieCard` recebia um parâmetro `onClick` que nunca era conectado ao `Card` — tocar no pôster de qualquer filme (Home/Favoritos) não navegava para Details; só o ícone de favorito respondia a toque. Encontrado pelo detekt (`UnusedParameter`) e confirmado testando no aparelho.
- `BiometricCapabilityChecker` exigia apenas `BIOMETRIC_STRONG`; em teste real num Android 9 (API 28) com digital cadastrada e funcional, o sensor não é certificado Class 3 pelo framework, então a biometria nunca era oferecida. Corrigido com fallback para `BIOMETRIC_WEAK` quando `BIOMETRIC_STRONG` não está disponível.
