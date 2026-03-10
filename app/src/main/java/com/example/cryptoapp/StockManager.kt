package com.example.cryptoapp

import androidx.compose.runtime.mutableStateListOf
import kotlin.random.Random

/*
    StockManager handles:
    - Stock data
    - Buy/Sell logic
    - Profit calculation
    - Transaction history
    - Efficient invested tracking using Fenwick Tree
*/

data class Stock(
    val id: Int,          // 1-based index (used in Fenwick Tree)
    val name: String,
    val price: Double,
    val quantity: Int,
    val invested: Double
)

class StockManager {

    // Observable lists for Compose UI
    val stocks = mutableStateListOf<Stock>()
    val transactions = mutableStateListOf<Transaction>()
    val profitHistory = mutableStateListOf<Double>()

    /*
        Fenwick Tree stores cumulative invested capital.
        update()  -> O(log n)
        query()   -> O(log n)
    */
    private val investedFenwick: FenwickTree

    init {
        val names = listOf("BTC", "ETH", "SOL", "XRP", "ADA")

        // Initialize stocks with random prices
        names.forEachIndexed { index, name ->
            stocks.add(
                Stock(
                    id = index + 1,
                    name = name,
                    price = Random.nextDouble(1000.0, 50000.0),
                    quantity = 0,
                    invested = 0.0
                )
            )
        }

        investedFenwick = FenwickTree(stocks.size)
        profitHistory.add(0.0)
    }


    fun buy(stockId: Int, qty: Int): String? {

        if (qty <= 0) return "Quantity must be greater than 0"

        val index = stocks.indexOfFirst { it.id == stockId }
        if (index == -1) return "Stock not found"

        val current = stocks[index]
        val cost = current.price * qty

        stocks[index] = current.copy(
            quantity = current.quantity + qty,
            invested = current.invested + cost
        )

        investedFenwick.update(stockId, cost)

        transactions.add(
            Transaction(
                coin = current.name,
                type = "BUY",
                quantity = qty.toDouble(),
                price = current.price,
                timestamp = System.currentTimeMillis()
            )
        )

        recordProfit()
        return null
    }


    fun sell(stockId: Int, qty: Int): String? {

        if (qty <= 0) return "Quantity must be greater than 0"

        val index = stocks.indexOfFirst { it.id == stockId }
        if (index == -1) return "Stock not found"

        val current = stocks[index]

        if (current.quantity < qty) {
            return "Insufficient stocks. You only own ${current.quantity}"
        }

        val investedReduction =
            current.invested * (qty.toDouble() / current.quantity)

        stocks[index] = current.copy(
            quantity = current.quantity - qty,
            invested = current.invested - investedReduction
        )

        investedFenwick.update(stockId, -investedReduction)

        transactions.add(
            Transaction(
                coin = current.name,
                type = "SELL",
                quantity = qty.toDouble(),
                price = current.price,
                timestamp = System.currentTimeMillis()
            )
        )

        recordProfit()
        return null
    }


    /*
        Simulates live market price fluctuation
    */
    fun updatePrices() {
        for (i in stocks.indices) {
            val current = stocks[i]
            val delta = Random.nextDouble(-200.0, 200.0)
            val newPrice = (current.price + delta).coerceAtLeast(1.0)
            stocks[i] = current.copy(price = newPrice)
        }
        recordProfit()
    }


    /*
        Stores rolling profit history for graph
    */
    private fun recordProfit() {
        profitHistory.add(totalProfit())

        if (profitHistory.size > 50) {
            profitHistory.removeAt(0)   // FIXED
        }
    }


    fun totalInvested(): Double {
        return investedFenwick.query(stocks.size)
    }


    fun totalCurrentValue(): Double {
        return stocks.sumOf { it.price * it.quantity }
    }


    fun totalProfit(): Double {
        return totalCurrentValue() - totalInvested()
    }


    fun profitPercent(): Double {
        val invested = totalInvested()
        if (invested == 0.0) return 0.0
        return (totalProfit() / invested) * 100
    }
}