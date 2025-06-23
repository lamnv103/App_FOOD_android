package com.example.app2025.Admin.Orders

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app2025.Utils.StatusUtils
import com.example.app2025.ui.theme.App2025Theme
import java.text.SimpleDateFormat
import java.util.*

class OrderManagementActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App2025Theme {
                OrderManagementScreen(
                    onNavigateBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderManagementScreen(
    onNavigateBack: () -> Unit,
    viewModel: OrderManagementViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var selectedFilter by remember { mutableStateOf<String?>(null) }
    var searchText by remember { mutableStateOf("") }

    val filteredOrders = remember(uiState.orders, selectedFilter, searchText) {
        uiState.orders.filter { order ->
            (selectedFilter == null || order.status == selectedFilter) &&
                    (searchText.isEmpty() || order.id.contains(searchText, ignoreCase = true))
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadOrders()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản lý Đơn hàng") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadOrders() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Search
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                placeholder = { Text("Tìm kiếm theo mã đơn hàng") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (searchText.isNotEmpty()) {
                        IconButton(onClick = { searchText = "" }) {
                            Icon(Icons.Default.Clear, "Clear")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf(
                    null to "Tất cả",
                    "pending" to "Chờ xử lý",
                    "processing" to "Đang giao",
                    "completed" to "Đã giao",
                    "cancelled" to "Đã hủy"
                )

                filters.forEach { (status, label) ->
                    FilterChip(
                        selected = selectedFilter == status,
                        onClick = { selectedFilter = status },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            // Status counts
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatusCounter(
                    count = uiState.orders.count { it.status == "pending" },
                    label = StatusUtils.getStatusInfo("pending", isAdminView = true).label,
                    color = StatusUtils.getStatusInfo("pending", isAdminView = true).color,
                    modifier = Modifier.weight(1f)
                )

                StatusCounter(
                    count = uiState.orders.count { it.status == "processing" },
                    label = StatusUtils.getStatusInfo("processing", isAdminView = true).label,
                    color = StatusUtils.getStatusInfo("processing", isAdminView = true).color,
                    modifier = Modifier.weight(1f)
                )

                StatusCounter(
                    count = uiState.orders.count { it.status == "completed" },
                    label = StatusUtils.getStatusInfo("completed", isAdminView = true).label,
                    color = StatusUtils.getStatusInfo("completed", isAdminView = true).color,
                    modifier = Modifier.weight(1f)
                )

                StatusCounter(
                    count = uiState.orders.count { it.status == "cancelled" },
                    label = StatusUtils.getStatusInfo("cancelled", isAdminView = true).label,
                    color = StatusUtils.getStatusInfo("cancelled", isAdminView = true).color,
                    modifier = Modifier.weight(1f)
                )
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (filteredOrders.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (uiState.error != null)
                            "Lỗi: ${uiState.error}"
                        else if (searchText.isNotEmpty() || selectedFilter != null)
                            "Không tìm thấy đơn hàng phù hợp"
                        else
                            "Chưa có đơn hàng nào",
                        textAlign = TextAlign.Center,
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Tổng số: ${filteredOrders.size} đơn hàng",
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 14.sp
                        )
                    }

                    items(filteredOrders) { order ->
                        OrderCard(
                            order = order,
                            onStatusUpdate = { newStatus ->
                                viewModel.updateOrderStatus(order.id, newStatus)
                            },
                            onViewDetails = {
                                // Navigate to order details
                            }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
fun StatusCounter(
    count: Int,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toString(),
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun OrderCard(
    order: OrderModel,
    onStatusUpdate: (String) -> Unit,
    onViewDetails: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showStatusMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Đơn #${order.id.takeLast(6).uppercase()}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
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

            Spacer(modifier = Modifier.height(8.dp))

            // Order date and total
            val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val orderDate = dateFormatter.format(order.timestamp?.let { Date(it) } ?: Date())

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Ngày đặt: $orderDate",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Text(
                    text = "${order.total_price}đ",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 16.sp
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(12.dp))

                // Customer info
                if (order.user_name.isNotEmpty()) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Color.Gray
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = order.user_name,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Delivery address
                if (order.delivery_address.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Color.Gray
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = order.delivery_address,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onViewDetails() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Chi tiết")
                    }

                    Box {
                        Button(
                            onClick = { showStatusMenu = true },
                            modifier = Modifier.fillMaxWidth(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cập nhật")
                        }

                        DropdownMenu(
                            expanded = showStatusMenu,
                            onDismissRequest = { showStatusMenu = false }
                        ) {
                            StatusOption(
                                status = "pending",
                                currentStatus = order.status,
                                onSelect = {
                                    onStatusUpdate("pending")
                                    showStatusMenu = false
                                }
                            )

                            StatusOption(
                                status = "processing",
                                currentStatus = order.status,
                                onSelect = {
                                    onStatusUpdate("processing")
                                    showStatusMenu = false
                                }
                            )

                            StatusOption(
                                status = "completed",
                                currentStatus = order.status,
                                onSelect = {
                                    onStatusUpdate("completed")
                                    showStatusMenu = false
                                }
                            )

                            StatusOption(
                                status = "cancelled",
                                currentStatus = order.status,
                                onSelect = {
                                    onStatusUpdate("cancelled")
                                    showStatusMenu = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusOption(
    status: String,
    currentStatus: String,
    onSelect: () -> Unit
) {
    val statusInfo = StatusUtils.getStatusInfo(status, isAdminView = true)

    DropdownMenuItem(
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(statusInfo.color, CircleShape)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = statusInfo.label,
                    fontWeight = if (status == currentStatus) FontWeight.Bold else FontWeight.Normal
                )
            }
        },
        onClick = onSelect,
        trailingIcon = {
            if (status == currentStatus) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    )
}

data class StatusInfo(
    val label: String,
    val color: Color
)

fun getStatusInfo(status: String): StatusInfo {
    return when (status) {
        "pending" -> StatusInfo("Chờ xử lý", Color(0xFFFFA000))
        "processing" -> StatusInfo("Đang giao", Color(0xFF2196F3))
        "completed" -> StatusInfo("Đã giao", Color(0xFF4CAF50))
        "cancelled" -> StatusInfo("Đã hủy", Color.Red)
        else -> StatusInfo("Không xác định", Color.Gray)
    }
}