package com.example.app2025.Activity.Favorite

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import coil.compose.rememberAsyncImagePainter
import com.example.app2025.Activity.BaseActivity
import com.example.app2025.Domain.FoodModel
import com.example.app2025.R
import com.uilover.project2142.Helper.ManagementFavorite
import com.uilover.project2142.Helper.ManagmentCart
import java.text.DecimalFormat

class FavoriteActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FavoriteScreen(
                managementFavorite = ManagementFavorite(this),
                managmentCart = ManagmentCart(this),
                onBackClick = { finish() }
            )
        }
    }
}

@Composable
fun FavoriteScreen(
    managementFavorite: ManagementFavorite,
    managmentCart: ManagmentCart,
    onBackClick: () -> Unit,
) {
    val favoriteList by remember { derivedStateOf { managementFavorite.getFavoriteList() } }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        item {
            ConstraintLayout(modifier = Modifier.padding(top = 36.dp)) {
                val (backBtn, titleTxt) = createRefs()
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .constrainAs(titleTxt) { centerTo(parent) },
                    text = "Danh Sách Yêu Thích",
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 25.sp
                )
                Image(
                    painter = painterResource(R.drawable.back_grey),
                    contentDescription = null,
                    modifier = Modifier
                        .constrainAs(backBtn) {
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            start.linkTo(parent.start)
                        }
                        .clickable { onBackClick() }
                )
            }
        }

        if (favoriteList.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.fav_icon),
                        contentDescription = null,
                        modifier = Modifier.size(100.dp)
                    )
                    Text(
                        text = "Chưa có món nào được yêu thích!",
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Thêm món ngay nào!",
                        color = colorResource(R.color.orange),
                        modifier = Modifier.clickable { /* Chuyển đến màn hình danh sách món */ }
                    )
                }
            }
        } else {
            items(favoriteList, key = { it.Title }) { item ->
                FavoriteItem(
                    item = item,
                    managementFavorite = managementFavorite,
                    managmentCart = managmentCart
                )
            }
        }
    }
}