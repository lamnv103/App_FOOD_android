package com.example.app2025.Activity.DetailEachFood

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app2025.R

@Composable
fun DescriptionSection(description: String) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Mô tả: ",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.darkPurple),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = colorResource(R.color.light_orange)
            )
        ) {
            Text(
                text = description,
                fontSize = 16.sp,
                color = colorResource(R.color.darkPurple),
                lineHeight = 24.sp,
                textAlign = TextAlign.Justify,
                modifier = Modifier
                    .padding(16.dp)
            )
        }
    }
}
