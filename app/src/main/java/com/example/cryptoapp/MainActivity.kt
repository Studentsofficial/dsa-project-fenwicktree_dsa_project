package com.example.cryptoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.room.Room
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var repository: TransactionRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = Room.databaseBuilder(////
            applicationContext,
            AppDatabase::class.java,
            "crypto_database"
        ).build()

        repository = TransactionRepository(db.transactionDao())

        setContent {

            App(
                onTrade = { coin, type, quantity, price ->

                    lifecycleScope.launch {

                        repository.saveTransaction(
                            coin,
                            type,
                            quantity,
                            price
                        )

                    }

                }
            )

        }
    }
}