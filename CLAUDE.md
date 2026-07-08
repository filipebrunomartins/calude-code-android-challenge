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

## Estado atual (2026-07-07) e próximos passos

**Feito em 2026-07-06:**
- Infra completa do Claude Code (CLAUDE.md, skills, hooks, subagents, CI) — ver seções acima.
- Scaffolding Gradle completo: todos os 12 módulos criados e compilando (`domain`, `core-common`, `core-navigation` como Kotlin puro; `core-ui`, `core-network`, `core-security`, `data`, `feature-*`, `app` como Android). AGP 9.2.1 com built-in Kotlin (compileSdk/targetSdk 37), Gradle wrapper 9.4.1.
- `./gradlew assembleDebug` passa de ponta a ponta (305 tasks, grafo completo do Hilt/KSP/Room montado sem erros).
- App instalado e testado manualmente no dispositivo físico ASUS_A001D (não usar o emulador Pixel 9 — o usuário pediu explicitamente para usar o físico já conectado): login mockado funciona, navegação Login→Home e bottom bar Home↔Favoritos funcionam, tema Teal Green renderiza corretamente, estado de erro (401 esperado, sem API key real) e estado vazio de Favoritos renderizam corretamente.
- **Bug real encontrado e corrigido**: `HomeViewModel` inicializava `_uiState` com `isLoading = true`, e o guard de paginação em `loadNextPage()` (`if (state.isLoading) return`) bloqueava a própria primeira chamada feita em `init {}` — a Home ficava presa no spinner para sempre. Corrigido inicializando `_uiState` com `HomeUiState()` (isLoading=false); o valor inicial exposto via `stateIn(..., HomeUiState(isLoading = true))` continua fazendo a UI mostrar loading até a primeira emissão do `combine`.
- **Gotcha de API descoberto**: `NavDestination.hasRoute<T>()` (rota type-safe da Navigation Compose 2.9.x) só resolve corretamente com `import androidx.navigation.NavDestination.Companion.hasRoute` explícito — é uma extension function declarada dentro do companion object, não um top-level function. Sem esse import exato, o Kotlin resolve silenciosamente para o outro overload antigo (`hasRoute(route: String, arguments: Bundle?)`) e dá erros de tipo confusos.

**Feito em 2026-07-07:**
- **Item 1 do pendente concluído**: 35 testes unitários escritos via subagent `test-writer`, na ordem definida — `LoginUseCase` (3), `AuthViewModel` (6), `HomeViewModel` (8), `DetailsViewModel` (5), `MoviesRepositoryImpl` (8), `FavoritesRepositoryImpl` (5). Todos passando (`./gradlew :data:testDebugUnitTest :domain:test :feature:feature-auth:testDebugUnitTest :feature:feature-home:testDebugUnitTest :feature:feature-details:testDebugUnitTest`). Nenhum bug real encontrado nesta rodada.
- **`DetailsViewModel` exigiu Robolectric**: `SavedStateHandle.toRoute<Route.Details>()` chama `BundleKt.bundleOf(...)` internamente (navigation-common 2.9.8), que quebra em JVM puro (`android.os.Bundle` não mockado). Solução: `org.robolectric:robolectric:4.16.1` como `testImplementation` em `feature-details` (a entrada já existe no catálogo `libs.versions.toml`), `@RunWith(RobolectricTestRunner::class)` + `@Config(sdk = [34])` (34, não 37 — compileSdk 37 ainda não tem shadows do Robolectric) e `testOptions.unitTests.isIncludeAndroidResources = true` no `build.gradle.kts` do módulo. Vale replicar esse padrão em qualquer outro ViewModel futuro que use `SavedStateHandle.toRoute<T>()`.
- **Gotcha de teste documentado**: ViewModels que expõem `uiState` via `combine(_uiState, someFlow).stateIn(scope, WhileSubscribed(5_000), placeholder)` (padrão usado em `HomeViewModel` e `DetailsViewModel`) emitem o `placeholder` como primeiro item para um novo subscriber do Turbine quando a inscrição ocorre depois que o estado interno já mudou — descartar esse primeiro item nos testes com `awaitItem()` extra antes de asserir.
- O hook de formatação automática (ktlint) reformatou ~51 arquivos pré-existentes do repo (estilo `class X @Inject constructor(...)` quebrado em múltiplas linhas, etc.) como efeito colateral de salvar os novos arquivos de teste. Confirmado que é puramente cosmético via `git diff` em amostra — nenhuma lógica alterada. Ainda não commitado nem revisado item por item.

