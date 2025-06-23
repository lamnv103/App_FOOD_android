package com.example.app2025.Admin.AddFood

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import coil.compose.AsyncImage
import com.example.app2025.Admin.AddProductActivity
import com.example.app2025.Admin.EditProductActivity
import com.example.app2025.Domain.FoodModel
import com.example.app2025.ui.theme.App2025Theme
import com.google.firebase.database.*
import java.text.NumberFormat
import java.util.Locale

class ProductManagementActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App2025Theme {
                ProductManagementScreen(
                    onNavigateBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductManagementScreen(onNavigateBack: () -> Unit) {
    val viewModel = remember { FoodViewModel() }
    val foods by viewModel.loadFood().observeAsState(emptyList())
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var isSearchVisible by remember { mutableStateOf(false) }

    val filteredFoods = foods.filter { food ->
        (searchQuery.isEmpty() || food.Title.contains(searchQuery, ignoreCase = true)) &&
                (selectedCategory == null || food.CategoryId.toString() == selectedCategory)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản lý Sản phẩm") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    IconButton(onClick = { isSearchVisible = !isSearchVisible }) {
                        Icon(Icons.Default.Search, contentDescription = "Tìm kiếm")
                    }
                    IconButton(onClick = {
                        try {
                            val intent = Intent(context, AddProductActivity::class.java)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Log.e("ProductManagement", "Error: ${e.message}")
                            Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Thêm sản phẩm")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Collapsible Search Bar
            AnimatedVisibility(visible = isSearchVisible) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Tìm kiếm sản phẩm...") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Xóa")
                            }
                        }
                    },
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                )
            }

            // Category filter with horizontal scroll
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val categories = listOf(
                    null to "Tất cả",
                    "0" to "Món chính",
                    "1" to "Món phụ",
                    "2" to "Tráng miệng",
                    "3" to "Đồ uống"
                )

                categories.forEach { (id, name) ->
                    FilterChip(
                        selected = selectedCategory == id,
                        onClick = { selectedCategory = id },
                        label = { Text(name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            if (filteredFoods.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (foods.isEmpty()) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    } else {
                        Text("Không tìm thấy sản phẩm nào", color = Color.Gray, fontSize = 16.sp)
                    }
                }
            } else {
                Text(
                    text = "Tổng số: ${filteredFoods.size} sản phẩm",
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredFoods, key = { it.Id }) { food ->
                        ProductItem(
                            food = food,
                            onDelete = {
                                viewModel.deleteFood(food.Id)
                                Toast.makeText(context, "Đã xóa sản phẩm", Toast.LENGTH_SHORT).show()
                            },
                            onEdit = {
                                val intent = Intent(context, EditProductActivity::class.java).apply {
                                    putExtra("PRODUCT_ID", food.Id.toString())
                                }
                                context.startActivity(intent)
                            }
                        )
                    }

                    // Bottom spacing
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
fun ProductItem(food: FoodModel, onDelete: () -> Unit, onEdit: () -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Xác nhận xóa", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có chắc muốn xóa sản phẩm \"${food.Title}\"?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Xóa")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = food.ImagePath,
                    contentDescription = food.Title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                ) {
                    Text(
                        text = food.Title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Giá: ${formatPrice(food.Price)}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = getCategoryName(food.CategoryId.toString()),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (food.BestFood) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFB347),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Món đặc biệt",
                                fontSize = 12.sp,
                                color = Color(0xFFFFB347)
                            )
                        }
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Sửa",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(onClick = { showDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Xóa",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Expanded details
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Mô tả:",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )

                    Text(
                        text = food.Description.ifEmpty { "Không có mô tả" },
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column {
                            Text(
                                text = "Calories:",
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "${food.Calorie}",
                                fontSize = 14.sp
                            )
                        }

                        Column {
                            Text(
                                text = "Thời gian chuẩn bị:",
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "${food.TimeValue} phút",
                                fontSize = 14.sp
                            )
                        }

                        Column {
                            Text(
                                text = "Đánh giá:",
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "${food.Star} ⭐",
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatPrice(price: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    return format.format(price)
}

private fun getCategoryName(categoryId: String): String {
    return when (categoryId) {
        "0" -> "Món chính"
        "1" -> "Món phụ"
        "2" -> "Tráng miệng"
        "3" -> "Đồ uống"
        else -> "Không xác định"
    }
}

class FoodViewModel {
    private val firebaseDatabase = FirebaseDatabase.getInstance()

    fun loadFood(): LiveData<MutableList<FoodModel>> {
        val listData = MutableLiveData<MutableList<FoodModel>>()
        val ref = firebaseDatabase.getReference("Foods")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<FoodModel>()
                for (childSnapshot in snapshot.children) {
                    val item = childSnapshot.getValue(FoodModel::class.java)
                    item?.let { list.add(it) }
                }
                listData.value = list
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Database error: ${error.message}")
            }
        })

        return listData
    }

    fun deleteFood(foodId: Long) {
        firebaseDatabase.getReference("Foods").child(foodId.toString())
            .removeValue()
            .addOnSuccessListener {
                Log.d("Firebase", "Food item $foodId deleted successfully")
            }
            .addOnFailureListener { error ->
                Log.e("Firebase", "Failed to delete food item $foodId: ${error.message}")
            }
    }
}