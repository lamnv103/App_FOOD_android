// DeliveryScreen.kt
package com.example.app2025

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.activity.ComponentActivity
import com.example.app2025.Activity.Cart.DeliveryInfoBox
import com.example.app2025.Domain.AddressesModel
import com.example.app2025.ViewModel.CartEvent
import com.example.app2025.ViewModel.CartViewModel
import com.example.app2025.ViewModel.PaymentResult
import com.uilover.project2142.Helper.ManagmentCart
import kotlin.math.roundToInt

@Composable
fun DeliveryScreen(
    viewModel: CartViewModel,
    onAddAddress: (AddressesModel) -> Unit,
    onOrderCreated: (String) -> Unit,
    onPay: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    var selectedAddressId by remember { mutableStateOf("") }
    val zpToken = state.zpToken
    val paymentResult = state.paymentResult
    val context = LocalContext.current
    val activity = context as? ComponentActivity ?: return

    // Tính toán tổng số tiền đầy đủ
    val managmentCart = ManagmentCart(context)
    val itemTotal = managmentCart.getTotalFee()
    val percenTax = 0.02
    val tax = Math.round((itemTotal * percenTax) * 100) / 100.0
    val delivery = 10.0
    val fullTotal = itemTotal + tax + delivery

    // Auto-select first address if available and none selected
    LaunchedEffect(state.userAddresses) {
        if (state.userAddresses.size == 1 && selectedAddressId.isEmpty()) {
            selectedAddressId = state.userAddresses[0].id
        }
    }

    if (state.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }

    DeliveryInfoBox(
        addresses = state.userAddresses,
        selectedAddressId = selectedAddressId,
        selectedPaymentMethod = "ZaloPay",
        onAddressSelected = { selectedAddressId = it },
        onPaymentMethodSelected = { /* Có thể xử lý nếu cần */ },
        onPlaceOrder = {
            if (selectedAddressId.isNotEmpty()) {
                // Chuyển fullTotal từ nghìn đồng thành đồng (nhân với 1000)
                val amount = (fullTotal * 1000).toLong().toString() // Ví dụ: 123.45 -> 123450
                onOrderCreated(amount)
            } else {
                Toast.makeText(context, "Vui lòng chọn địa chỉ", Toast.LENGTH_SHORT).show()
            }
        },
        onAddNewAddress = onAddAddress
    )

    // Khi nhận được token, gọi hàm onPay để mở ứng dụng ZaloPay
    LaunchedEffect(zpToken) {
        zpToken?.let { token ->
            android.util.Log.d("ZaloPay", "Launching ZaloPay with token: $token")
            onPay(token)
        }
    }

    // Hiển thị kết quả thanh toán
    paymentResult?.let { result ->
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(CartEvent.ResetPaymentResult) },
            title = {
                Text(
                    when (result) {
                        is PaymentResult.Success -> "Thanh toán thành công"
                        is PaymentResult.Canceled -> "Đã huỷ thanh toán"
                        is PaymentResult.Error -> "Lỗi thanh toán"
                        else -> "Kết quả thanh toán"
                    }
                )
            },
            text = {
                Text(
                    when (result) {
                        is PaymentResult.Success -> "Mã giao dịch: ${result.transactionId}"
                        is PaymentResult.Canceled -> "Người dùng đã huỷ."
                        is PaymentResult.Error -> "Mã lỗi: ${result.error}"
                        else -> "Không có thông tin chi tiết"
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onEvent(CartEvent.ResetPaymentResult)
                    if (result is PaymentResult.Success) {
                        // Đặt hàng sau khi thanh toán thành công
                        viewModel.onEvent(CartEvent.PlaceOrder(selectedAddressId, "ZaloPay"))
                    }
                }) { Text("OK") }
            }
        )
    }
}