**Feito em 2026-07-07 (continuação — testes manuais no ASUS_A001D e correções):**
- **Item 9 resolvido**: a reformatação automática dos ~51 arquivos acabou entrando junto no commit `d31aac8` (testes) — confirmado via `git show --stat`, sem separação adicional necessária.
- **`./gradlew ktlintCheck`/`detekt` rodados pela primeira vez (item 5)**: falharam na primeira passada. Corrigidos: `Routes.kt`→`Route.kt` e `Result.kt`→`ResultOf.kt` (regra ktlint `filename` — nome do arquivo deve bater com a única classe/objeto que contém). `config/detekt/detekt.yml` ganhou `MaxLineLength: 160` (nomes de teste Given-When-Then em português + o próprio ktlint recolhe expression bodies de volta a uma linha), `FunctionNaming.ignoreAnnotated: ['Composable']`, `LongParameterList` threshold 8. Novo `.editorconfig` na raiz com `ktlint_function_naming_ignore_when_annotated_with = Composable` e `max_line_length = 160` (ktlint tem o próprio limite, independente do detekt). `SafeApiCall.safeApiCall` ganhou `@Suppress("TooGenericExceptionCaught", "SwallowedException")` (mapeamento deliberado de exceções para `Failure`). `HomeScreen` teve o `LazyVerticalGrid` extraído para um `MovieGrid` privado (detekt `LongMethod`). Todos os ajustes foram validados pelo subagent `code-reviewer` como necessários, não afrouxamento gratuito (revertendo cada um isoladamente, o detekt volta a falhar nos módulos correspondentes).
- **Bug real encontrado pelo detekt (`UnusedParameter`) e corrigido**: `MovieCard` recebia `onClick` mas nunca conectava ao `Card` — tocar no pôster de um filme (Home/Favoritos) não navegava para Details, só o ícone de favoritar respondia a toque. Corrigido com o overload `Card(onClick = onClick, ...)` do Material3. Confirmado quebrado e depois corrigido manualmente no ASUS_A001D.
- **TMDB_API_KEY real configurada (item 4)**: usuário colou primeiro o token v4 (Read Access Token, JWT, 244 chars) por engano — o interceptor do projeto usa v3 (`api_key` como query param, decisão documentada na skill `tmdb-api`), então continuou dando 401. Trocado pela API Key v3 correta (32 chars) e confirmado funcionando — Home, Details, Favoritos todos testados com dados reais no ASUS_A001D (poster grande, gêneros mapeados, favoritar/desfavoritar sincronizado entre telas via Room, compartilhar abrindo o share sheet nativo).
- **Bug real de arquitetura de biometria encontrado e corrigido, validado no ASUS_A001D (Android 9 / API 28)**: `BiometricCapabilityChecker.canAuthenticate()` só checava `BIOMETRIC_STRONG`. No aparelho físico, com digital cadastrada e sensor de fingerprint funcional (confirmado via `adb shell dumpsys fingerprint`, `count:1`), `canAuthenticate(BIOMETRIC_STRONG)` falhava mesmo assim — sensores de Android <10 não são certificados Class 3 pelo framework. `minSdk` do projeto é 26, então isso descartava biometria em qualquer aparelho nessa faixa. Corrigido: `BiometricCapabilityChecker.allowedAuthenticators(): Int?` tenta `BIOMETRIC_STRONG or DEVICE_CREDENTIAL` primeiro, cai para `BIOMETRIC_WEAK` sozinho (não pode ser combinado com `DEVICE_CREDENTIAL` — a API rejeita). Novo `BiometricGateViewModel` expõe isso para `BiometricGateScreen`, que monta o `PromptInfo` dinamicamente e só usa `setNegativeButtonText` quando `DEVICE_CREDENTIAL` está ausente. Skill `biometric-auth` atualizada para documentar o porquê. **Teste de ponta a ponta com o `BiometricPrompt` real ainda pendente** — o ASUS_A001D não tem PIN/padrão/senha configurado no SO (só a digital), e o Android exige uma tela de bloqueio segura como pré-requisito para qualquer biometria funcionar via apps de terceiros, independente da digital estar cadastrada. Usuário optou por não configurar PIN por ora.
- **Hook de pré-commit corrigido (item 8)**: o gate de testes usava `[ -x ./gradlew ] && ./gradlew testDebugUnitTest -q || echo '...'`. Bug: se os testes falhassem, o `||` disparava o `echo` e o exit code final virava sucesso — testes quebrados não bloqueavam o commit. Corrigido para `if/then/else`, que propaga o exit code real do gradle. Validado com pipe-test simulando os dois casos (gradlew ausente vs. comando falhando).
- **Revisão do subagent `code-reviewer` (item 7)**: sem bloqueantes nem problemas importantes. Validou empiricamente (rodando detekt/ktlint/testes e revertendo trechos isolados) que os quatro pontos de atenção pedidos estavam corretos: lógica de `BiometricCapabilityChecker`/`BiometricGateScreen`, `Card(onClick=...)` não quebra o `IconButton` de favoritar aninhado, configs de lint são justificadas, sem regressão de teste pela troca de assinatura. Uma sugestão aplicada: `contentDescription` dinâmica ("Favoritar"/"Remover dos favoritos") no ícone de favoritar do `MovieCard`, já que o card inteiro ficou clicável.
- **README.md preenchido (item 6)**: seções "a preencher" completadas com passos reais de setup, cobertura de testes (37 testes), decisões de arquitetura resumidas, e dois exemplos concretos de bugs reais encontrados via Claude Code nesta sessão.

**Pendente:**
1. **Testar o `BiometricPrompt` de ponta a ponta** no ASUS_A001D — precisa de um PIN/padrão configurado no aparelho primeiro (pré-requisito do Android, não do app). Código já corrigido (fallback STRONG→WEAK) e coberto por testes unitários (`BiometricGateViewModelTest`), mas o fluxo real de toque no sensor ainda não foi validado ponta a ponta.
2. Considerar testar em um segundo dispositivo/emulador Android 10+ para validar o caminho `BIOMETRIC_STRONG` (só o caminho `BIOMETRIC_WEAK` foi exercitado indiretamente até agora, e nem esse chegou a abrir o prompt real por falta de PIN).
3. Revisar se o commit desta sessão (fix do `MovieCard`, biometria, configs de lint, README) deve ser um commit só ou splitted por tema — ainda não commitado.
