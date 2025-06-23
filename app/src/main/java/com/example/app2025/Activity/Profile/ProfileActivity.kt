package com.example.app2025.Activity.Profile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app2025.Login.AuthViewModel
import com.example.app2025.ViewModel.ProfileViewModel
import com.example.app2025.ui.theme.App2025Theme

class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App2025Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val authViewModel: AuthViewModel = viewModel()
                    val profileViewModel: ProfileViewModel = viewModel()
                    val profileState by profileViewModel.state.collectAsState()

                    ProfileScreen(
                        state = profileState,
                        onEvent = profileViewModel::onEvent,
                        onNavigateBack = { finish() },
                        authViewModel = authViewModel
                    )
                }
            }
        }
    }
}
