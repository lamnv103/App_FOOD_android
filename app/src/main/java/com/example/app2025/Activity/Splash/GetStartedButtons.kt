package com.example.app2025.Activity.Splash

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app2025.R

@Composable
fun GetStartedButton(
    onLoginClick: () -> Unit,
    onSignupClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Button(
            onClick = onSignupClick,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.Transparent,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(50.dp),
            modifier = modifier
                .padding(end = 16.dp)
                .fillMaxWidth(0.35f)
                .border(1.dp, Color.White, shape = RoundedCornerShape(50.dp))
                .height(50.dp)
        ) {
            Text(text = "Đăng Kí", fontSize = 16.sp)
        }

        Button(
            onClick = onLoginClick,

            colors = ButtonDefaults.buttonColors(
                backgroundColor = colorResource(R.color.orange),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(50.dp),
            modifier = modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(text = "Đăng Nhập", fontSize = 16.sp)
        }
    }
}
