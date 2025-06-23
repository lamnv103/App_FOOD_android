package com.example.app2025.Login

import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.app2025.R

private const val TAG = "LoginPage"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var showVerificationDialog by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var forgotPasswordEmail by remember { mutableStateOf("") }

    // Validation states
    var emailError by remember { mutableStateOf<String?>(null) }

    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current

    // Validation function
    fun validateEmail(): Boolean {
        return if (email.isEmpty()) {
            emailError = "Email không được để trống"
            false
        } else if (!email.contains("@") || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = "Email không hợp lệ. Phải có ký tự @"
            false
        } else {
            emailError = null
            true
        }
    }

    LaunchedEffect(authState.value) {
        when (val state = authState.value) {
            is AuthState.Error -> {
                Log.d(TAG, "Authentication error: ${state.message}")
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                isLoading = false
            }
            is AuthState.Loading -> {
                Log.d(TAG, "Authentication loading")
                isLoading = true
            }
            is AuthState.EmailNotVerified -> {
                Log.d(TAG, "Email not verified")
                isLoading = false
                showVerificationDialog = true
            }
            is AuthState.VerificationEmailSent -> {
                Toast.makeText(context, "Email xác thực đã được gửi lại", Toast.LENGTH_SHORT).show()
            }
            is AuthState.PasswordResetEmailSent -> {
                isLoading = false
                showForgotPasswordDialog = false
                Toast.makeText(
                    context,
                    "Email đặt lại mật khẩu đã được gửi. Vui lòng kiểm tra hộp thư của bạn.",
                    Toast.LENGTH_LONG
                ).show()
            }
            else -> {
                isLoading = false
            }
        }
    }

    // Email verification dialog
    if (showVerificationDialog) {
        AlertDialog(
            onDismissRequest = { showVerificationDialog = false },
            title = { Text("Xác thực email") },
            text = {
                Column {
                    Text("Tài khoản của bạn chưa được xác thực. Vui lòng kiểm tra email và nhấp vào liên kết xác thực để kích hoạt tài khoản.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Nếu bạn không nhận được email, bạn có thể yêu cầu gửi lại.")
                }
            },
            confirmButton = {
                Button(
                    onClick = { showVerificationDialog = false }
                ) {
                    Text("Đã hiểu")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        authViewModel.resendVerificationEmail()
                    }
                ) {
                    Text("Gửi lại email")
                }
            }
        )
    }

    // Forgot password dialog
    if (showForgotPasswordDialog) {
        var forgotPasswordEmailError by remember { mutableStateOf<String?>(null) }

        fun validateForgotPasswordEmail(): Boolean {
            return if (forgotPasswordEmail.isEmpty()) {
                forgotPasswordEmailError = "Email không được để trống"
                false
            } else if (!forgotPasswordEmail.contains("@") || !Patterns.EMAIL_ADDRESS.matcher(forgotPasswordEmail).matches()) {
                forgotPasswordEmailError = "Email không hợp lệ. Phải có ký tự @"
                false
            } else {
                forgotPasswordEmailError = null
                true
            }
        }

        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = { Text("Quên mật khẩu") },
            text = {
                Column {
                    Text("Nhập địa chỉ email của bạn để nhận liên kết đặt lại mật khẩu.")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = forgotPasswordEmail,
                        onValueChange = {
                            forgotPasswordEmail = it
                            validateForgotPasswordEmail()
                        },
                        label = { Text("Email") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        isError = forgotPasswordEmailError != null
                    )

                    if (forgotPasswordEmailError != null) {
                        Text(
                            text = forgotPasswordEmailError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (validateForgotPasswordEmail()) {
                            authViewModel.sendPasswordResetEmail(forgotPasswordEmail)
                        }
                    },
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Text("Gửi")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showForgotPasswordDialog = false }
                ) {
                    Text("Hủy")
                }
            }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Ảnh nền
        Image(
            painter = painterResource(id = R.drawable.nen__1_),
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Overlay tối để văn bản dễ đọc hơn
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        )

        // Nội dung đăng nhập
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo hoặc icon ứng dụng
            Image(
                painter = painterResource(id = R.drawable.nen__2_),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(60.dp))
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Tiêu đề
            Text(
                text = "Đăng Nhập",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Đăng nhập để tiếp tục sử dụng ứng dụng",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Email field
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    validateEmail()
                },
                label = { Text("Email") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Email"
                    )
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorResource(id = R.color.orange),
                    unfocusedBorderColor = Color.White,
                    focusedLabelColor = colorResource(id = R.color.orange),
                    unfocusedLabelColor = Color.White,
                    cursorColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = emailError != null
            )

            // Email error message
            if (emailError != null) {
                Text(
                    text = emailError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Password field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Mật khẩu") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Password"
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = Color.White
                        )
                    }
                },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorResource(id = R.color.orange),
                    unfocusedBorderColor = Color.White,
                    focusedLabelColor = colorResource(id = R.color.orange),
                    unfocusedLabelColor = Color.White,
                    cursorColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            // Forgot password link
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                TextButton(
                    onClick = {
                        showForgotPasswordDialog = true
                        forgotPasswordEmail = email // Pre-fill with current email if available
                    }
                ) {
                    Text(
                        text = "Quên mật khẩu?",
                        color = colorResource(id = R.color.orange),
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Login button
            Button(
                onClick = {
                    Log.d(TAG, "Login button clicked")
                    if (validateEmail()) {
                        authViewModel.login(email, password)
                    }
                },
                enabled = !isLoading && email.isNotEmpty() && password.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.orange)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Đăng Nhập",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Signup link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Chưa có tài khoản? ",
                    color = Color.White
                )
                TextButton(onClick = {
                    navController.navigate("signup")
                }) {
                    Text(
                        text = "Đăng ký ngay",
                        color = colorResource(id = R.color.orange),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}