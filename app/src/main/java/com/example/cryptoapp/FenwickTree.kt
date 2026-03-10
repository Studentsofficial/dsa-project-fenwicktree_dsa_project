package com.example.cryptoapp

class FenwickTree(private val size: Int) {

    private val tree = DoubleArray(size + 1)

    // O(log n)
    fun update(index: Int, delta: Double) {
        var i = index
        while (i <= size) {
            tree[i] += delta
            i += (i and -i)
        }
    }

    // O(log n)
    fun query(index: Int): Double {
        var sum = 0.0
        var i = index
        while (i > 0) {
            sum += tree[i]
            i -= (i and -i)
        }
        return sum
    }

    fun rangeQuery(l: Int, r: Int): Double {
        return query(r) - query(l - 1)
    }
}