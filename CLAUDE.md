# MovieFlux — Memória do Projeto

> **Regra inegociável: nunca inclua nomes de empresas, instituições ou clientes reais em código, commits, nomes de branch, documentação ou qualquer arquivo deste repositório.** É requisito explícito de confidencialidade do desafio técnico. `com.movieflux` é o nome do próprio app e é seguro de usar.

## Visão geral

MovieFlux é um desafio técnico Android nativo: um app que lista filmes populares via **TMDB API v3**, precedido de um fluxo de login + biometria, com detalhes de filme (gêneros mapeados por ID), e favoritos offline-first sincronizados entre todas as telas.

Requisitos funcionais completos estão em `README-CHALLENGE.md` (texto original do desafio — não editar). O plano de arquitetura completo com justificativas está registrado no histórico de planejamento deste projeto; este arquivo resume as decisões já fechadas.

## Stack técnica

- **Linguagem**: Kotlin
- **UI**: Jetpack Compose (Material3)
- **Arquitetura**: MVVM + Clean Architecture, modularizado por camada/feature
- **Assincronismo**: Coroutines + Flow
- **DI**: Hilt
- **Networking**: Retrofit + OkHttp
- **Persistência**: Room (favoritos + cache de gêneros)
- **Segurança**: AndroidX Biometric Library + EncryptedSharedPreferences
- **Imagens**: Coil
- **Testes**: JUnit4, MockK, Turbine, kotlinx-coroutines-test, Compose UI Test
- **minSdk**: 26 / **compileSdk**: 37 / **targetSdk**: 37 / **AGP**: 9.2.1 (built-in Kotlin — não aplicar o plugin `org.jetbrains.kotlin.android` nos módulos Android; apenas módulos Kotlin puros como `domain`/`core-common`/`core-navigation` usam `kotlin("jvm")`)

## Estrutura de módulos

```
app/                    Application, MainActivity (FragmentActivity), NavHost raiz, Hilt wiring
core/
  core-common/          Kotlin puro — UiState<T>, sealed Failure
  core-ui/              Compose — tema Teal Green, MovieCard, LoadingState, ErrorState, EmptyState
  core-network/         Retrofit/OkHttp genérico, interceptors, safeApiCall
  core-security/        EncryptedSharedPreferences — SecureStorage
  core-navigation/      Rotas @Serializable, sem lógica/UI
domain/                 Kotlin puro (sem android.*) — pacotes: auth / movies / favorites
data/                   Android lib — pacotes: auth / movies (remote+local+repo) / favorites (local+repo) / di
feature/
  feature-auth/         Login + toggle biometria + BiometricPrompt
  feature-home/         Grid + paginação manual + busca
  feature-details/      Poster + gêneros + favoritar + compartilhar
  feature-favorites/    Aba dedicada, offline-first
```

**Grafo de dependências (sem ciclos):**
```
core-common     -> (nada)
domain          -> core-common
core-ui / core-network / core-security / core-navigation -> core-common
data            -> domain, core-network, core-security, core-common
feature-*       -> domain, core-ui, core-navigation, core-common   (nunca outra feature, nunca data diretamente)
app             -> feature-*, data, core-ui, core-navigation        (só o app conhece data)
```

## Decisões de arquitetura já fechadas

- **`domain` é Kotlin puro** (`kotlin("jvm")`) — zero dependência Android, testável sem Robolectric.
- **Sem módulo `core-database`** — Room vive em `data/local` (único consumidor, 2 tabelas).
- **`MainActivity` estende `FragmentActivity`** (exigência do AndroidX Biometric).
- **Navegação**: Navigation Compose, rotas type-safe (`@Serializable`) em `core-navigation`; cada feature expõe `NavGraphBuilder.xGraph(...)` recebendo lambdas de navegação — nenhuma feature importa outra.
- **Paginação da Home é manual** (contador de página no ViewModel + `suspend fun getPopularMovies(page: Int)`), não Paging3 — mantém `domain` livre de dependência androidx.
- **Gêneros**: tabela Room `genres`, fetch único (se vazia, busca `GET /genre/movie/list`), exposta como `Flow<Map<Int,String>>`, combinada com a lista de filmes antes da UI. Ver skill `tmdb-api`.
- **Favoritos**: fonte única de verdade é `FavoriteMovieDao.observeFavoriteIds(): Flow<Set<Int>>` no Room. Home/Details/Favoritos combinam esse Flow — nunca duplicar estado de favorito em memória sem derivar do Room.
- **Erros**: `sealed class Failure` (`NoConnection`, `Timeout`, `Http(code)`, `Unknown`) no `domain`; `UiState<T>` genérico (`Loading/Success/Error/Empty`) em `core-common`.
- **API key**: `local.properties` → `TMDB_API_KEY=...` (git-ignorado) → `buildConfigField` no `core-network`. **Nunca hardcodear em `.kt`, nunca commitar.**

## Convenções de código

