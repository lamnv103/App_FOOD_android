package com.example.app2025.Activity.Cart

import android.icu.text.DecimalFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.app2025.R

@Composable
fun CartSummary(itemTotal: Double, tax: Double, delivery: Double) {
    // Định dạng số thập phân 2 chữ số sau dấu phẩy
    val decimalFormat = DecimalFormat("#.00")

    // Tính tổng tiền cuối cùng
    val total = itemTotal + tax + delivery

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .background(
                color = colorResource(R.color.grey),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(12.dp)
    ) {
        // Hiển thị tổng tiền hàng
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text(
                text = "Tổng cộng:",
                modifier = Modifier.weight(1f),
                color = colorResource(R.color.darkPurple)
            )
            Text(text = "$${decimalFormat.format(itemTotal)}")
        }

        // Hiển thị phí vận chuyển
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text(
                text = "Vận chuyển:",
                modifier = Modifier.weight(1f),
                color = colorResource(R.color.darkPurple)
            )
            Text(text = "$${decimalFormat.format(delivery)}")
        }

        // Hiển thị thuế
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text(
                text = "Thuế:",
                modifier = Modifier.weight(1f),
                color = colorResource(R.color.darkPurple)
            )
            Text(text = "$${decimalFormat.format(tax)}")
        }

        // Đường phân cách
        Box(
            modifier = Modifier
                .padding(top = 16.dp)
                .height(2.dp)
                .fillMaxWidth()
                .background(colorResource(R.color.darkPurple))
        )

        // Hiển thị tổng thanh toán
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            Text(
                text = "Tổng thanh toán:",
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.darkPurple)
            )
            Text(
                text = "$${decimalFormat.format(total)}",
                fontWeight = FontWeight.Bold
            )
        }
    }
}
