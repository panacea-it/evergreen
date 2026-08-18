package com.prod.evergreen.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.prod.evergreen.models.Company

@Dao
interface NewsDao {

    @Query("SELECT * FROM movies ORDER BY id DESC")
    fun getAllNews(): LiveData<List<Movie>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNews(movieList: List<Movie>)

    @Query("DELETE FROM movies")
    suspend fun deleteAll()


}