- **Pacotes**: base `com.movieflux`, sub-pacotes por camada dentro de cada módulo (`com.movieflux.domain.movies`, `com.movieflux.feature.home.presentation`, etc.) — ver árvore completa acima.
- **Nomenclatura Kotlin padrão**: classes/objetos em PascalCase, funções/propriedades em camelCase, constantes em UPPER_SNAKE_CASE.
- **Testes**: nome no formato Given-When-Then entre backticks, ex. `` `given credenciais válidas, when login, then retorna sucesso` ``, corpo com comentários `// given` / `// when` / `// then`. Detalhes completos na skill `android-testing`.
- **Commits**: [Conventional Commits](https://www.conventionalcommits.org/) — `feat:`, `fix:`, `test:`, `refactor:`, `chore:`, `docs:`. Mensagens em português, foco no *porquê* quando não for óbvio.
- **Branches**: `feature/<escopo>` (ex. `feature/auth-biometria`) → PR direto para `main`.

## Skills do projeto

Invoque quando o contexto pedir:
- `tmdb-api` — endpoints TMDB, paginação, mapeamento de gênero, tratamento de erro de rede/API.
- `android-testing` — convenções de teste, fakes vs mocks, Turbine.
- `biometric-auth` — AndroidX BiometricPrompt, fallback, requisitos de Activity.
- `compose-ui` — paleta Teal Green, componentes reutilizáveis de `core-ui`.

## Comandos úteis

- Build completo: `./gradlew assembleDebug` (validado, ~17min na primeira vez após bump de AGP; incremental leva segundos)
- Compilar só um módulo: `./gradlew :app:compileDebugKotlin` (troque `:app` pelo módulo desejado)
- Testes unitários: `./gradlew testDebugUnitTest` (ainda sem testes escritos — ver pendências abaixo)
- Lint/format: `./gradlew ktlintCheck` / `./gradlew ktlintFormat`
- Análise estática: `./gradlew detekt`
- Testes instrumentados: `./gradlew connectedAndroidTest`
- Instalar no dispositivo físico conectado: `adb install -r app/build/outputs/apk/debug/app-debug.apk` (ou `android run --apks <caminho> --activity com.movieflux.app.MainActivity`)

## Estado atual (2026-07-06) e próximos passos

**Feito nesta sessão:**
- Infra completa do Claude Code (CLAUDE.md, skills, hooks, subagents, CI) — ver seções acima.
- Scaffolding Gradle completo: todos os 12 módulos criados e compilando (`domain`, `core-common`, `core-navigation` como Kotlin puro; `core-ui`, `core-network`, `core-security`, `data`, `feature-*`, `app` como Android). AGP 9.2.1 com built-in Kotlin (compileSdk/targetSdk 37), Gradle wrapper 9.4.1.
- `./gradlew assembleDebug` passa de ponta a ponta (305 tasks, grafo completo do Hilt/KSP/Room montado sem erros).
- App instalado e testado manualmente no dispositivo físico ASUS_A001D (não usar o emulador Pixel 9 — o usuário pediu explicitamente para usar o físico já conectado): login mockado funciona, navegação Login→Home e bottom bar Home↔Favoritos funcionam, tema Teal Green renderiza corretamente, estado de erro (401 esperado, sem API key real) e estado vazio de Favoritos renderizam corretamente.
- **Bug real encontrado e corrigido**: `HomeViewModel` inicializava `_uiState` com `isLoading = true`, e o guard de paginação em `loadNextPage()` (`if (state.isLoading) return`) bloqueava a própria primeira chamada feita em `init {}` — a Home ficava presa no spinner para sempre. Corrigido inicializando `_uiState` com `HomeUiState()` (isLoading=false); o valor inicial exposto via `stateIn(..., HomeUiState(isLoading = true))` continua fazendo a UI mostrar loading até a primeira emissão do `combine`.
- **Gotcha de API descoberto**: `NavDestination.hasRoute<T>()` (rota type-safe da Navigation Compose 2.9.x) só resolve corretamente com `import androidx.navigation.NavDestination.Companion.hasRoute` explícito — é uma extension function declarada dentro do companion object, não um top-level function. Sem esse import exato, o Kotlin resolve silenciosamente para o outro overload antigo (`hasRoute(route: String, arguments: Bundle?)`) e dá erros de tipo confusos.

**Pendente para amanhã:**
1. **Testes unitários** (requisito explícito do desafio, ainda não escritos): `AuthViewModel`/`LoginUseCase` → `HomeViewModel` (paginação+busca+sync) → `DetailsViewModel` → `MoviesRepositoryImpl` → `FavoritesRepositoryImpl`, nessa ordem de prioridade. Usar a skill `android-testing` e/ou o subagent `test-writer`.
2. **Testar fluxo de biometria** de verdade no ASUS_A001D (ativar biometria após login, reabrir o app, checar fallback). Ainda não verificado manualmente.
3. **Testar a tela de Details** (poster grande, gêneros, favoritar, compartilhar) — só a Home e Favoritos foram verificadas visualmente até agora.
4. **Adicionar uma TMDB_API_KEY real** em `local.properties` para validar o fluxo de dados de verdade (até agora só vimos o estado de erro 401 esperado, com a chave placeholder).
5. Rodar `./gradlew ktlintCheck` e `./gradlew detekt` pela primeira vez — ainda não validamos se o código gerado passa nas regras configuradas.
6. Preencher as seções "a preencher" do `README.md` (instruções de rodar, cobertura de testes, prints do fluxo de biometria).
7. Revisar código com o subagent `code-reviewer` antes de abrir PR.
8. Confirmar que o hook de pré-commit (gate de testes) funciona corretamente agora que `./gradlew` existe de verdade (ele tinha um fallback para quando gradlew não existia — checar se ainda faz sentido).
