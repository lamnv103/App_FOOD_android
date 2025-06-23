package com.example.app2025.Activity.Cart

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app2025.Activity.Profile.AddressSelectorDialog
import com.example.app2025.Activity.Profile.AddAddressDialog
import com.example.app2025.Activity.Profile.formatAddress
import com.example.app2025.Domain.AddressesModel
import com.example.app2025.R

@Composable
fun DeliveryInfoBox(
    addresses: List<AddressesModel> = emptyList(),
    selectedAddressId: String = "",
    selectedPaymentMethod: String = "Cash",
    onAddressSelected: (String) -> Unit = {},
    onPaymentMethodSelected: (String) -> Unit = {},
    onPlaceOrder: () -> Unit = {},
    onAddNewAddress: (AddressesModel) -> Unit = {}
) {
    var showAddressSelector by remember { mutableStateOf(false) }
    var showAddAddressDialog by remember { mutableStateOf(false) }
    var showPaymentSelector by remember { mutableStateOf(false) }

    // Use a local state to track the selected payment method
    var paymentMethod by remember { mutableStateOf(selectedPaymentMethod) }

    // Update the parent when the payment method changes
    LaunchedEffect(paymentMethod) {
        onPaymentMethodSelected(paymentMethod)
    }

    // Find selected address and payment
    val selectedAddress = addresses.find { it.id == selectedAddressId }
    val addressText = selectedAddress?.let { formatAddress(it) }
        ?: "Nhấn để chọn địa chỉ giao hàng"

    val paymentText = when (paymentMethod) {
        "Cash" -> "Tiền mặt"
        "ZaloPay" -> "ZaloPay"
        else -> "Chọn phương thức thanh toán"
    }

    // Address dialogs
    if (showAddressSelector) {
        if (addresses.isEmpty()) {
            showAddAddressDialog = true
            showAddressSelector = false
        } else {
            AddressSelectorDialog(
                addresses = addresses,
                selectedAddressId = selectedAddressId,
                onAddressSelected = {
                    onAddressSelected(it)
                    showAddressSelector = false
                },
                onDismiss = { showAddressSelector = false },
                onAddNewAddressClick = {
                    showAddressSelector = false
                    showAddAddressDialog = true
                }
            )
        }
    }
    if (showAddAddressDialog) {
        AddAddressDialog(
            onAddressAdded = { newAddress ->
                onAddNewAddress(newAddress)
                if (newAddress.id.isNotEmpty()) onAddressSelected(newAddress.id)
                showAddAddressDialog = false
            },
            onDismiss = { showAddAddressDialog = false }
        )
    }

    // Payment selector dialog
    if (showPaymentSelector) {
        PaymentSelectorDialog(
            selectedMethod = paymentMethod,
            onMethodSelected = {
                paymentMethod = it
                showPaymentSelector = false
            },
            onDismiss = { showPaymentSelector = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .background(color = colorResource(R.color.grey), shape = RoundedCornerShape(10.dp))
            .padding(8.dp)
    ) {
        InfoItem(
            title = "Địa chỉ giao hàng của bạn:",
            content = addressText,
            icon = painterResource(R.drawable.location),
            isClickable = true,
            onClick = { showAddressSelector = true }
        )

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        InfoItem(
            title = "Phương thức thanh toán:",
            content = paymentText,
            icon = painterResource(R.drawable.credit_card),
            isClickable = true,
            onClick = { showPaymentSelector = true }
        )
    }

    Button(
        onClick = {
            if (selectedAddressId.isNotEmpty() && selectedPaymentMethod.isNotEmpty()) {
                onPlaceOrder()
            } else {
                if (selectedAddressId.isEmpty()) showAddressSelector = true
                else showPaymentSelector = true
            }
        },
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.orange)),
        modifier = Modifier
            .padding(vertical = 32.dp)
            .fillMaxWidth()
            .height(50.dp)
    ) {
        val buttonText = when {
            selectedAddressId.isEmpty() -> "Vui lòng chọn địa chỉ"
            selectedPaymentMethod.isEmpty() -> "Vui lòng chọn phương thức"
            else -> "Đặt hàng"
        }
        Text(text = buttonText, fontSize = 18.sp, color = Color.White)
    }
}

@Composable
fun InfoItem(
    title: String,
    content: String,
    icon: Painter,
    isClickable: Boolean = false,
    onClick: () -> Unit = {}
) {
    val modifier = if (isClickable) Modifier.clickable(onClick = onClick) else Modifier
    Column(modifier = modifier) {
        Text(text = title, fontSize = 14.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(painter = icon, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = content,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isClickable && content.startsWith("Nhấn")) colorResource(R.color.orange) else Color.Black
            )
        }
    }
}

// PaymentSelectorDialog.kt
@Composable
fun PaymentSelectorDialog(
    selectedMethod: String,
    onMethodSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Chọn phương thức thanh toán") },
        text = {
            Column {
                val methods = listOf("Cash", "ZaloPay")
                methods.forEach { method ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onMethodSelected(method) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = method == selectedMethod,
                            onClick = { onMethodSelected(method) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = if (method == "Cash") "Tiền mặt" else "ZaloPay")
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy") }
        }
    )
}
