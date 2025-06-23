// AddProductActivity.kt
package com.example.app2025.Admin

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.app2025.Admin.AddFood.CloudinaryUploader
import com.example.app2025.Admin.AddFood.FirebaseManager
import com.example.app2025.ui.theme.App2025Theme

class AddProductActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App2025Theme {
                AdminAddProductScreen(
                    onNavigateBack = { finish() },
                    onProductAdded = {
                        // Khi sản phẩm được thêm thành công, đóng màn hình
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAddProductScreen(
    onNavigateBack: () -> Unit,
    onProductAdded: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // State variables
    var title by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var prepTime by remember { mutableStateOf("") }
    var isBestFood by remember { mutableStateOf(false) }
    var categoryId by remember { mutableStateOf("0") } // Default category

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Image picker launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thêm sản phẩm mới") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
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
            // Main content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Image selection
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { launcher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = "Ảnh sản phẩm",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Nhấn để chọn ảnh")
                        }
                    }
                }

                // Basic information
                Text(
                    text = "Thông tin cơ bản",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Tên món ăn") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Giá (VNĐ)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Mô tả") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )

                // Additional information
                Text(
                    text = "Thông tin bổ sung",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = calories,
                        onValueChange = { calories = it },
                        label = { Text("Calories") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = prepTime,
                        onValueChange = { prepTime = it },
                        label = { Text("Thời gian (phút)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Category selection
                Text("Danh mục")
                CategorySelector(
                    selectedCategoryId = categoryId,
                    onCategorySelected = { categoryId = it }
                )

                // Best food checkbox
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isBestFood,
                        onCheckedChange = { isBestFood = it }
                    )
                    Text("Đánh dấu là món đặc biệt")
                }

                // Error message
                errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Submit button
                Button(
                    onClick = {
                        if (validateInputs(context, imageUri, title, price)) {
                            isUploading = true
                            errorMessage = null

                            // Upload and save product
                            uploadProductWithImage(
                                context = context,
                                imageUri = imageUri!!,
                                title = title,
                                price = price,
                                description = description,
                                calories = calories.toIntOrNull() ?: 0,
                                prepTime = prepTime.toIntOrNull() ?: 15,
                                categoryId = categoryId,
                                isBestFood = isBestFood,
                                onSuccess = {
                                    isUploading = false
                                    Toast.makeText(context, "Thêm sản phẩm thành công", Toast.LENGTH_SHORT).show()
                                    onProductAdded()
                                },
                                onError = { error ->
                                    isUploading = false
                                    errorMessage = error
                                }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isUploading
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Đang tải lên...")
                    } else {
                        Text("Thêm sản phẩm")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Loading overlay
            if (isUploading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .padding(16.dp)
                            .width(300.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Đang tải lên sản phẩm...")
                        }
                    }
                }
            }
        }
    }
}



private fun validateInputs(
    context: Context,
    imageUri: Uri?,
    title: String,
    price: String
): Boolean {
    if (imageUri == null) {
        Toast.makeText(context, "Vui lòng chọn ảnh sản phẩm", Toast.LENGTH_SHORT).show()
        return false
    }

    if (title.isBlank()) {
        Toast.makeText(context, "Vui lòng nhập tên sản phẩm", Toast.LENGTH_SHORT).show()
        return false
    }

    if (price.isBlank() || price.toDoubleOrNull() == null || price.toDouble() <= 0) {
        Toast.makeText(context, "Vui lòng nhập giá hợp lệ", Toast.LENGTH_SHORT).show()
        return false
    }

    return true
}

private fun uploadProductWithImage(
    context: Context,
    imageUri: Uri,
    title: String,
    price: String,
    description: String,
    calories: Int,
    prepTime: Int,
    categoryId: String,
    isBestFood: Boolean,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    // Upload image to Cloudinary
    CloudinaryUploader.uploadImage(
        context = context,
        imageUri = imageUri,
        onSuccess = { imageUrl ->
            // Create product data
            val product = mapOf(
                "Title" to title,
                "Price" to (price.toDoubleOrNull() ?: 0.0),
                "Description" to description,
                "ImagePath" to imageUrl,
                "Id" to System.currentTimeMillis(),
                "CategoryId" to categoryId,
                "LocationId" to 1,
                "PriceId" to 1,
                "TimeId" to 1,
                "TimeValue" to prepTime,
                "Star" to 4.5,
                "BestFood" to isBestFood,
                "Calorie" to calories,
                "numberInCart" to 0
            )

            // Save to Firebase
            FirebaseManager.addProduct(
                product = product,
                onSuccess = onSuccess,
                onError = onError
            )
        },
        onError = onError
    )
}