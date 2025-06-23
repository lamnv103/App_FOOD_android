package com.example.app2025.Activity.Dashboard

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app2025.Activity.Cart.CartActivity
import com.example.app2025.Activity.Order.OrderHistoryActivity
import com.example.app2025.Activity.Profile.ProfileActivity
import com.example.app2025.R
import com.example.app2025.Activity.Favorite.FavoriteActivity

@Composable
@Preview
fun MyBottomBar(onItemSelected: (String) -> Unit = {}) {
    val bottomMenuItemsList = prepareBottomMenu()
    val context = LocalContext.current // ✅ Sửa lỗi ở đây
    var selectedItem by remember { mutableStateOf("Home") }

    BottomNavigation(
        backgroundColor = colorResource(R.color.grey),
        elevation = 10.dp // Tăng elevation để tạo bóng đổ đẹp hơn
    ) {
        bottomMenuItemsList.forEach { bottomMenuItem ->
            BottomNavigationItem(
                selected = (selectedItem == bottomMenuItem.label),
                onClick = {
                    selectedItem = bottomMenuItem.label
                    onItemSelected(bottomMenuItem.label) // Gọi callback để xử lý điều hướng
                    when (bottomMenuItem.label) {
                        "Cart" -> context.startActivity(Intent(context, CartActivity::class.java))
                        "Favorite" -> context.startActivity(Intent(context, FavoriteActivity::class.java))
                        "Profile" -> context.startActivity(Intent(context, ProfileActivity::class.java))
                        "Order" -> context.startActivity(Intent(context, OrderHistoryActivity::class.java))
                        else -> Toast.makeText(context, bottomMenuItem.label, Toast.LENGTH_SHORT).show()
                    }
                },
                icon = {
                    Icon(
                        painter = bottomMenuItem.icon,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .size(24.dp) // Tăng kích thước icon để dễ nhìn hơn
                    )
                },
                label = {
                    Text(
                        text = bottomMenuItem.label,
                        fontSize = 12.sp, // Kích thước chữ nhỏ gọn
                        fontWeight = if (selectedItem == bottomMenuItem.label) FontWeight.Bold else FontWeight.Normal // Làm đậm chữ khi được chọn
                    )
                },
                selectedContentColor = Color.White, // Màu khi được chọn
                unselectedContentColor = Color.Gray // Màu khi không được chọn
            )
        }
    }
}

data class BottomMenuItem(
    val label: String,
    val icon: Painter
)

@Composable
fun prepareBottomMenu(): List<BottomMenuItem> {
    return listOf(
        BottomMenuItem(label = "Home", icon = painterResource(R.drawable.btn_1)),
        BottomMenuItem(label = "Cart", icon = painterResource(R.drawable.btn_2)),
        BottomMenuItem(label = "Favorite", icon = painterResource(R.drawable.btn_3)),
        BottomMenuItem(label = "Order", icon = painterResource(R.drawable.btn_4)),
        BottomMenuItem(label = "Profile", icon = painterResource(R.drawable.btn_5))
    )
}