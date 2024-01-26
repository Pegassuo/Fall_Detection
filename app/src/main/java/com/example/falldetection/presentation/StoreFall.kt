package com.example.falldetection.presentation

import java.io.File
import android.util.Log
import android.content.ContentValues.TAG
import android.content.Context
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class StoreFall {
    private val gson = Gson()
    private val dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    private val timeFomat: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a")

    fun saveData(context: Context){
        val data = getData(context);
        if(data != null){
            val dataFall = DataFall(
                id = data.last().id + 1,
                ubicacion = "Guayaquil, Guayas, Ecuador",
                fecha = LocalDate.now().format(dateFormat),
                hora = LocalTime.now().format(timeFomat)
            )
            Log.d(TAG, "Registro creado con exito ${Json.encodeToString(dataFall)}")
        }else{
            Log.d(TAG, "Fall could'nt be created")
        }

    }

    fun getData(context: Context): List<DataFall>? {
        lateinit var jsonString: String

        try {
            jsonString = context.assets.open("FallInfo.json")
                .bufferedReader()
                .use { it.readText() }
        } catch (ioException: IOException) {
            Log.d(TAG, ioException.toString())
        }

        val listDataFall = object : TypeToken<List<DataFall>>() {}.type
        return gson.fromJson(jsonString, listDataFall)
    }

}