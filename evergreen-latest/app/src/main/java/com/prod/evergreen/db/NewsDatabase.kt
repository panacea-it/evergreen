package com.prod.evergreen.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.prod.evergreen.models.Company

@Database(entities = [Movie::class, Company::class], version =  2)
abstract class NewsDatabase : RoomDatabase() {
    abstract fun newsDao(): NewsDao
    abstract fun companyDao():CompaniesDao

//    companion object {
//        val MIGRATION_1_2 = object : Migration(1, 2) {
//            override fun migrate(db: SupportSQLiteDatabase) {
//                db.execSQL("CREATE TABLE IF NOT EXISTS users_company (" +
//                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
//                        "name TEXT, " +
//                        "branch_name TEXT, " +
//                        "email TEXT, " +
//                        "location TEXT)")
//            }
//        }
//    }
}
