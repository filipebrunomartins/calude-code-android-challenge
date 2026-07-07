package com.movieflux.data.favorites.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteMovieDao {

    @Query("SELECT id FROM favorite_movies")
    fun observeFavoriteIds(): Flow<List<Int>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_movies WHERE id = :movieId)")
    fun observeIsFavorite(movieId: Int): Flow<Boolean>

    @Query("SELECT * FROM favorite_movies")
    fun observeAll(): Flow<List<FavoriteMovieEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(movie: FavoriteMovieEntity)

    @Delete
    suspend fun delete(movie: FavoriteMovieEntity)

    @Query("SELECT * FROM favorite_movies WHERE id = :movieId")
    suspend fun findById(movieId: Int): FavoriteMovieEntity?
}
