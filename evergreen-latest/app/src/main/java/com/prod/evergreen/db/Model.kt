package com.prod.evergreen.db


import androidx.room.Entity
import androidx.room.PrimaryKey


data class ProductsResponse(
    val products: List<Movie>,
    val total: Int,
    val skip: Int,
    val limit: Int
)


@Entity(tableName = "movies")
data class Movie(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val thumbnail: String,
    val description: String
)






