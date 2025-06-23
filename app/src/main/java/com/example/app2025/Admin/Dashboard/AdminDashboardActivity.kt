package com.example.app2025.Admin

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app2025.Admin.AddFood.ProductManagementActivity
import com.example.app2025.Admin.Orders.OrderManagementActivity
import com.example.app2025.Admin.Statistics.StatisticsActivity
import com.example.app2025.Admin.Users.UserManagementActivity
import com.example.app2025.ui.theme.App2025Theme

class AdminDashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App2025Theme {
                AdminDashboardScreen(
                    onLogout = {
                        startActivity(Intent(this, AdminActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(onLogout: () -> Unit) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Quản lý Cửa hàng",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DashboardCard(
                    icon = Icons.Default.Fastfood,
                    title = "Quản lý Sản phẩm",
                    description = "Thêm, sửa, xóa các sản phẩm",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        context.startActivity(Intent(context, ProductManagementActivity::class.java))
                    }
                )

                DashboardCard(
                    icon = Icons.Default.ShoppingBag,
                    title = "Quản lý Đơn hàng",
                    description = "Xem và cập nhật trạng thái đơn hàng",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        context.startActivity(Intent(context, OrderManagementActivity::class.java))
                    }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DashboardCard(
                    icon = Icons.Default.People,
                    title = "Quản lý Người dùng",
                    description = "Xem thông tin người dùng",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        context.startActivity(Intent(context, UserManagementActivity::class.java))
                    }
                )

                DashboardCard(
                    icon = Icons.Default.BarChart,
                    title = "Thống kê",
                    description = "Xem báo cáo và thống kê",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        context.startActivity(Intent(context, StatisticsActivity::class.java))
                    }
                )
            }
        }
    }
}

@Composable
fun DashboardCard(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(180.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
