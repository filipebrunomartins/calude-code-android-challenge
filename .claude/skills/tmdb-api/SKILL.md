---
name: tmdb-api
description: Como consumir a TMDB API v3 no MovieFlux — endpoints de filmes populares, busca, gêneros e detalhes; paginação; mapeamento de genre_ids para nomes; tratamento de erros de rede/API. Use ao implementar ou revisar código em core-network ou data/movies.
when_to_use: Ao criar/editar TmdbApiService, DTOs, mappers, repositórios de filmes/gêneros, ou o interceptor de API key.
---

# TMDB API v3 no MovieFlux

## Configuração base

- Base URL: `https://api.themoviedb.org/3/`
- Autenticação: query param `api_key` (v3 auth simples) injetado via `OkHttp Interceptor` em `core-network`, lendo de `BuildConfig.TMDB_API_KEY`.
- **Nunca** hardcodear a chave em `.kt`. Ela vem de `local.properties` → `buildConfigField` (ver seção "API key" no CLAUDE.md).
- Idioma sugerido: query param `language=pt-BR` (ou `en-US` — decidir uma vez e manter consistente em todos os endpoints).

## Endpoints usados

| Endpoint | Uso | Parâmetros relevantes |
|---|---|---|
| `GET /movie/popular` | Home — lista paginada de populares | `page` (1-indexed) |
| `GET /search/movie` | Busca por título | `query`, `page` |
| `GET /genre/movie/list` | Cache local de gêneros | (nenhum, retorna lista completa) |
| `GET /movie/{movie_id}` | Detalhes do filme | `movie_id` no path |

## Formato de paginação da TMDB

Toda resposta paginada (`popular`, `search`) retorna:
```json
{ "page": 1, "results": [...], "total_pages": 500, "total_results": 10000 }
```
- TMDB usa páginas 1-indexed. `total_pages` é o limite real para parar de paginar (evita chamadas além do fim).
- Estratégia de paginação manual do MovieFlux (ver CLAUDE.md): o repositório expõe `suspend fun getPopularMovies(page: Int): Result<PagedResult<Movie>>`; o ViewModel guarda `currentPage` e `totalPages`, e só dispara a próxima chamada se `currentPage < totalPages` e o usuário chegou perto do fim da lista visível.
- Busca (`search/movie`) segue o mesmo contrato de paginação — reutilize o mesmo mecanismo de acumulação do Home, resetando `currentPage` a cada nova query.

## Modelos de resposta (DTOs) — campos essenciais

`MovieDto` (de `/movie/popular`, `/search/movie`, `/movie/{id}`):
- `id: Int`, `title: String`, `poster_path: String?`, `backdrop_path: String?`, `overview: String`, `vote_average: Double`, `genre_ids: List<Int>` (só em list/search — em `/movie/{id}` vem como `genres: List<GenreDto>` já expandido, **não confundir os dois formatos**).

`GenreDto` (de `/genre/movie/list` e embutido em `/movie/{id}`): `id: Int`, `name: String`.

URL de imagem: montar como `https://image.tmdb.org/t/p/{size}/{poster_path}` — usar tamanho pequeno (ex. `w342`) para listagem/grid e grande (ex. `w780` ou `original`) na tela de detalhes.

## Mapeamento de genre_ids → nomes

Esse é um dos critérios de avaliação do desafio — fazer bem:

1. `data/movies/local`: tabela Room `genres(id, name)`.
2. No repositório de gêneros: se a tabela estiver vazia, buscar `GET /genre/movie/list` uma única vez e inserir tudo; caso contrário, servir do Room. Expor como `Flow<Map<Int, String>>`.
3. No repositório de filmes (list/search), fazer `combine` do resultado da API com esse `Flow<Map<Int, String>>` para transformar `genre_ids: List<Int>` em `genres: List<String>` **antes** de o dado chegar à camada de apresentação — a UI nunca deve ver IDs crus.
4. Para `/movie/{id}`, os gêneros já vêm expandidos (`genres: List<GenreDto>`) — não precisa do cache, só mapear direto DTO → domain. Ainda assim, é boa prática popular/atualizar o cache local com esses gêneros caso algum ainda não esteja lá.

## Tratamento de erros

- Usar um helper `safeApiCall` em `core-network` que envolve a chamada Retrofit e traduz para `sealed class Failure` do domain:
  - `IOException` (sem internet/timeout) → `Failure.NoConnection` ou `Failure.Timeout`.
  - `HttpException` (código HTTP) → `Failure.Http(code)` — TMDB retorna 401 para API key inválida e 404 para filme/gênero inexistente; trate 401 como caso especial (mensagem clara de configuração, não "erro de rede genérico").
  - Qualquer outra exceção → `Failure.Unknown`.
- A UI nunca trata exceções cruas — sempre recebe `Result<T>`/`Failure` já mapeado, e decide o `UiState` (`Loading/Success/Error/Empty`) a partir disso.
- Lista vazia (0 resultados de busca) é `Empty`, não `Error` — são estados de UI distintos.
