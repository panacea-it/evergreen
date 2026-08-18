package com.prod.evergreen.helper


import android.content.Context
import android.content.SharedPreferences
class SharedPreferencesHelper(val context: Context) {

    private val PREFNAME = "evergreen-preferences"
    private val PREF_NAME_INTROPAGE = "evergreen-intropage"
    val sharedPreferences : SharedPreferences = context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)
    val sharedPreferencesIntroPage : SharedPreferences = context.getSharedPreferences(PREF_NAME_INTROPAGE, Context.MODE_PRIVATE)
    fun save(KEY_NAME : String, text: String?){
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString(KEY_NAME, text)
        editor.apply()
    }


    fun saveDouble(KEY_NAME : String, text: String?){
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString(KEY_NAME, text)
        editor.apply()
    }




    fun saveIntropage(KEY_NAME : String, text: Boolean){
        val editor: SharedPreferences.Editor = sharedPreferencesIntroPage.edit()
        editor.putBoolean(KEY_NAME, text)
        editor.apply()
    }

    fun saveInt(KEY_NAME : String, value: Int){
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putInt(KEY_NAME, value)
        editor.apply()
    }


    fun save(KEY_NAME : String, status: Boolean){
        val editor : SharedPreferences.Editor = sharedPreferences.edit()
        editor.putBoolean(KEY_NAME, status)
        editor.apply()
    }


    fun getValueString(KEY_NAME: String) : String?{
        return sharedPreferences.getString(KEY_NAME, null)
    }


    fun getValueStringNull(KEY_NAME: String) : String?{
        return sharedPreferences.getString(KEY_NAME, "0.0")
    }


  fun getIntropageBool(KEY_NAME: String) : Boolean {
        return sharedPreferencesIntroPage.getBoolean(KEY_NAME, false)
    }


    fun getValueInt(KEY_NAME: String) : Int? {
        return  sharedPreferences.getInt(KEY_NAME, 0)
    }
  fun getValueInt12(KEY_NAME: String) : Int? {
        return  sharedPreferences.getInt(KEY_NAME, 11)
    }


    fun  getValueBoolean(KEY_NAME: String, defaultValue: Boolean) : Boolean?{
        return sharedPreferences.getBoolean(KEY_NAME, defaultValue)
    }

    fun clearSharedPreferences(){
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }

    fun removeValue(KEY_NAME: String){
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.remove(KEY_NAME)
        editor.apply()
    }

}