package com.example.falldetection

import android.util.Log
import android.content.ContentValues.TAG
import android.content.Context
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.IOException
import java.lang.IllegalArgumentException

class StoreData {
    private val gson = Gson()

    fun saveData(context: Context, data: Any){
        when(data){
            is DataFall -> {
                val dataFallList = getData(context, DataFall::class.java) ?: mutableListOf()
                val newFallID = if (dataFallList.isEmpty()) 1 else dataFallList.last().id + 1
                data.id = newFallID
                dataFallList.add(data)
                saveJson(context, dataFallList)
            }
            is DataContact -> {
                val dataContactsList = getData(context, DataContact::class.java) ?: mutableListOf()
                dataContactsList.add(data)
                saveJson(context, dataContactsList)
            }
            else -> {
                Log.e(TAG, "Unsupported data type: ${data.javaClass}")
            }
        }
    }

    fun saveJson(context: Context, data: MutableList<out Any>, flag: String = ""){
        if (data.firstOrNull() is DataFall || flag == "falls"){
            val jsonString = Json.encodeToString(data as MutableList<DataFall>)
            saveToFile(context, jsonString, "data.json")
        }
        else if(data.firstOrNull() is DataContact || flag == "contacts"){
            val jsonString = Json.encodeToString(data as MutableList<DataContact>)
            saveToFile(context, jsonString, "contacts.json")
        }
        else{
            Log.e(TAG, "Unsupported data type")
        }
    }

    private fun saveToFile(context: Context, jsonString: String, fileName: String){
        try{
            val fileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE)
            fileOutputStream.write(jsonString.toByteArray())
            fileOutputStream.close()
            Log.d(TAG, "Data saved to JSON file: $fileName")

        }catch(e: Exception){
            Log.e(TAG, "Error saving contact to JSON file: $fileName - ${e.message}")
        }
    }

    fun <T> getData(context: Context, type: Class<T>): MutableList<T>? {
        val fileName = when (type){
            DataFall::class.java -> "data.json"
            DataContact::class.java -> "contacts.json"
            else -> throw IllegalArgumentException("Unsupported data type: $type")
        }

        val file = File(context.filesDir, fileName)

        lateinit var jsonString: String

        try{
            if(file.exists()){
                jsonString = file.bufferedReader().use { it.readText() }
            }else{
                Log.w(TAG, "JSON File $fileName not found on internal storage")
                return null
            }
        }catch(ioException: IOException){
            Log.e(TAG, "Error reading file: ${file.name} - ${ioException.message}")
            return null
        }

        when (type){
            DataFall::class.java -> {
                val listType = object : TypeToken<List<DataFall>>() {}.type
                return gson.fromJson(jsonString, listType)
            }
            DataContact::class.java -> {
                val listType = object : TypeToken<List<DataContact>>() {}.type
                return gson.fromJson(jsonString, listType)
            }
            else ->{
                return mutableListOf()
            }
        }

    }
}