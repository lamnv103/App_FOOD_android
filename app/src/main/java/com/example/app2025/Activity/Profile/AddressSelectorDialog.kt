package com.example.app2025.Activity.Profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.app2025.Domain.AddressesModel
import com.example.app2025.R

@Composable
fun AddressSelectorDialog(
    addresses: List<AddressesModel>,
    selectedAddressId: String,
    onAddressSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    onAddNewAddressClick: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Chọn địa chỉ giao hàng",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (addresses.isEmpty()) {
                    Text(
                        text = "Bạn chưa có địa chỉ nào",
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .fillMaxWidth()
                    ) {
                        items(addresses) { address ->
                            AddressItem(
                                address = address,
                                isSelected = address.id == selectedAddressId,
                                onClick = { onAddressSelected(address.id) }
                            )
                        }
                    }
                }

                Button(
                    onClick = onAddNewAddressClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.orange)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text("Thêm địa chỉ mới")
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 8.dp)
                ) {
                    Text("Đóng")
                }
            }
        }
    }
}

@Composable
fun AddressItem(
    address: AddressesModel,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val addressText = formatAddress(address)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .selectable(
                selected = isSelected,
                onClick = onClick
            ),
        color = if (isSelected) colorResource(R.color.grey) else Color.White,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick
            )

            Column(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .weight(1f)
            ) {
                Text(
                    text = address.name,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = addressText,
                    fontSize = 14.sp
                )

                if (address.type == "default") {
                    Text(
                        text = "Mặc định",
                        color = colorResource(R.color.orange),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

// Helper function to format address
fun formatAddress(address: AddressesModel): String {
    val parts = mutableListOf<String>()

    if (address.address.isNotEmpty()) parts.add(address.address)
    if (address.locality.isNotEmpty()) parts.add(address.locality)
    if (address.landmark.isNotEmpty()) parts.add(address.landmark)
    if (address.city.isNotEmpty()) parts.add(address.city)
    if (address.state.isNotEmpty()) parts.add(address.state)

    return parts.joinToString(", ")
}