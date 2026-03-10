package com.example.cryptoapp

import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay

@Composable
fun App(
    onTrade: (String, String, Double, Double) -> Unit
) {

    val manager = remember { StockManager() }
    var selected by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            manager.updatePrices()
        }
    }

    MaterialTheme {

        Scaffold(
            containerColor = Color(0xFF0F172A),
            bottomBar = {
                BottomNavigationBar(selected) { selected = it }
            }
        ) { paddingValues ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {

                when (selected) {
                    0 -> HomeScreen(manager, onTrade)
                    1 -> WalletScreen(manager, onTrade)
                    2 -> ActivityScreen(manager)
                }
            }
        }
    }
}