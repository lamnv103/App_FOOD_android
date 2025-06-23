package com.example.app2025.Login

import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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

private const val TAG = "SignupPage"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    // Validation states
    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var showVerificationDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current

    // Validation functions
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

    fun validatePhone(): Boolean {
        return if (phone.isEmpty()) {
            // Phone is optional, so empty is fine
            phoneError = null
            true
        } else if (phone.length != 10 || !phone.all { it.isDigit() }) {
            phoneError = "Số điện thoại phải có đúng 10 chữ số"
            false
        } else {
            phoneError = null
            true
        }
    }

    fun validatePassword(): Boolean {
        return if (password.isEmpty()) {
            passwordError = "Mật khẩu không được để trống"
            false
        } else if (password.length < 6) {
            passwordError = "Mật khẩu phải có ít nhất 6 ký tự"
            false
        } else if (password != confirmPassword) {
            passwordError = "Mật khẩu không khớp"
            false
        } else {
            passwordError = null
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
            is AuthState.RegistrationSuccess -> {
                Log.d(TAG, "Registration successful, verification email sent")
                isLoading = false
                showVerificationDialog = true
            }
            is AuthState.VerificationEmailSent -> {
                Toast.makeText(context, "Email xác thực đã được gửi lại", Toast.LENGTH_SHORT).show()
            }
            else -> {
                isLoading = false
            }
        }
    }

    // Email verification dialog
    if (showVerificationDialog) {
        AlertDialog(
            onDismissRequest = {
                showVerificationDialog = false
                navController.navigate("login") {
                    popUpTo("signup") { inclusive = true }
                }
            },
            title = { Text("Xác thực email") },
            text = {
                Column {
                    Text("Chúng tôi đã gửi một email xác thực đến $email. Vui lòng kiểm tra hộp thư và nhấp vào liên kết để kích hoạt tài khoản của bạn.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Sau khi xác thực, bạn có thể đăng nhập vào ứng dụng.")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showVerificationDialog = false
                        navController.navigate("login") {
                            popUpTo("signup") { inclusive = true }
                        }
                    }
                ) {
                    Text("Đến trang đăng nhập")
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

        // Nội dung đăng ký
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo hoặc icon ứng dụng
            Image(
                painter = painterResource(id = R.drawable.nen__2_),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(50.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tiêu đề
            Text(
                text = "Tạo Tài Khoản",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Đăng ký để trải nghiệm dịch vụ của chúng tôi",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Name field
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Họ và tên") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Name"
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
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Phone field
            OutlinedTextField(
                value = phone,
                onValueChange = {
                    phone = it
                    validatePhone()
                },
                label = { Text("Số điện thoại") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Phone"
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = phoneError != null
            )

            // Phone error message
            if (phoneError != null) {
                Text(
                    text = phoneError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                onValueChange = {
                    password = it
                    validatePassword()
                },
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = passwordError != null
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Password field
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    validatePassword()
                },
                label = { Text("Xác nhận mật khẩu") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Confirm Password"
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password",
                            tint = Color.White
                        )
                    }
                },
                singleLine = true,
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = passwordError != null
            )

            // Password error message
            if (passwordError != null) {
                Text(
                    text = passwordError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Signup button
            Button(
                onClick = {
                    Log.d(TAG, "Signup button clicked")
                    val isEmailValid = validateEmail()
                    val isPhoneValid = validatePhone()
                    val isPasswordValid = validatePassword()

                    if (isEmailValid && isPhoneValid && isPasswordValid) {
                        authViewModel.signup(email, password, name, phone)
                    }
                },
                enabled = !isLoading && email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty(),
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
                        text = "Đăng Ký",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Login link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Đã có tài khoản? ",
                    color = Color.White
                )
                TextButton(onClick = {
                    navController.navigate("login") {
                        popUpTo("signup") { inclusive = true }
                    }
                }) {
                    Text(
                        text = "Đăng nhập",
                        color = colorResource(id = R.color.orange),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}