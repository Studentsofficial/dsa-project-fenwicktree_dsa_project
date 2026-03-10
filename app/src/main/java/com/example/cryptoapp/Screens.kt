package com.example.cryptoapp

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max

private val BackgroundDark = Color(0xFF0B1220)
private val CardDark = Color(0xFF162033)
private val Accent = Color(0xFF7C3AED)
private val ProfitGreen = Color(0xFF22C55E)
private val LossRed = Color(0xFFEF4444)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF94A3B8)

@Composable
fun BottomNavigationBar(selected: Int, onSelect: (Int) -> Unit) {

    NavigationBar(
        containerColor = CardDark,
        tonalElevation = 8.dp
    ) {

        listOf("Home", "Wallet", "Activity").forEachIndexed { index, label ->

            NavigationBarItem(
                selected = selected == index,
                onClick = { onSelect(index) },
                label = { Text(label) },
                icon = {},
                colors = NavigationBarItemDefaults.colors(
                    selectedTextColor = Accent,
                    unselectedTextColor = TextSecondary,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
fun WalletScreen(
    manager: StockManager,
    onTrade: (String, String, Double, Double) -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {

        LazyColumn(
            modifier = Modifier.padding(16.dp)
        ) {
            items(manager.stocks, key = { it.id }) { stock ->
                StockCard(stock, manager, onTrade)
            }
        }
    }
}

@Composable
fun StockCard(
    stock: Stock,
    manager: StockManager,
    onTrade: (String, String, Double, Double) -> Unit
) {

    val context = LocalContext.current
    var qty by remember { mutableStateOf("") }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {

        Column(Modifier.padding(20.dp)) {

            Text(
                "${stock.name} - ₹${"%.2f".format(stock.price)}",
                color = TextPrimary,
                fontSize = 16.sp
            )

            Text(
                "Owned: ${stock.quantity}",
                color = TextSecondary
            )

            Spacer(Modifier.height(16.dp))

            Row {

                OutlinedTextField(
                    value = qty,
                    onValueChange = { qty = it.filter { c -> c.isDigit() } },
                    label = { Text("Qty") },
                    textStyle = TextStyle(color = TextPrimary, fontSize = 16.sp),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Accent,
                        unfocusedBorderColor = TextSecondary,
                        focusedLabelColor = Accent,
                        unfocusedLabelColor = TextSecondary,
                        cursorColor = Accent,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(Modifier.width(10.dp))

                PremiumButton(
                    text = "Buy",
                    onClick = {
                        val q = qty.toIntOrNull()
                        if (q != null) {

                            manager.buy(stock.id, q)?.let {
                                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                            }

                            onTrade(
                                stock.name,
                                "BUY",
                                q.toDouble(),
                                stock.price
                            )
                        }
                        qty = ""
                    }
                )

                Spacer(Modifier.width(8.dp))

                PremiumButton(
                    text = "Sell",
                    onClick = {
                        val q = qty.toIntOrNull()
                        if (q != null) {

                            manager.sell(stock.id, q)?.let {
                                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                            }

                            onTrade(
                                stock.name,
                                "SELL",
                                q.toDouble(),
                                stock.price
                            )
                        }
                        qty = ""
                    }
                )
            }
        }
    }
}

@Composable
fun PremiumButton(text: String, onClick: () -> Unit) {

    Button(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Accent,
            contentColor = TextPrimary
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        Text(text)
    }
}

@Composable
fun HomeScreen(
    manager: StockManager,
    onTrade: (String, String, Double, Double) -> Unit
) {

    val invested = manager.totalInvested()
    val current = manager.totalCurrentValue()
    val profit = manager.totalProfit()
    val percent = manager.profitPercent()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(20.dp)
    ) {

        Column {

            Text(
                "Portfolio Summary",
                color = TextPrimary,
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(Modifier.height(20.dp))

            Text("Invested: ₹${"%.2f".format(invested)}", color = TextSecondary)
            Text("Current: ₹${"%.2f".format(current)}", color = TextSecondary)

            Spacer(Modifier.height(12.dp))

            Text(
                "Profit: ₹${"%.2f".format(profit)} (${ "%.2f".format(percent)}%)",
                color = if (profit >= 0) ProfitGreen else LossRed,
                fontSize = 18.sp
            )

            Spacer(Modifier.height(30.dp))

            ProfitGraph(manager.profitHistory)
        }
    }
}

@Composable
fun ProfitGraph(data: List<Double>) {

    if (data.size < 2) return

    val maxVal = data.maxOrNull() ?: 1.0
    val minVal = data.minOrNull() ?: 0.0
    val range = max(1.0, maxVal - minVal)

    val graphColor =
        if (data.last() >= 0) ProfitGreen else LossRed

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {

        val width = size.width
        val height = size.height
        val stepX = width / (data.size - 1)

        val path = Path()

        data.forEachIndexed { index, value ->

            val x = index * stepX
            val normalized = ((value - minVal) / range).toFloat()
            val y = height - (normalized * height)

            if (index == 0) path.moveTo(x, y)
            else path.lineTo(x, y)
        }

        drawPath(
            path = path,
            color = graphColor,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 5f)
        )
    }
}

@Composable
fun ActivityScreen(manager: StockManager) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(20.dp)
    ) {

        Column {

            Text(
                "Transaction History",
                color = TextPrimary,
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(Modifier.height(20.dp))

            if (manager.transactions.isEmpty()) {
                Text("No transactions yet.", color = TextSecondary)
            } else {
                LazyColumn {
                    items(manager.transactions.reversed()) {
                        Text(
                            "${it.type} ${it.coin} x${it.quantity}",
                            color = Accent
                        )
                        Spacer(Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}