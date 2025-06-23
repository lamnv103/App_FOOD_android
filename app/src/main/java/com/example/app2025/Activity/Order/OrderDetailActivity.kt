package com.example.app2025.Activity.Order

import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.app2025.Activity.BaseActivity
import com.example.app2025.Activity.Profile.formatAddress
import com.example.app2025.R
import com.example.app2025.Utils.StatusUtils
import com.example.app2025.ViewModel.OrderDetailViewModel
import java.text.SimpleDateFormat
import java.util.*

class OrderDetailActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val orderId = intent.getStringExtra("ORDER_ID")
        Log.d("OrderDetail", "Received order ID: $orderId")
        if (orderId == null) {
            Toast.makeText(this, "Không tìm thấy mã đơn hàng", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setContent {
            val viewModel: OrderDetailViewModel = viewModel()
            val state by viewModel.state.collectAsState()
            val context = LocalContext.current

            // Load order details when the activity starts
            LaunchedEffect(orderId) {
                viewModel.loadOrderDetails(orderId)
            }

            // Show toast when cancel is successful
            LaunchedEffect(state.cancelSuccess) {
                if (state.cancelSuccess) {
                    Toast.makeText(context, "Đơn hàng đã được hủy thành công", Toast.LENGTH_SHORT).show()
                    viewModel.resetCancelSuccess()
                }
            }

            OrderDetailScreen(
                state = state,
                onBackClick = { finish() },
                onCancelOrder = { viewModel.cancelOrder(orderId) }
            )
        }
    }
}

@Composable
fun OrderDetailScreen(
    state: OrderDetailViewModel.OrderDetailState,
    onBackClick: () -> Unit,
    onCancelOrder: () -> Unit
) {
    val context = LocalContext.current
    var showCancelDialog by remember { mutableStateOf(false) }

    // Cancel confirmation dialog
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Xác nhận hủy đơn") },
            text = { Text("Bạn có chắc chắn muốn hủy đơn hàng này không?") },
            confirmButton = {
                Button(
                    onClick = {
                        onCancelOrder()
                        showCancelDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    )
                ) {
                    Text("Hủy đơn")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCancelDialog = false }) {
                    Text("Không")
                }
            }
        )
    }

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
                text = "Chi Tiết Đơn Hàng",
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

        // Order details
        if (!state.isLoading && state.error == null && state.order != null) {
            val order = state.order!!

            // Cancelling overlay
            if (state.isCancelling) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.padding(16.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = colorResource(R.color.orange))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Đang hủy đơn hàng...")
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Order ID and Date
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Đơn hàng #${order.id.takeLast(6).uppercase()}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )

                                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                val orderDate = state.orderDate?.let { dateFormat.format(it) } ?: "N/A"
                                Text(
                                    text = orderDate,
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Status
                            val statusInfo = StatusUtils.getStatusInfo(order.status, isAdminView = false)
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

                                // Cancel button - only show for pending or processing orders
                                if (StatusUtils.canBeCancelled(order.status)) {
                                    Button(
                                        onClick = { showCancelDialog = true },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.Red
                                        ),
                                        modifier = Modifier.height(36.dp)
                                    ) {
                                        Text("Hủy đơn", fontSize = 12.sp)
                                    }
                                }
                            }

// Hộp thoại xác nhận hủy đơn hàng
                            if (showCancelDialog) {
                                AlertDialog(
                                    onDismissRequest = { showCancelDialog = false },
                                    title = {
                                        Text(
                                            "Xác nhận hủy đơn hàng",
                                            fontWeight = FontWeight.Bold
                                        )
                                    },
                                    text = {
                                        Column {
                                            Text("Bạn có chắc chắn muốn hủy đơn hàng này không?")
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                "Lưu ý: Sau khi hủy, trạng thái đơn hàng sẽ được cập nhật và hiển thị cho admin.",
                                                fontSize = 12.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    },
                                    confirmButton = {
                                        Button(
                                            onClick = {
                                                onCancelOrder()
                                                showCancelDialog = false
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color.Red
                                            )
                                        ) {
                                            Text("Xác nhận hủy")
                                        }
                                    },
                                    dismissButton = {
                                        OutlinedButton(onClick = { showCancelDialog = false }) {
                                            Text("Không hủy")
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // Delivery address
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Địa chỉ giao hàng",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            if (state.address != null) {
                                Text(
                                    text = state.address.name,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = formatAddress(state.address),
                                    color = Color.DarkGray,
                                    fontSize = 14.sp
                                )
                            } else {
                                Text(
                                    text = "Không có thông tin địa chỉ",
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                // Order items
                item {
                    Text(
                        text = "Sản phẩm đã mua",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(state.orderItems) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Product image
                            Image(
                                painter = if (item.product_image.isNotEmpty())
                                    rememberAsyncImagePainter(item.product_image)
                                else
                                    painterResource(R.drawable.image),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = item.product_name,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 16.sp
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Số lượng: ${item.quality}",
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "${item.price}đ",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = colorResource(R.color.orange)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Order summary
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Tổng kết đơn hàng",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Tổng tiền sản phẩm:",
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "${state.subtotal}đ",
                                    fontSize = 14.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Phí vận chuyển:",
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "${state.shippingFee}đ",
                                    fontSize = 14.sp
                                )
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
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "${order.total_price}đ",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = colorResource(R.color.orange)
                                )
                            }
                        }
                    }
                }

                // Payment info
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Phương thức thanh toán",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(R.drawable.credit_card),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = "Tiền mặt khi nhận hàng",
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                // Bottom spacing
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}