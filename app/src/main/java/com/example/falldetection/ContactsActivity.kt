package com.example.falldetection

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.RecyclerView

class ContactsActivity: ComponentActivity() {
    private val storeData = StoreData()
    private lateinit var saveContactButton: Button
    private lateinit var textName: EditText
    private lateinit var textPhone: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChipAdapter<DataContact>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.contacts)

        saveContactButton = findViewById(R.id.saveContactButton)
        textName = findViewById(R.id.textName)
        textPhone = findViewById(R.id.textPhone)
        recyclerView = findViewById(R.id.recycler_view_contacts)

        adapter = ChipAdapter(mutableListOf())
        recyclerView.adapter = adapter

        val dataContactList = storeData.getData(this, DataContact::class.java)
        dataContactList?.let{
            adapter.updateData(it)
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

                val updatedDataContactList = storeData.getData(this, DataContact::class.java)
                updatedDataContactList?.let {
                    adapter.updateData(it)
                }

            }
            textName.setText("")
            textPhone.setText("")
        }

    }
}