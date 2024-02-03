package com.example.falldetection.presentation

import android.util.Log
import android.content.ContentValues.TAG
import android.content.Context
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.IOException
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class StoreFall {
    private val gson = Gson()
    private val dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    private val timeFomat: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a")

    fun saveData(context: Context){
        val retreivedData = getData(context) ?: mutableListOf()
        val newFallID = if(retreivedData.isEmpty()) 1 else retreivedData.last().id + 1
        val newFall = DataFall(
            id = newFallID,
            ubicacion = "Guayaquil, Guayas, Ecuador",
            fecha = LocalDate.now().format(dateFormat),
            hora = LocalTime.now().format(timeFomat)
        )
        retreivedData.add(newFall)
        saveJson(context, retreivedData)
    }

    fun saveJson(context: Context, data: MutableList<DataFall>){
        val jsonString = Json.encodeToString(data)
        try{
            val fileOutputStream = context.openFileOutput("data.json", Context.MODE_PRIVATE)
            fileOutputStream.write(jsonString.toByteArray())
            fileOutputStream.close()
            Log.d(TAG, "Data saved to JSON file successfully")

        }catch(e: Exception){
            Log.e(TAG, "Error saving data to JSON file: ${e.message}")
        }
    }

    fun getData(context: Context): MutableList<DataFall>? {
        val file = File(context.filesDir, "data.json")
        lateinit var jsonString: String

        try{
            if(file.exists())
                jsonString = file.bufferedReader().use { it.readText() }
            else{
                Log.w(TAG, "JSON file not found on internal storage")
                return null
            }
        }catch(ioException: IOException){
            Log.e(TAG, "Error reading file: ${ioException.message}")
            return null
        }

        val listDataFall = object : TypeToken<List<DataFall>>() {}.type
        return gson.fromJson(jsonString, listDataFall)
    }
}