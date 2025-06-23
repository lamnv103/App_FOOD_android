package com.example.app2025.Admin.Statistics

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app2025.ui.theme.App2025Theme
import java.util.*

class StatisticsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App2025Theme {
                val viewModel: StatisticsViewModel = viewModel()
                StatisticsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    var selectedPeriod by remember { mutableStateOf("day") }

    // Tải dữ liệu khi thay đổi khoảng thời gian
    LaunchedEffect(selectedPeriod) {
        when (selectedPeriod) {
            "day" -> viewModel.loadDailyStatistics()
            "month" -> viewModel.loadMonthlyStatistics()
            "year" -> viewModel.loadYearlyStatistics()
        }
    }

    // Theo dõi cập nhật uiState
    LaunchedEffect(uiState) {
        Log.d("StatisticsScreen", "uiState đã cập nhật: $uiState")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thống kê Doanh thu") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            // Bộ chọn khoảng thời gian
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PeriodButton(
                    text = "Ngày",
                    selected = selectedPeriod == "day",
                    onClick = { selectedPeriod = "day" },
                    modifier = Modifier.weight(1f)
                )
                PeriodButton(
                    text = "Tháng",
                    selected = selectedPeriod == "month",
                    onClick = { selectedPeriod = "month" },
                    modifier = Modifier.weight(1f)
                )
                PeriodButton(
                    text = "Năm",
                    selected = selectedPeriod == "year",
                    onClick = { selectedPeriod = "year" },
                    modifier = Modifier.weight(1f)
                )
            }

            // Thẻ tóm tắt
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SummaryCard(
                    title = "Tổng doanh thu",
                    value = uiState.totalRevenue,
                    icon = Icons.Default.AttachMoney,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = "Tổng đơn hàng",
                    value = uiState.totalOrders.toString(),
                    icon = Icons.Default.ShoppingBag,
                    color = Color(0xFF2196F3),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SummaryCard(
                    title = "Đơn hoàn thành",
                    value = uiState.completedOrders.toString(),
                    icon = Icons.Default.CheckCircle,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = "Đơn hủy",
                    value = uiState.cancelledOrders.toString(),
                    icon = Icons.Default.Cancel,
                    color = Color.Red,
                    modifier = Modifier.weight(1f)
                )
            }

            // Biểu đồ doanh thu
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Biểu đồ doanh thu",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    if (uiState.isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (uiState.revenueData.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Không có dữ liệu",
                                color = Color.Gray
                            )
                        }
                    } else {
                        RevenueChart(
                            data = uiState.revenueData,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            uiState.revenueData.forEachIndexed { index, data ->
                                if (index % 2 == 0 || index == uiState.revenueData.size - 1) {
                                    Text(
                                        text = data.label,
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.width(40.dp)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.width(40.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Sản phẩm bán chạy
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Sản phẩm bán chạy",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    if (uiState.isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (uiState.topProducts.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Không có dữ liệu",
                                color = Color.Gray
                            )
                        }
                    } else {
                        uiState.topProducts.forEachIndexed { index, product ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${index + 1}.",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = product.name,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = "Đã bán: ${product.quantity} sản phẩm",
                                        color = Color.Gray,
                                        fontSize = 14.sp
                                    )
                                }
                                Text(
                                    text = product.revenue,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (index < uiState.topProducts.size - 1) {
                                Divider(modifier = Modifier.padding(vertical = 8.dp))
                            }
                        }
                    }
                }
            }

            // Thông tin khoảng thời gian
            Text(
                text = when (selectedPeriod) {
                    "day" -> "Dữ liệu 7 ngày gần nhất"
                    "month" -> "Dữ liệu 6 tháng gần nhất"
                    else -> "Dữ liệu năm ${Calendar.getInstance().get(Calendar.YEAR)}"
                },
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/** Nút chọn khoảng thời gian */
@Composable
fun PeriodButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary else Color.LightGray,
            contentColor = if (selected) Color.White else Color.Black
        )
    ) {
        Text(text)
    }
}

/** Thẻ tóm tắt thông tin */
@Composable
fun SummaryCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                color = Color.Gray
            )
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = color
            )
        }
    }
}

/** Biểu đồ doanh thu */
@Composable
fun RevenueChart(
    data: List<RevenueData>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary
) {
    if (data.isEmpty()) return
    val maxValue = data.maxOf { it.value }
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val padding = 16.dp.toPx()
        val chartWidth = width - 2 * padding
        val chartHeight = height - 2 * padding

        // Vẽ trục x và y
        drawLine(
            color = Color.LightGray,
            start = Offset(padding, padding),
            end = Offset(padding, height - padding),
            strokeWidth = 2f
        )
        drawLine(
            color = Color.LightGray,
            start = Offset(padding, height - padding),
            end = Offset(width - padding, height - padding),
            strokeWidth = 2f
        )

        // Vẽ lưới
        val gridLines = 5
        for (i in 1..gridLines) {
            val y = height - padding - (i * chartHeight / gridLines)
            drawLine(
                color = Color.LightGray.copy(alpha = 0.5f),
                start = Offset(padding, y),
                end = Offset(width - padding, y),
                strokeWidth = 1f
            )
        }

        // Vẽ đường biểu đồ
        val pointWidth = chartWidth / (data.size - 1)
        val points = data.mapIndexed { index, dataPoint ->
            val x = padding + index * pointWidth
            val y = height - padding - (dataPoint.value / maxValue * chartHeight)
            Offset(x, y.toFloat())
        }
        val path = Path()
        path.moveTo(points.first().x, points.first().y)
        for (i in 1 until points.size) {
            path.lineTo(points[i].x, points[i].y)
        }
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 3f)
        )

        // Vẽ các điểm trên biểu đồ
        points.forEach { point ->
            drawCircle(
                color = lineColor,
                radius = 6f,
                center = point
            )
            drawCircle(
                color = Color.White,
                radius = 3f,
                center = point
            )
        }
    }
}