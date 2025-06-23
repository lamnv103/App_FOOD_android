package com.example.app2025.Admin.Orders

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.app2025.Utils.StatusUtils
import com.example.app2025.ui.theme.App2025Theme
import java.text.SimpleDateFormat
import java.util.*

class OrderDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val orderId = intent.getStringExtra("ORDER_ID") ?: ""

        setContent {
            App2025Theme {
                val viewModel: OrderDetailViewModel = viewModel()

                LaunchedEffect(orderId) {
                    viewModel.loadOrderDetails(orderId)
                }

                OrderDetailScreen(
                    viewModel = viewModel,
                    onNavigateBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    viewModel: OrderDetailViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết Đơn hàng") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.order == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.error ?: "Không tìm thấy đơn hàng",
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )
                }
            } else {
                val order = uiState.order!!

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Order ID and status
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Đơn #${order.id.takeLast(6).uppercase()}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )

                                    val statusInfo = getStatusInfo(order.status)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .background(statusInfo.color, CircleShape)
                                        )

                                        Spacer(modifier = Modifier.width(4.dp))

                                        Text(
                                            text = statusInfo.label,
                                            color = statusInfo.color,
                                            fontSize = 14.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                val orderDate = order.timestamp?.let { Date(it) } ?: Date()

                                Text(
                                    text = "Ngày đặt: ${dateFormatter.format(orderDate)}",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Status update
                                Text(
                                    text = "Cập nhật trạng thái:",
                                    fontWeight = FontWeight.Medium
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    StatusButton(
                                        status = "pending",
                                        currentStatus = order.status,
                                        onSelect = { viewModel.updateOrderStatus(order.id, "pending") }
                                    )

                                    StatusButton(
                                        status = "processing",
                                        currentStatus = order.status,
                                        onSelect = { viewModel.updateOrderStatus(order.id, "processing") }
                                    )

                                    StatusButton(
                                        status = "completed",
                                        currentStatus = order.status,
                                        onSelect = { viewModel.updateOrderStatus(order.id, "completed") }
                                    )

                                    StatusButton(
                                        status = "cancelled",
                                        currentStatus = order.status,
                                        onSelect = { viewModel.updateOrderStatus(order.id, "cancelled") }
                                    )
                                }
                            }
                        }
                    }

                    // Customer info
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Thông tin Khách hàng",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                if (order.user_name.isNotEmpty()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Text(
                                            text = order.user_name,
                                            fontSize = 16.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                if (order.delivery_address.isNotEmpty()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Text(
                                            text = order.delivery_address,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Order items
                    item {
                        Text(
                            text = "Sản phẩm đã đặt",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    if (uiState.orderItems.isEmpty()) {
                        item {
                            Text(
                                text = "Không có thông tin sản phẩm",
                                color = Color.Gray,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    } else {
                        items(uiState.orderItems) { item ->
                            OrderItemCard(item = item)
                        }
                    }

                    // Order summary
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Tổng kết",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Tổng tiền sản phẩm:")
                                    Text(uiState.subtotal)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Phí vận chuyển:")
                                    Text(uiState.shippingFee)
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Divider()
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Tổng thanh toán:",
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = order.total_price,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    // Bottom padding
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}
@Composable
fun StatusButton(
    status: String,
    currentStatus: String,
    onSelect: () -> Unit
) {
    val statusInfo = StatusUtils.getStatusInfo(status, isAdminView = true)
    val isSelected = status == currentStatus

    OutlinedButton(
        onClick = onSelect,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isSelected) statusInfo.color.copy(alpha = 0.1f) else Color.Transparent,
            contentColor = statusInfo.color
        ),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (isSelected) statusInfo.color else Color.Gray.copy(alpha = 0.5f)
            )
        ),
        modifier = Modifier.height(36.dp)
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
        }

        Text(statusInfo.label, fontSize = 12.sp)
    }
}
@Composable
fun OrderItemCard(item: OrderItemModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = item.image_url,
                contentDescription = item.name,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = item.name,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )

                Text(
                    text = "Số lượng: ${item.quantity}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Text(
                text = "${item.price}",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}