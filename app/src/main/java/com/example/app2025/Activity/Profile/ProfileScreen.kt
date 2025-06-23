package com.example.app2025.Activity.Profile

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter

import com.example.app2025.Domain.AddressesModel
import com.example.app2025.Login.AuthViewModel
import com.example.app2025.R
import com.example.app2025.ViewModel.ProfileEvent
import com.example.app2025.ViewModel.ProfileState
import java.util.*
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext

@Composable
fun ProfileScreen(
    state: ProfileState,
    onEvent: (ProfileEvent) -> Unit,
    onNavigateBack: () -> Unit,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onEvent(ProfileEvent.SelectImage(it)) }
    }

    // Date picker
    LaunchedEffect(state.showDatePicker) {
        if (state.showDatePicker) {
            val calendar = Calendar.getInstance()
            // Parse existing date if available
            if (state.birthdayInput.isNotEmpty()) {
                try {
                    val parts = state.birthdayInput.split("/")
                    if (parts.size == 3) {
                        val day = parts[0].toInt()
                        val month = parts[1].toInt() - 1 // Calendar months are 0-based
                        val year = parts[2].toInt()
                        calendar.set(year, month, day)
                    }
                } catch (e: Exception) {
                    // If parsing fails, use current date
                }
            }

            val datePickerDialog = DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    val formattedDate = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
                    onEvent(ProfileEvent.UpdateBirthday(formattedDate))
                    onEvent(ProfileEvent.HideDatePicker)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )

            datePickerDialog.setOnCancelListener {
                onEvent(ProfileEvent.HideDatePicker)
            }

            datePickerDialog.show()
        }
    }

    // Address selector dialog
    if (state.showAddressSelector) {
        AddressSelectorDialog(
            addresses = state.userAddresses,
            selectedAddressId = state.selectedAddressId,
            onAddressSelected = { addressId ->
                onEvent(ProfileEvent.SelectAddress(addressId))
                onEvent(ProfileEvent.HideAddressSelector)
            },
            onDismiss = { onEvent(ProfileEvent.HideAddressSelector) }
        )
    }

    // Success or error messages
    LaunchedEffect(state.successMessage, state.error) {
        state.successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }

        state.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    var showChangePasswordDialog by remember { mutableStateOf(false) }
    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showChangePasswordDialog = false }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            colorResource(id = R.color.profile_gradient_start),
                            colorResource(id = R.color.profile_gradient_end)
                        )
                    )
                )
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar with back button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Text(
                    text = "Hồ sơ cá nhân",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Profile picture
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // User image or placeholder
                val imagePainter = if (state.selectedImageUri != null) {
                    rememberAsyncImagePainter(state.selectedImageUri)
                } else if (!state.user?.image.isNullOrEmpty()) {
                    // Log để debug
                    android.util.Log.d("ProfileScreen", "Hiển thị ảnh từ URL: ${state.user?.image}")
                    rememberAsyncImagePainter(
                        model = state.user?.image,
                        error = painterResource(id = R.drawable.image)
                    )
                } else {
                    painterResource(id = R.drawable.image)
                }

                Image(
                    painter = imagePainter,
                    contentDescription = "Profile Picture",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Camera icon for changing profile picture
                if (state.isEditMode) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(colorResource(id = R.color.orange))
                            .clickable { imagePickerLauncher.launch("image/*") }
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "Change Photo",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // User name
            Text(
                text = state.user?.name ?: "",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            // User email
            Text(
                text = state.user?.email ?: "",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ActionButton(
                    icon = Icons.Default.History,
                    label = "Lịch sử",
                    onClick = { /* Handle history click */ }
                )

                ActionButton(
                    icon = Icons.Default.Notifications,
                    label = "Thông báo",
                    onClick = { /* Handle notification click */ }
                )

                ActionButton(
                    icon = Icons.Default.ExitToApp,
                    label = "Đăng xuất",
                    onClick = {
                        authViewModel.signout()
                        onNavigateBack()
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Profile information card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    if (state.isEditMode) {
                        // Edit mode - show form fields
                        ProfileEditForm(
                            state = state,
                            onEvent = onEvent
                        )
                    } else {
                        // View mode - show profile info
                        ProfileInfoItem(
                            label = "Mật khẩu:",
                            value = "••••••••",
                            showChangeButton = true,
                            onChangeClick = { showChangePasswordDialog = true }
                        )

                        ProfileInfoItem(
                            label = "Ngày sinh:",
                            value = state.user?.birthday ?: "Chưa cập nhật"
                        )

                        ProfileInfoItem(
                            label = "Số điện thoại:",
                            value = state.user?.phone ?: "Chưa cập nhật"
                        )

                        // Display formatted address
                        val addressText = if (state.selectedAddress != null) {
                            formatAddress(state.selectedAddress)
                        } else {
                            "Chưa cập nhật"
                        }

                        ProfileInfoItem(
                            label = "Địa chỉ:",
                            value = addressText,
                            isLast = true
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Edit/Save button
            Button(
                onClick = {
                    if (state.isEditMode) {
                        onEvent(ProfileEvent.SaveProfile(context)) // Truyền context vào đây
                    } else {
                        onEvent(ProfileEvent.ToggleEditMode)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.profile_button)
                )
            ) {
                Text(
                    text = if (state.isEditMode) "LƯU THÔNG TIN" else "CHỈNH SỬA HỒ SƠ",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (state.isEditMode) {
                Spacer(modifier = Modifier.height(16.dp))

                // Cancel button
                OutlinedButton(
                    onClick = { onEvent(ProfileEvent.ToggleEditMode) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colorResource(id = R.color.profile_button)
                    )
                ) {
                    Text(
                        text = "HỦY",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp)) // Space for bottom navigation
        }

    }

    // Loading indicator
    if (state.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = colorResource(id = R.color.orange))
        }
    }
}

@Composable
fun ProfileEditForm(
    state: ProfileState,
    onEvent: (ProfileEvent) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Chỉnh sửa thông tin",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(id = R.color.orange),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Name field
        OutlinedTextField(
            value = state.nameInput,
            onValueChange = { onEvent(ProfileEvent.UpdateName(it)) },
            label = { Text("Họ và tên") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Name"
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true,
            isError = state.nameInput.isEmpty()
        )

        if (state.nameInput.isEmpty()) {
            Text(
                text = "Họ và tên không được để trống",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, bottom = 16.dp)
            )
        }

        // Phone field
        OutlinedTextField(
            value = state.phoneInput,
            onValueChange = { onEvent(ProfileEvent.UpdatePhone(it)) },
            label = { Text("Số điện thoại") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = "Phone"
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true,
            isError = state.phoneInput.isEmpty()
        )

        if (state.phoneInput.isEmpty()) {
            Text(
                text = "Số điện thoại không được để trống",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, bottom = 16.dp)
            )
        }

        // Birthday field
        OutlinedTextField(
            value = state.birthdayInput,
            onValueChange = { /* Not allowing manual entry */ },
            label = { Text("Ngày sinh") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Birthday"
                )
            },
            trailingIcon = {
                IconButton(onClick = { onEvent(ProfileEvent.ShowDatePicker) }) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Select Date"
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .clickable { onEvent(ProfileEvent.ShowDatePicker) },
            singleLine = true,
            readOnly = true
        )

        // Address field - now a selector
        val selectedAddressText = if (state.selectedAddress != null) {
            formatAddress(state.selectedAddress)
        } else if (state.selectedAddressId.isNotEmpty() && state.userAddresses.isNotEmpty()) {
            val address = state.userAddresses.find { it.id == state.selectedAddressId }
            if (address != null) formatAddress(address) else "Chọn địa chỉ"
        } else {
            "Chọn địa chỉ"
        }

        OutlinedTextField(
            value = selectedAddressText,
            onValueChange = { /* Not allowing manual entry */ },
            label = { Text("Địa chỉ") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Address"
                )
            },
            trailingIcon = {
                IconButton(onClick = { onEvent(ProfileEvent.ShowAddressSelector) }) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select Address"
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onEvent(ProfileEvent.ShowAddressSelector) },
            readOnly = true
        )
    }
}

@Composable
fun AddressSelectorDialog(
    addresses: List<AddressesModel>,
    selectedAddressId: String,
    onAddressSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Chọn địa chỉ",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (addresses.isEmpty()) {
                    Text(
                        text = "Bạn chưa có địa chỉ nào. Vui lòng thêm địa chỉ mới.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
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

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Hủy")
                    }

                    // Add a button to add new address if needed
                    /*
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { /* Navigate to add address screen */ }) {
                        Text("Thêm địa chỉ mới")
                    }
                    */
                }
            }
        }
    }
}


@Composable
fun ProfileInfoItem(
    label: String,
    value: String,
    showChangeButton: Boolean = false,
    onChangeClick: () -> Unit = {},
    isLast: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = value,
            color = Color.Black,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )

        if (showChangeButton) {
            TextButton(
                onClick = onChangeClick,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "Đổi",
                    color = colorResource(id = R.color.orange),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    if (!isLast) {
        Divider(
            color = Color.LightGray.copy(alpha = 0.5f),
            thickness = 1.dp
        )
    }
}

@Composable
fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(Color.White)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = colorResource(id = R.color.orange),
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}
