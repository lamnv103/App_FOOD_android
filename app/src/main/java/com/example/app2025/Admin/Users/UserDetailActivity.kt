package com.example.app2025.Admin.Users

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.app2025.ui.theme.App2025Theme
import java.text.SimpleDateFormat
import java.util.*

class UserDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userId = intent.getStringExtra("USER_ID") ?: ""

        setContent {
            App2025Theme {
                val viewModel: UserDetailViewModel = viewModel()

                LaunchedEffect(userId) {
                    if (userId.isNotEmpty()) {
                        viewModel.loadUserDetails(userId)
                    }
                }

                UserDetailScreen(
                    viewModel = viewModel,
                    onNavigateBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailScreen(
    viewModel: UserDetailViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var showBlockDialog by remember { mutableStateOf(false) }

    // Block user confirmation dialog
    if (showBlockDialog) {
        AlertDialog(
            onDismissRequest = { showBlockDialog = false },
            title = { Text("Xác nhận") },
            text = {
                Text(
                    if (uiState.user?.isBlocked == true)
                        "Bạn có chắc chắn muốn bỏ chặn người dùng này không?"
                    else
                        "Bạn có chắc chắn muốn chặn người dùng này không?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.toggleUserBlockStatus()
                        showBlockDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (uiState.user?.isBlocked == true)
                            MaterialTheme.colorScheme.primary
                        else
                            Color.Red
                    )
                ) {
                    Text(
                        if (uiState.user?.isBlocked == true) "Bỏ chặn" else "Chặn"
                    )
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showBlockDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    // Show toast for success/error messages
    LaunchedEffect(uiState.successMessage, uiState.error) {
        uiState.successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }

        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết Người dùng") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showBlockDialog = true }
                    ) {
                        Icon(
                            imageVector = if (uiState.user?.isBlocked == true)
                                Icons.Default.LockOpen
                            else
                                Icons.Default.Lock,
                            contentDescription = if (uiState.user?.isBlocked == true)
                                "Bỏ chặn người dùng"
                            else
                                "Chặn người dùng"
                        )
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
            } else if (uiState.user == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.error ?: "Không tìm thấy thông tin người dùng",
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )
                }
            } else {
                val user = uiState.user!!

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // User avatar
                    if (user.image.isNotEmpty()) {
                        AsyncImage(
                            model = user.image,
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = user.name.take(1).uppercase(),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 48.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // User name
                    Text(
                        text = user.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )

                    // User status
                    if (user.isBlocked == true) {
                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.Red.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "Đã bị chặn",
                                color = Color.Red,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // User information
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Thông tin cá nhân",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Email
                            InfoRow(
                                icon = Icons.Default.Email,
                                label = "Email",
                                value = user.email
                            )

                            Divider(modifier = Modifier.padding(vertical = 12.dp))

                            // Phone
                            InfoRow(
                                icon = Icons.Default.Phone,
                                label = "Số điện thoại",
                                value = user.phone.ifEmpty { "Chưa cập nhật" }
                            )

                            Divider(modifier = Modifier.padding(vertical = 12.dp))

                            // Birthday
                            InfoRow(
                                icon = Icons.Default.Cake,
                                label = "Ngày sinh",
                                value = user.birthday ?: "Chưa cập nhật"
                            )

                            if (uiState.address != null) {
                                Divider(modifier = Modifier.padding(vertical = 12.dp))

                                // Address
                                InfoRow(
                                    icon = Icons.Default.LocationOn,
                                    label = "Địa chỉ",
                                    value = formatAddress(uiState.address!!)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Account information
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Thông tin tài khoản",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // User ID
                            InfoRow(
                                icon = Icons.Default.Badge,
                                label = "ID",
                                value = user.id
                            )

                            Divider(modifier = Modifier.padding(vertical = 12.dp))

                            // Created at
                            InfoRow(
                                icon = Icons.Default.CalendarToday,
                                label = "Ngày tạo",
                                value = if (user.createdAt > 0) {
                                    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                    dateFormat.format(Date(user.createdAt))
                                } else {
                                    "Không có thông tin"
                                }
                            )

                            Divider(modifier = Modifier.padding(vertical = 12.dp))

                            // Last login
                            InfoRow(
                                icon = Icons.Default.AccessTime,
                                label = "Đăng nhập gần đây",
                                value = if (user.lastLogin > 0) {
                                    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                    dateFormat.format(Date(user.lastLogin))
                                } else {
                                    "Chưa đăng nhập"
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Order statistics
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Thống kê đơn hàng",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                StatItem(
                                    value = uiState.totalOrders.toString(),
                                    label = "Tổng đơn",
                                    color = MaterialTheme.colorScheme.primary
                                )

                                StatItem(
                                    value = uiState.completedOrders.toString(),
                                    label = "Đã giao",
                                    color = Color(0xFF4CAF50)
                                )

                                StatItem(
                                    value = uiState.cancelledOrders.toString(),
                                    label = "Đã hủy",
                                    color = Color.Red
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Tổng chi tiêu: ${uiState.totalSpent}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = label,
                color = Color.Gray,
                fontSize = 14.sp
            )

            Text(
                text = value,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun StatItem(
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = color
        )

        Text(
            text = label,
            color = Color.Gray,
            fontSize = 14.sp
        )
    }
}

fun formatAddress(address: AddressModel): String {
    val parts = mutableListOf<String>()

    if (address.name.isNotEmpty()) parts.add(address.name)
    if (address.address.isNotEmpty()) parts.add(address.address)
    if (address.city.isNotEmpty()) parts.add(address.city)

    return parts.joinToString(", ")
}
