package com.prod.evergreen.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.prod.evergreen.models.Company

@Dao
interface CompaniesDao {


    // Methods for the User table
    @Query("SELECT * FROM users_company ORDER BY id DESC")
    fun getAllCompanies(): LiveData<List<Company>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompanies(userList: Company)

    @Query("DELETE FROM users_company")
    suspend fun deleteAllCompanies()
}
