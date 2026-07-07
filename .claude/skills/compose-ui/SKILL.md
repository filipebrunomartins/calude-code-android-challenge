---
name: compose-ui
description: Padrão de componentes reutilizáveis e paleta de cores do MovieFlux (Teal Green como primária) em core-ui. Use ao criar telas Compose, componentes compartilhados, ou o tema Material3 do app.
when_to_use: Ao criar/editar qualquer Composable, o ColorScheme do tema, ou componentes em core-ui.
---

# Compose UI no MovieFlux

## Paleta de cores

Cor primária: **Teal Green** (sugestão do desafio). Definir como `Color(0xFF00695C)` (Teal 800) ou tom próximo, com variações claras/escuras derivadas para `ColorScheme` Material3 (light e dark). Centralizar em `core-ui/theme/Color.kt` e `Theme.kt` — nenhuma feature deve declarar cores hardcoded fora de `core-ui`.

```kotlin
val TealGreenPrimary = Color(0xFF00695C)
val TealGreenPrimaryDark = Color(0xFF004D40)
val TealGreenContainer = Color(0xFFB2DFDB)
```

Usar `MaterialTheme.colorScheme.primary` (nunca a cor hardcoded) dentro dos Composables de feature — isso garante que uma mudança de tema não exija tocar em cada tela.

## Componentes reutilizáveis esperados em `core-ui`

| Componente | Uso |
|---|---|
| `MovieCard` | Item de grid/lista (Home, Favoritos) — poster, título, rating, ícone de favorito |
| `LoadingState` | Indicador de carregamento centralizado, reutilizado em qualquer tela com `UiState.Loading` |
| `ErrorState` | Mensagem de erro + ação de retry, parametrizada pela `Failure` recebida |
| `EmptyState` | Estado de "nenhum resultado" (busca sem resultado, favoritos vazio) — mensagem customizável |
| `MovieBackdrop`/`MoviePoster` | Wrapper de `AsyncImage` (Coil) com placeholder/erro consistentes |

Toda tela de feature deve montar seu conteúdo em cima de `UiState<T>` (`core-common`) com um `when` que despacha para `LoadingState`/`ErrorState`/`EmptyState`/conteúdo real — não reimplementar esses estados feature a feature.

## Convenção de Preview

Todo componente novo em `core-ui` deve ter pelo menos dois `@Preview`: light e dark, usando o `MovieFluxTheme` real (não hardcode de cor no preview):

```kotlin
@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun MovieCardPreview() {
    MovieFluxTheme {
        MovieCard(movie = sampleMovie, onClick = {}, onFavoriteClick = {})
    }
}
```

## Regras de dependência

- Só `core-ui` depende de Compose diretamente como biblioteca de design system; features consomem os componentes de `core-ui`, não recriam variações locais dos mesmos componentes.
- `core-ui` não conhece `domain` nem `data` — os Composables recebem dados já mapeados (ex. `MovieUiModel`, não o `MovieDto` nem a entity do Room).
