package com.example.app2025.Activity.Dashboard

import android.content.Intent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.app2025.Activity.DetailEachFood.DetailEachFoodActivity
import com.example.app2025.Domain.FoodModel
import com.example.app2025.R
import com.uilover.project2142.Helper.ManagementFavorite
import com.uilover.project2142.Helper.ManagmentCart
import java.text.NumberFormat
import java.util.*

@Composable
fun FeaturedProductSection(
    foods: List<FoodModel>,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp)
    ) {
        Text(
            text = "Sản phẩm nổi bật",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.darkPurple),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = colorResource(R.color.darkPurple))
                }
            }

            foods.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Không có sản phẩm nào",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
            }

            else -> {
                val featuredFoods by remember(foods) {
                    derivedStateOf { foods.filter { it.BestFood } }
                }

                if (featuredFoods.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        items(featuredFoods, key = { it.Id }) { food ->
                            FeaturedFoodItem(food = food)
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Không có sản phẩm nổi bật",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FeaturedFoodItem(food: FoodModel) {
    val context = LocalContext.current
    val managementFavorite = remember { ManagementFavorite(context) }
    val managmentCart = remember { ManagmentCart(context) }
    val isFavorite by remember { derivedStateOf { managementFavorite.isFavorite(food) } }
    val scale by animateFloatAsState(if (isFavorite) 1.2f else 1f)

    Card(
        modifier = Modifier
            .width(380.dp)
            .height(250.dp)
            .clickable {
                val intent = Intent(context, DetailEachFoodActivity::class.java).apply {
                    putExtra("food", food)
                }
                context.startActivity(intent)
            }
            .semantics { contentDescription = "Xem chi tiết của ${food.Title}" },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .width(140.dp)
                    .fillMaxHeight()
                    .background(
                        color = colorResource(R.color.lightOrange),
                        shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = food.ImagePath,
                    contentDescription = food.Title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                    placeholder = painterResource(R.drawable.image),
                    error = painterResource(R.drawable.flame)
                )

                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = colorResource(R.color.darkPurple)
                ) {
                    Text(
                        text = "Best",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .padding(24.dp), // Tăng padding để thoáng hơn
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = food.Title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(R.color.darkPurple),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = food.Description,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "Đánh giá",
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = food.Star.toString(),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AccessTime,
                                contentDescription = "Thời gian",
                                tint = Color.Gray,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "${food.TimeValue} phút",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Image(
                        painter = painterResource(R.drawable.fav_icon),
                        contentDescription = if (isFavorite) "Xóa khỏi yêu thích" else "Thêm vào yêu thích",
                        modifier = Modifier
                            .size(24.dp)
                            .scale(scale)
                            .clickable {
                                if (isFavorite) {
                                    managementFavorite.removeFromFavorite(food)
                                } else {
                                    managementFavorite.addToFavorite(food)
                                }
                            },
                        colorFilter = ColorFilter.tint(
                            if (isFavorite) Color.Red else Color.Gray
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatPrice(food.Price),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(R.color.darkPurple)
                    )

                    Button(
                        onClick = {
                            managmentCart.insertItem(food)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(R.color.darkPurple)
                        ),
                        shape = RoundedCornerShape(50), // Nút dạng viên thuốc
                        modifier = Modifier.height(40.dp) // Tăng chiều cao nút
                    ) {
                        Text(
                            text = "Thêm",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private fun formatPrice(price: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    format.currency = Currency.getInstance("VND")
    return format.format(price)
}