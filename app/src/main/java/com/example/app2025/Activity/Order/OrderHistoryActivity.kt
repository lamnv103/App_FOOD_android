package com.example.app2025.Activity.Order

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app2025.Activity.BaseActivity
import com.example.app2025.R
import com.example.app2025.Utils.StatusUtils
import com.example.app2025.ViewModel.OrderHistoryViewModel
import java.text.SimpleDateFormat
import java.util.*

class OrderHistoryActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: OrderHistoryViewModel = viewModel()
            val state by viewModel.state.collectAsState()

            OrderHistoryScreen(
                state = state,
                onBackClick = { finish() },
                onOrderClick = { orderId ->
                    try {
                        Log.d("OrderHistory", "Navigating to order details for ID: $orderId")
                        val intent = Intent(this, OrderDetailActivity::class.java)
                        intent.putExtra("ORDER_ID", orderId)
                        startActivity(intent)
                    } catch (e: Exception) {
                        Log.e("OrderHistory", "Error navigating to order details: ${e.message}", e)
                    }
                }
            )
        }
    }
}

@Composable
fun OrderHistoryScreen(
    state: OrderHistoryViewModel.OrderHistoryState,
    onBackClick: () -> Unit,
    onOrderClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        ConstraintLayout(modifier = Modifier.padding(top = 36.dp, bottom = 24.dp)) {
            val (backBtn, titleTxt) = createRefs()
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(titleTxt) { centerTo(parent) },
                text = "Lịch Sử Mua Hàng",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 25.sp
            )
            Image(
                painter = painterResource(R.drawable.back_grey),
                contentDescription = null,
                modifier = Modifier
                    .constrainAs(backBtn) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                    }
                    .clickable { onBackClick() }
            )
        }

        // Loading indicator
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = colorResource(R.color.orange)
                )
            }
        }

        // Error message
        state.error?.let { error ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = error,
                    color = Color.Red,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Empty state
        if (!state.isLoading && state.error == null && state.orders.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.credit_card),
                        contentDescription = null,
                        modifier = Modifier.size(120.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Bạn chưa có đơn hàng nào",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Order list
        if (state.orders.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(state.orders) { orderWithDetails ->
                    OrderItem(
                        order = orderWithDetails,
                        onOrderClick = onOrderClick
                    )
                }
            }
        }
    }
}

@Composable
fun OrderItem(
    order: OrderHistoryViewModel.OrderWithDetails,
    onOrderClick: (String) -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Order ID and Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Đơn hàng #${order.order.id.takeLast(6).uppercase()}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val orderDate = order.orderDate?.let { dateFormat.format(it) } ?: "N/A"
                Text(
                    text = orderDate,
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Status with badge
            val statusInfo = StatusUtils.getStatusInfo(order.order.status, isAdminView = false)
            val statusColor = statusInfo.color
            val statusText = statusInfo.label

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(statusColor, shape = androidx.compose.foundation.shape.CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Delivery address
            if (order.address != null) {
                Text(
                    text = "Địa chỉ giao hàng:",
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${order.address.name}, ${order.address.address}, ${order.address.city}",
                    color = Color.DarkGray,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Total price and items count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Tổng tiền:",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "${order.order.total_price}đ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = colorResource(R.color.orange)
                    )
                }

                // Items count with badge
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = colorResource(R.color.orange).copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "${order.itemCount} sản phẩm",
                        color = colorResource(R.color.orange),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // View details button
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    try {
                        Log.d("OrderItem", "Clicked on order ID: ${order.order.id}")
                        onOrderClick(order.order.id)
                    } catch (e: Exception) {
                        Log.e("OrderItem", "Error in onClick: ${e.message}", e)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.orange)
                )
            ) {
                Text("Xem chi tiết đơn hàng")
            }
        }
    }
}