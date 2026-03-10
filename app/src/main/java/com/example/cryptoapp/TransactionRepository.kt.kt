package com.example.cryptoapp

class TransactionRepository(
    private val dao: TransactionDao
) {

    suspend fun saveTransaction(
        coin: String,
        type: String,
        quantity: Double,
        price: Double
    ) {

        val transaction = Transaction(
            coin = coin,
            type = type,
            quantity = quantity,
            price = price,
            timestamp = System.currentTimeMillis()
        )

        dao.insert(transaction)
    }

    suspend fun getTransactions(): List<Transaction> {
        return dao.getAll()
    }
}
//UI → Repository → DAO → Database