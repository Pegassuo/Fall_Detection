package com.example.falldetection.presentation

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.falldetection.R

class ContactsActivity: ComponentActivity() {
    val storeData = StoreData()
    private lateinit var saveContactButton: Button
    private lateinit var textName: EditText
    private lateinit var textPhone: EditText
    private lateinit var recyclerView: RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.contacts)

        saveContactButton = findViewById(R.id.saveContactButton)
        textName = findViewById(R.id.textName)
        textPhone = findViewById(R.id.textPhone)
        recyclerView = findViewById(R.id.recycler_view_contacts)


        val dataContactList = storeData.getData(this, DataContact::class.java)

        Log.d(TAG, dataContactList.toString())

        //Load history of contacts
        if (dataContactList != null){
            val adapter = ChipAdapter<DataContact>(dataContactList)
            recyclerView.adapter = adapter
            adapter.notifyDataSetChanged()
        }


        saveContactButton.setOnClickListener{
            val nameString = textName.text.toString()
            val numberString = textPhone.text.toString()
            if(nameString.trim().isNotEmpty() && numberString.trim().length == 10){
                val newContact = DataContact(
                    name = nameString,
                    number = numberString
                )
                storeData.saveData(this, newContact)
            }
        }

    }
}