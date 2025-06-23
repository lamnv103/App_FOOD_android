package com.example.app2025.Activity.DetailEachFood

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import coil.compose.rememberAsyncImagePainter
import com.example.app2025.Domain.FoodModel
import com.example.app2025.R
import com.uilover.project2142.Helper.ManagementFavorite


@Composable
fun HeaderSection(
    item: FoodModel,
    numberInCart: Int,
    onBackClick: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    val context = LocalContext.current
    val managementFavorite = remember { ManagementFavorite(context) }
    val isFavorite = remember { mutableStateOf(managementFavorite.isFavorite(item)) }

    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .height(570.dp)
            .padding(bottom = 14.dp)
    ) {
    val (back, fav, mainImage, arcImage, title, detailRow, numberRow) = createRefs()

    Image(
        painter = rememberAsyncImagePainter(model = item.ImagePath),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .clip(
                RoundedCornerShape(
                    bottomStart = 30.dp, bottomEnd = 30.dp
                )
            )
            .constrainAs(mainImage){
                top.linkTo(parent.top)
                end.linkTo(parent.end)
                start.linkTo(parent.start)

            }
    )
    Image(
        painter = painterResource(R.drawable.arc_bg),
        contentDescription = null,
        colorFilter = ColorFilter.tint(colorResource(id = R.color.pinkTT)),// đổi màu tại đây
        modifier = Modifier
            .height(190.dp)
            .constrainAs(arcImage) {
                top.linkTo(mainImage.bottom, margin = (-64).dp)
                end.linkTo(parent.end)
                start.linkTo(parent.start)
            }
    )
    BackButton(onBackClick, Modifier.constrainAs(back) {
        top.linkTo(parent.top)
        start.linkTo(parent.start)
    })

    FavoriteButton(
        isFavorite = isFavorite.value,
        modifier = Modifier.constrainAs(fav) {
            top.linkTo(parent.top)
            end.linkTo(parent.end)
        },
        onClick = {
            if (isFavorite.value) {
                managementFavorite.removeFromFavorite(item)
            } else {
                managementFavorite.addToFavorite(item)
            }
            isFavorite.value = !isFavorite.value
        }
    )





    Text(
        text = item.Title,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = colorResource(R.color.darkPurple),
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .constrainAs(title){
                top.linkTo(arcImage.top,  margin = 32.dp)
                end.linkTo(parent.end)
                start.linkTo(parent.start)
            }

    )
    RowDetail(item,Modifier.constrainAs(detailRow){
        top.linkTo(title.bottom)
        start.linkTo(parent.start)
        end.linkTo(parent.end)
    })
    NumberRow(
        item=item,
        numberInCart=numberInCart,
        onIncrement=onIncrement,
        onDecrement=onDecrement,
        Modifier.constrainAs(numberRow){
            top.linkTo(detailRow.bottom)
            bottom.linkTo(parent.bottom)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }
    )
}

}

@Composable
private fun BackButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.back),
        contentDescription = null,
        modifier = modifier
            .padding(start = 16.dp, top = 48.dp)
            .clickable { onClick() }
    )
}


@Composable
fun FavoriteButton(
    modifier: Modifier = Modifier,
    onClick: (Boolean) -> Unit, // Pass current isFavorite state to onClick
    isFavorite: Boolean
) {
    val tintColor = if (isFavorite) {
        androidx.compose.ui.graphics.Color.Red
    } else {
        androidx.compose.ui.graphics.Color.Gray
    }

    Image(
        painter = painterResource(R.drawable.fav_icon),
        contentDescription = null,
        modifier = modifier
            .padding(end = 16.dp, top = 8.dp)
            .clickable { onClick(isFavorite) }, // Pass isFavorite to onClick
        colorFilter = ColorFilter.tint(tintColor)
    )
}


