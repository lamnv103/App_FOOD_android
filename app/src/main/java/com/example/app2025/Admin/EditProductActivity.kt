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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.app2025.Admin.AddFood.CloudinaryUploader
import com.example.app2025.Admin.AddFood.FirebaseManager
import com.example.app2025.Domain.FoodModel
import com.example.app2025.ui.theme.App2025Theme
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.tasks.await

class EditProductActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val productId = intent.getStringExtra("PRODUCT_ID") ?: ""

        setContent {
            App2025Theme {
                EditProductScreen(
                    productId = productId,
                    onNavigateBack = { finish() },
                    onProductUpdated = {
                        Toast.makeText(this, "Sản phẩm đã được cập nhật", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductScreen(
    productId: String,
    onNavigateBack: () -> Unit,
    onProductUpdated: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // State variables
    var title by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var prepTime by remember { mutableStateOf("") }
    var isBestFood by remember { mutableStateOf(false) }
    var categoryId by remember { mutableStateOf("0") }
    var imageUrl by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // Load product data
    LaunchedEffect(productId) {
        loadProductData(
            productId = productId,
            onSuccess = { product ->
                title = product.Title
                price = product.Price.toString()
                description = product.Description
                calories = product.Calorie.toString()
                prepTime = product.TimeValue.toString()
                isBestFood = product.BestFood
                categoryId = product.CategoryId.toString()
                imageUrl = product.ImagePath
                isLoading = false
            },
            onError = {
                errorMessage = "Không thể tải thông tin sản phẩm: $it"
                isLoading = false
            }
        )
    }

    // Image picker launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chỉnh sửa sản phẩm") },
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
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
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
                        } else if (imageUrl.isNotEmpty()) {
                            AsyncImage(
                                model = imageUrl,
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
                            if (validateInputs(context, title, price)) {
                                isSaving = true
                                errorMessage = null

                                // Update product
                                updateProduct(
                                    context = context,
                                    productId = productId,
                                    imageUri = imageUri,
                                    currentImageUrl = imageUrl,
                                    title = title,
                                    price = price,
                                    description = description,
                                    calories = calories.toIntOrNull() ?: 0,
                                    prepTime = prepTime.toIntOrNull() ?: 15,
                                    categoryId = categoryId,
                                    isBestFood = isBestFood,
                                    onSuccess = {
                                        isSaving = false
                                        onProductUpdated()
                                    },
                                    onError = { error ->
                                        isSaving = false
                                        errorMessage = error
                                    }
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Đang lưu...")
                        } else {
                            Text("Cập nhật sản phẩm")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Loading overlay
            if (isSaving) {
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
                            Text("Đang cập nhật sản phẩm...")
                        }
                    }
                }
            }
        }
    }
}

private suspend fun loadProductData(
    productId: String,
    onSuccess: (FoodModel) -> Unit,
    onError: (String) -> Unit
) {
    try {
        val database = FirebaseDatabase.getInstance()
        val productRef = database.getReference("Foods").child(productId)

        val snapshot = productRef.get().await()
        val product = snapshot.getValue(FoodModel::class.java)

        if (product != null) {
            onSuccess(product)
        } else {
            onError("Không tìm thấy sản phẩm")
        }
    } catch (e: Exception) {
        onError(e.message ?: "Lỗi không xác định")
    }
}

private fun validateInputs(
    context: Context,
    title: String,
    price: String
): Boolean {
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

private fun updateProduct(
    context: Context,
    productId: String,
    imageUri: Uri?,
    currentImageUrl: String,
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
    // If image is changed, upload new image first
    if (imageUri != null) {
        CloudinaryUploader.uploadImage(
            context = context,
            imageUri = imageUri,
            onSuccess = { imageUrl ->
                saveProductData(
                    productId = productId,
                    imageUrl = imageUrl,
                    title = title,
                    price = price,
                    description = description,
                    calories = calories,
                    prepTime = prepTime,
                    categoryId = categoryId,
                    isBestFood = isBestFood,
                    onSuccess = onSuccess,
                    onError = onError
                )
            },
            onError = onError
        )
    } else {
        // Use existing image
        saveProductData(
            productId = productId,
            imageUrl = currentImageUrl,
            title = title,
            price = price,
            description = description,
            calories = calories,
            prepTime = prepTime,
            categoryId = categoryId,
            isBestFood = isBestFood,
            onSuccess = onSuccess,
            onError = onError
        )
    }
}

private fun saveProductData(
    productId: String,
    imageUrl: String,
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
    try {
        val updates = mapOf(
            "Title" to title,
            "Price" to (price.toDoubleOrNull() ?: 0.0),
            "Description" to description,
            "ImagePath" to imageUrl,
            "CategoryId" to categoryId,
            "TimeValue" to prepTime,
            "BestFood" to isBestFood,
            "Calorie" to calories
            // Keep other fields unchanged
        )

        FirebaseDatabase.getInstance().getReference("Foods")
            .child(productId)
            .updateChildren(updates)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onError("Lỗi khi cập nhật: ${e.message}")
            }
    } catch (e: Exception) {
        onError("Lỗi: ${e.message}")
    }
}

@Composable
fun CategorySelector(
    selectedCategoryId: String,
    onCategorySelected: (String) -> Unit
) {
    val categories = listOf(
        "0" to "Món chính",
        "1" to "Món phụ",
        "2" to "Tráng miệng",
        "3" to "Đồ uống"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { (id, name) ->
            FilterChip(
                selected = selectedCategoryId == id,
                onClick = { onCategorySelected(id) },
                label = { Text(name) }
            )
        }
    }
}