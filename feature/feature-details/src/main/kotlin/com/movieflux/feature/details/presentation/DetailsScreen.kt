package com.movieflux.feature.details.presentation

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.movieflux.core.ui.components.ErrorState
import com.movieflux.core.ui.components.LoadingState
import com.movieflux.domain.movies.Movie

private const val TMDB_BACKDROP_BASE_URL = "https://image.tmdb.org/t/p/w780"

@Composable
fun DetailsScreen(
    modifier: Modifier = Modifier,
    viewModel: DetailsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    when {
        uiState.isLoading && uiState.movie == null -> LoadingState(modifier)
        uiState.errorMessage != null && uiState.movie == null ->
            ErrorState(
                failure = uiState.errorMessage!!,
                onRetry = viewModel::loadDetails,
                modifier = modifier,
            )
        uiState.movie != null ->
            MovieDetailsContent(
                movie = uiState.movie!!,
                isFavorite = uiState.isFavorite,
                onFavoriteClick = viewModel::onFavoriteClick,
                onShareClick = { shareMovie(context, uiState.movie!!) },
                modifier = modifier,
            )
    }
}

@Composable
private fun MovieDetailsContent(
    movie: Movie,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        item {
            AsyncImage(
                model = movie.backdropPath?.let { "$TMDB_BACKDROP_BASE_URL$it" } ?: movie.posterPath,
                contentDescription = movie.title,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = movie.title, style = MaterialTheme.typography.headlineSmall)

                    Row {
                        IconButton(onClick = onFavoriteClick) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = "Favoritar",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                        IconButton(onClick = onShareClick) {
                            Icon(imageVector = Icons.Filled.Share, contentDescription = "Compartilhar")
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(text = "%.1f".format(movie.voteAverage), style = MaterialTheme.typography.bodyMedium)
                }

                Row(modifier = Modifier.padding(vertical = 8.dp)) {
                    movie.genres.forEach { genre ->
                        AssistChip(
                            onClick = {},
                            label = { Text(genre) },
                            modifier = Modifier.padding(end = 4.dp),
                        )
                    }
                }

                Text(text = movie.overview, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

private fun shareMovie(
    context: Context,
    movie: Movie,
) {
    val posterUrl = movie.posterPath?.let { "https://image.tmdb.org/t/p/w500$it" }
    val shareText =
        buildString {
            append(movie.title)
            if (posterUrl != null) {
                append("\n")
                append(posterUrl)
            }
        }
    val intent =
        Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
    context.startActivity(Intent.createChooser(intent, movie.title))
}
