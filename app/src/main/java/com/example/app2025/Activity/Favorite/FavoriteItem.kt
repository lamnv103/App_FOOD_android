package com.example.app2025.Activity.Favorite

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import coil.compose.rememberAsyncImagePainter
import com.example.app2025.Domain.FoodModel
import com.example.app2025.R
import com.uilover.project2142.Helper.ManagementFavorite
import com.uilover.project2142.Helper.ManagmentCart
import java.text.DecimalFormat

@Composable
fun FavoriteItem(
    item: FoodModel,
    managementFavorite: ManagementFavorite,
    managmentCart: ManagmentCart
) {
    val isFavorite by remember { derivedStateOf { managementFavorite.isFavorite(item) } }
    val decimalFormat = DecimalFormat("0.00")

    ConstraintLayout(
        modifier = Modifier
            .padding(vertical = 12.dp)
            .fillMaxWidth()
            .border(1.dp, colorResource(R.color.grey), shape = RoundedCornerShape(10.dp))
            .clickable { /* Mở màn hình chi tiết với item */ }
    ) {
        val (pic, titleTxt, priceTxt, addToCartBtn, favoriteBtn) = createRefs()

        Image(
            painter = rememberAsyncImagePainter(
                model = item.ImagePath,
                placeholder = painterResource(R.drawable.image),
                error = painterResource(R.drawable.image)
            ),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .width(135.dp)
                .height(100.dp)
                .background(
                    colorResource(R.color.grey),
                    shape = RoundedCornerShape(10.dp)
                )
                .clip(RoundedCornerShape(10.dp))
                .constrainAs(pic) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }
        )

        Text(
            text = item.Title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .constrainAs(titleTxt) {
                    start.linkTo(pic.end)
                    top.linkTo(pic.top)
                }
                .padding(start = 12.dp, top = 12.dp)
        )

        Text(
            text = "$${decimalFormat.format(item.Price)}",
            fontSize = 16.sp,
            fontStyle = FontStyle.Italic,
            color = colorResource(R.color.darkPurple),
            modifier = Modifier
                .constrainAs(priceTxt) {
                    start.linkTo(titleTxt.start)
                    top.linkTo(titleTxt.bottom)
                }
                .padding(start = 12.dp, top = 8.dp)
        )

        FavoriteButton(
            isFavorite = isFavorite,
            modifier = Modifier.constrainAs(favoriteBtn) {
                top.linkTo(parent.top)
                end.linkTo(parent.end)
            },
            onClick = {
                if (isFavorite) {
                    managementFavorite.removeFromFavorite(item)
                } else {
                    managementFavorite.addToFavorite(item)
                }
            }
        )

        Row(
            modifier = Modifier
                .constrainAs(addToCartBtn) {
                    bottom.linkTo(parent.bottom)
                    end.linkTo(parent.end)
                }
                .padding(8.dp)
                .clickable { managmentCart.insertItem(item) },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.cart),
                contentDescription = "Add to cart",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Thêm",
                color = colorResource(R.color.orange),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun FavoriteButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    isFavorite: Boolean
) {
    val tintColor = if (isFavorite) {
        Color.Red
    } else {
        Color.Gray
    }

    Image(
        painter = painterResource(R.drawable.fav_icon),
        contentDescription = null,
        modifier = modifier
            .padding(end = 16.dp, top = 8.dp)
            .clickable { onClick() },
        colorFilter = ColorFilter.tint(tintColor)
    )
}