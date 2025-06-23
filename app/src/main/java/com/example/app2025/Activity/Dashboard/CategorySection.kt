package com.example.app2025.Activity.Dashboard

import android.content.Intent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import coil.compose.AsyncImage
import com.example.app2025.Activity.ItemList.ItemListActivity
import com.example.app2025.Domain.CategoryModel
import com.example.app2025.R

@Composable
fun CategorySection(categories: SnapshotStateList<CategoryModel>, showCategoryLoading: Boolean) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Danh mục",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.darkPurple)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { /* HaLndle view all click */ }
            ) {
                Text(
                    text = "Xem tất cả",
                    fontSize = 14.sp,
                    color = colorResource(R.color.darkPurple).copy(alpha = 0.7f)
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "View all",
                    tint = colorResource(R.color.darkPurple).copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        if (showCategoryLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = colorResource(R.color.darkPurple)
                )
            }
        } else {
            val context = LocalContext.current
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(categories) { category ->
                    CategoryItem(
                        category = category,
                        onItemClick = {
                            val intent = Intent(context, ItemListActivity::class.java).apply {
                                putExtra("id", category.Id.toString())
                                putExtra("title", category.Name)
                            }
                            startActivity(context, intent, null)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryItem(category: CategoryModel, onItemClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(130.dp)
            .height(150.dp) // giảm chiều cao
            .clickable(onClick = onItemClick)
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = 300,
                    easing = LinearOutSlowInEasing
                )
            ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.light_green)
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(Color.White), // chỉnh lại màu nền rõ ràng hơn
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = category.ImagePath,
                        contentDescription = category.Name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(70.dp)
                            .padding(8.dp)
                    )
                }
            }

            Text(
                text = category.Name,
                color = colorResource(R.color.darkPurple),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            )
        }
    }
}
