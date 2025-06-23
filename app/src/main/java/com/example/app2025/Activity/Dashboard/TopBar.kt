package com.example.app2025.Activity.Dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.example.app2025.R

@Composable
fun TopBar(onLogout: () -> Unit) {
    ConstraintLayout(
        modifier = Modifier
            .padding(top = 48.dp)
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        val (name, settings, notification, logout) = createRefs()

        // Biểu tượng cài đặt
        Image(
            painter = painterResource(R.drawable.settings_icon),
            contentDescription = "Cài đặt",
            modifier = Modifier
                .constrainAs(settings) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                }
                .clickable { /* Xử lý sự kiện nhấp chuột ở đây */ }
                .padding(8.dp)
        )

        // Tên và mô tả
        Column(
            modifier = Modifier
                .constrainAs(name) {
                    top.linkTo(parent.top)
                    start.linkTo(settings.end)
                    end.linkTo(notification.start)
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color.Magenta, fontWeight = FontWeight.Bold)) {
                        append("VAN")
                    }
                    withStyle(style = SpanStyle(color = Color.Red, fontWeight = FontWeight.Bold)) {
                        append("LAM")
                    }
                },
                fontSize = 20.sp
            )
            Text(
                text = "Cửa hàng trực tuyến",
                color = Color.Green,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Biểu tượng thông báo
        Image(
            painter = painterResource(R.drawable.bell_icon),
            contentDescription = "Thông báo",
            modifier = Modifier
                .constrainAs(notification) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    end.linkTo(logout.start)
                }
                .clickable { /* Xử lý sự kiện nhấp chuột ở đây */ }
                .padding(8.dp)
        )

        // Nút đăng xuất
        IconButton(
            onClick = onLogout,
            modifier = Modifier
                .constrainAs(logout) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    end.linkTo(parent.end)
                }
        ) {
            Icon(
                imageVector = Icons.Default.ExitToApp,
                contentDescription = "Đăng xuất",
                tint = Color(0xFFFF5722) // Màu cam phù hợp với theme của ứng dụng
            )
        }
    }
}