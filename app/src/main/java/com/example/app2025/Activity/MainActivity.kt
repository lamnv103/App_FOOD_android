package com.example.app2025.Activity

import LocationModel
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app2025.Activity.Dashboard.*
import com.example.app2025.Activity.Map.MapActivity
import com.example.app2025.Domain.BannerModel
import com.example.app2025.Domain.CategoryModel
import com.example.app2025.Domain.FoodModel
import com.example.app2025.Login.AuthViewModel
import com.example.app2025.ViewModel.MainViewModel
import com.example.app2025.ui.theme.App2025Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            App2025Theme {
                val authViewModel: AuthViewModel = viewModel()
                MainScreen(authViewModel)
            }
        }
    }
}

@Composable
fun MainScreen(authViewModel: AuthViewModel = viewModel()) {
    val scaffoldState = rememberScaffoldState()
    val viewModel: MainViewModel = viewModel() // ✅ Sửa ở đây
    val context = LocalContext.current

    val banners = remember { mutableStateListOf<BannerModel>() }
    val categories = remember { mutableStateListOf<CategoryModel>() }
    val foods = remember { mutableStateListOf<FoodModel>() }

    var showBannerLoading by remember { mutableStateOf(true) }
    var showCategoryLoading by remember { mutableStateOf(true) }
    var showFoodLoading by remember { mutableStateOf(true) }

    // Load dữ liệu
    LaunchedEffect(Unit) {
        viewModel.loadBanner().observeForever {
            it?.let {
                banners.clear()
                banners.addAll(it)
            }
            showBannerLoading = false
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadCategory().observeForever {
            it?.let {
                categories.clear()
                categories.addAll(it)
            }
            showCategoryLoading = false
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadFood().observeForever {
            it?.let {
                foods.clear()
                foods.addAll(it)
            }
            showFoodLoading = false
        }
    }

    // Giao diện chính
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopBar(
                onLogout = {
                    authViewModel.signout()
                }
            )
        },
        bottomBar = { MyBottomBar() },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val intent = Intent(context, MapActivity::class.java)
                    // Pass a default or selected location
                    val defaultLocation = LocationModel("Cửa Hàng Văn Lâm", 21.0285, 105.8542)
                    intent.putExtra("item", defaultLocation)
                    context.startActivity(intent)
                },
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Map,
                    contentDescription = "Open Map"
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            contentPadding = paddingValues,
            modifier = Modifier.fillMaxSize()
        ) {
            item { Search() }
            item { Banner(banners, showBannerLoading) }
            item { CategorySection(categories, showCategoryLoading) }
            item {
                FeaturedProductSection(
                    foods = foods,
                    isLoading = showFoodLoading
                )
            }
        }
    }
}
