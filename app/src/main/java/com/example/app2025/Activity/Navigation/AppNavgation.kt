package com.example.app2025.Activity.Navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.app2025.Activity.MainActivity
import com.example.app2025.Activity.MainScreen
import com.example.app2025.Activity.Splash.SplashScreen
import com.example.app2025.Login.AuthState
import com.example.app2025.Login.AuthViewModel
import com.example.app2025.Login.LoginPage
import com.example.app2025.Login.SignupPage

private const val TAG = "AppNavigation"

@Composable
fun AppNavigation(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val authState = authViewModel.authState.observeAsState()

    // Kiểm tra trạng thái xác thực khi khởi động
    LaunchedEffect(Unit) {
        Log.d(TAG, "Checking auth status on startup")
        authViewModel.checkAuthStatus()
    }

    // Theo dõi thay đổi trạng thái xác thực để điều hướng
    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Authenticated -> {
                Log.d(TAG, "User authenticated, navigating to home")
                navController.navigate("home") {
                    popUpTo("splash") { inclusive = true }
                }
            }
            is AuthState.Unauthenticated -> {
                // Nếu người dùng đã đăng xuất và đang ở màn hình home, điều hướng về login
                if (navController.currentDestination?.route == "home") {
                    Log.d(TAG, "User logged out, navigating to login")
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            }
            is AuthState.EmailNotVerified -> {
                // Nếu email chưa được xác thực và đang ở màn hình home, điều hướng về login
                if (navController.currentDestination?.route == "home") {
                    Log.d(TAG, "Email not verified, navigating to login")
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            }
            is AuthState.RegistrationSuccess -> {
                // Sau khi đăng ký thành công, điều hướng về trang đăng nhập
                if (navController.currentDestination?.route == "signup") {
                    Log.d(TAG, "Registration successful, navigating to login")
                    navController.navigate("login") {
                        popUpTo("signup") { inclusive = true }
                    }
                }
            }
            else -> {}
        }
    }

    NavigationGraph(navController = navController, authViewModel = authViewModel)
}

@Composable
fun NavigationGraph(navController: NavHostController, authViewModel: AuthViewModel) {
    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(
                onLoginClick = {
                    Log.d(TAG, "Navigating to login from splash")
                    navController.navigate("login")
                },
                onSignupClick = {
                    Log.d(TAG, "Navigating to signup from splash")
                    navController.navigate("signup")
                }
            )
        }

        composable("login") {
            LoginPage(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable("signup") {
            SignupPage(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable("home") {
            MainScreen(authViewModel = authViewModel)
        }
    }
}
