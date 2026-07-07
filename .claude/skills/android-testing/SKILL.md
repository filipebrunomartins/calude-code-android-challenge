---
name: android-testing
description: Convenções de teste do MovieFlux para ViewModels e Repositories — nomenclatura given-when-then, quando usar Fakes vs MockK, como testar Flow com Turbine, e cobertura mínima esperada pelo desafio. Use ao escrever ou revisar qualquer teste unitário no projeto.
when_to_use: Ao criar testes para ViewModel, UseCase, Repository, DAO, ou ao revisar cobertura de testes em um PR.
---

# Convenções de teste no MovieFlux

## Nomenclatura Given-When-Then

Nome do teste em backtick, português, descrevendo cenário → ação → resultado:

```kotlin
@Test
fun `given credenciais válidas, when login, then retorna sucesso`() = runTest {
    // given
    val repository = FakeAuthRepository()

    // when
    val result = LoginUseCase(repository)("admin", "1234")

    // then
    assertTrue(result.isSuccess)
}
```

Mantenha os três comentários (`// given`, `// when`, `// then`) mesmo em testes curtos — facilita revisão e é o padrão fixado no CLAUDE.md do projeto.

## Fakes vs MockK — quando usar cada um

| Situação | Ferramenta | Motivo |
|---|---|---|
| Repositório expõe `Flow` que precisa emitir múltiplos valores/reagir a mudanças (ex. favoritos sincronizando) | **Fake** (`MutableStateFlow` interno) | Mockar um `Flow` com MockK é frágil e não reproduz o comportamento reativo real. Um `FakeFavoritesRepository` com `MutableStateFlow<Set<Int>>` real testa o `combine`/sincronização de verdade. |
| Verificar que uma função foi chamada com certos argumentos, sem se importar com estado (ex. `shareUseCase(movie)` foi chamado) | **MockK** (`verify { ... }`) | Não há estado reativo envolvido — é só uma verificação de interação. |
| Testar mapeamento de erro de um repositório que chama uma API Retrofit | **MockK** no `TmdbApiService`/DAO, lançando exceções controladas | Simula `IOException`/`HttpException` facilmente sem precisar de infra real. |
| Testar `ViewModel` que depende de repositório reativo (Home, Details, Favorites) | **Fake** do repositório | Evita "stub hell" de mockar cada emissão do Flow manualmente. |

Regra prática: **se o teste depende de como o estado evolui ao longo do tempo, use Fake; se depende só de uma chamada pontual e seu retorno, MockK é suficiente.**

## Testando Flow com Turbine

```kotlin
@Test
fun `given filme favoritado em outra tela, when observar home, then estado atualiza`() = runTest {
    // given
    val favoritesRepo = FakeFavoritesRepository()
    val viewModel = HomeViewModel(fakeMoviesRepository, favoritesRepo)

    viewModel.uiState.test {
        // when
        favoritesRepo.toggleFavorite(movieId = 1)

        // then
        val state = awaitItem()
        assertTrue(state.movies.first { it.id == 1 }.isFavorite)
    }
}
```

- Sempre usar `runTest` (kotlinx-coroutines-test) envolvendo o bloco `.test { }` do Turbine.
- Preferir `awaitItem()` explícito por emissão esperada a `expectMostRecentItem()` — deixa claro quantas emissões o teste espera, e falha se houver emissões extras não consumidas (Turbine reclama de itens não consumidos ao final do bloco, o que pega bugs de emissão duplicada).
- Para `StateFlow` com valor inicial, o primeiro `awaitItem()` costuma ser o estado inicial (`Loading` ou valor default) — não pular isso silenciosamente, é parte do contrato do ViewModel.

## Cobertura mínima esperada (requisito do desafio)

O desafio exige testes unitários de **ViewModels e Repositories**. Prioridade, do mais crítico ao menos crítico:

1. `AuthViewModel` / `LoginUseCase` — validação de credenciais, geração/persistência de sessão.
2. `HomeViewModel` — paginação (não busca além de `total_pages`), busca (reset de página ao trocar query), sincronização de favoritos.
3. `DetailsViewModel` — mapeamento de gênero, estado de favorito, ação de compartilhar.
4. `MoviesRepositoryImpl` — mapeamento de erro (`IOException`/`HttpException` → `Failure`), mapeamento DTO→domain incluindo `genre_ids`.
5. `FavoritesRepositoryImpl` — toggle (insert/delete) e `observeFavoriteIds`/`observeIsFavorite`.

Testes de DAO (Room in-memory, instrumented) e Compose UI Test são complementares, não substituem os testes acima — ver estratégia completa no CLAUDE.md.

## Onde ficam os testes

- `src/test/` para JUnit + MockK + Turbine (ViewModels, UseCases, Repositories com dependências mockáveis).
- `src/androidTest/` só para o que exige Android real (Room in-memory, Compose UI Test).
