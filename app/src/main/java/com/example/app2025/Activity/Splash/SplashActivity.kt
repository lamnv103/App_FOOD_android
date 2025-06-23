package com.example.app2025.Activity.Splash

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app2025.Activity.Navigation.AppNavigation
import com.example.app2025.Login.AuthViewModel
import com.example.app2025.R
import com.example.app2025.ui.theme.App2025Theme

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App2025Theme {
                val authViewModel: AuthViewModel = viewModel() // <- tạo ViewModel
                AppNavigation(authViewModel = authViewModel)  // <- truyền vào
            }
        }
    }
}

@Composable
fun SplashScreen(
    onLoginClick: () -> Unit = {},
    onSignupClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Lấy thông tin về kích thước màn hình
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp
    val screenWidth = configuration.screenWidthDp
    val density = LocalDensity.current

    // Tính toán kích thước tương đối với giới hạn tối thiểu
    val logoSize = (screenWidth * 0.8f).coerceAtLeast(280f).coerceAtMost(350f).dp
    val titleFontSize = 26.sp
    val subtitleFontSize = 16.sp

    val scrollState = rememberScrollState()

    Box(modifier = modifier.fillMaxSize()) {
        // Ảnh nền
        Image(
            painter = painterResource(id = R.drawable.nen__1_),
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Khoảng cách trên cùng - điều chỉnh để logo nằm ở vị trí đẹp
            Spacer(modifier = Modifier.height((screenHeight * 0.06f).dp))

            // Logo
            Image(
                painter = painterResource(id = R.drawable.nen__2_),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(logoSize)
                    .clip(CircleShape)
            )

            // Khoảng cách giữa logo và tiêu đề
            Spacer(modifier = Modifier.height((screenHeight * 0.05f).dp))

            // Tiêu đề
            val styledText = buildAnnotatedString {
                append("CHÀO MỪNG BẠN ĐẾN VỚI")
                withStyle(style = SpanStyle(color = colorResource(id = R.color.orange))) {
                    append("\nNVL Food ")
                }
                append("\nHãy trải nghiệm các dịch vụ của chúng tôi")
            }

            Text(
                text = styledText,
                fontSize = titleFontSize,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
            )

            // Khoảng cách giữa tiêu đề và phụ đề
            Spacer(modifier = Modifier.height(24.dp))

            // Phụ đề
            Text(
                text = stringResource(R.string.splashSubtitle),
                fontSize = subtitleFontSize,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = 32.dp)
            )

            // Khoảng cách giữa phụ đề và nút
            Spacer(modifier = Modifier.weight(1f, fill = true).defaultMinSize(minHeight = 32.dp))

            // Nút bắt đầu
            GetStartedButton(
                onLoginClick = onLoginClick,
                onSignupClick = onSignupClick,
                modifier = Modifier.padding(bottom = 32.dp, top = 16.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSplashScreen() {
    SplashScreen(
        onLoginClick = {},
        onSignupClick = {}
    )
}