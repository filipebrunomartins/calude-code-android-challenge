package com.movieflux.data.favorites.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters

@Entity(tableName = "favorite_movies")
@TypeConverters(GenreListConverter::class)
data class FavoriteMovieEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?,
    val voteAverage: Double,
    val genres: List<String>,
)

class GenreListConverter {
    @TypeConverter
    fun fromGenres(genres: List<String>): String = genres.joinToString(separator = "|")

    @TypeConverter
    fun toGenres(value: String): List<String> = if (value.isEmpty()) emptyList() else value.split("|")
}
