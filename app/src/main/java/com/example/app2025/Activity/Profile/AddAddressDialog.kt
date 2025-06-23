package com.example.app2025.Activity.Profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
fun AddAddressDialog(
    onAddressAdded: (AddressesModel) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("Việt Nam") }
    var zip by remember { mutableStateOf("") }
    var locality by remember { mutableStateOf("") }
    var landmark by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("home") } // default type

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
                    text = "Thêm địa chỉ mới",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Họ và tên") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Địa chỉ") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )

                OutlinedTextField(
                    value = locality,
                    onValueChange = { locality = it },
                    label = { Text("Phường/Xã") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )

                OutlinedTextField(
                    value = landmark,
                    onValueChange = { landmark = it },
                    label = { Text("Quận/Huyện") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("Thành phố") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 4.dp)
                    )

                    OutlinedTextField(
                        value = state,
                        onValueChange = { state = it },
                        label = { Text("Tỉnh") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 4.dp)
                    )
                }

                OutlinedTextField(
                    value = zip,
                    onValueChange = { zip = it },
                    label = { Text("Mã bưu điện") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = type == "home",
                            onClick = { type = "home" }
                        )
                        Text("Nhà riêng")
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = type == "work",
                            onClick = { type = "work" }
                        )
                        Text("Cơ quan")
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = type == "other",
                            onClick = { type = "other" }
                        )
                        Text("Khác")
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Hủy")
                    }

                    Button(
                        onClick = {
                            // Create new address
                            val newAddress = AddressesModel(
                                id = "", // Will be generated in ViewModel
                                user_id = "", // Will be set in ViewModel
                                name = name,
                                address = address,
                                locality = locality,
                                landmark = landmark,
                                city = city,
                                state = state,
                                country = country,
                                zip = zip,
                                type = type
                            )
                            onAddressAdded(newAddress)
                        },
                        enabled = name.isNotEmpty() && address.isNotEmpty() && city.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(R.color.orange)
                        ),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text("Lưu")
                    }
                }
            }
        }
    }
}

