package com.example.cryptoapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val coin: String,
    val type: String,
    val quantity: Double,
    val price: Double,
    val timestamp: Long
)//Create a table called transactions
//with these columns