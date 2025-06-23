package com.example.app2025.Activity.Cart

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app2025.Activity.BaseActivity
import com.example.app2025.Activity.Cart.CartViewModelFactory
import com.example.app2025.DeliveryScreen
import com.example.app2025.ViewModel.CartEvent
import com.example.app2025.ViewModel.CartViewModel
import com.example.app2025.ui.theme.App2025Theme
import vn.zalopay.sdk.Environment
import vn.zalopay.sdk.ZaloPaySDK

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ZaloPaySDK.init(2553, Environment.SANDBOX)
        setContent {
            App2025Theme {
                val factory = CartViewModelFactory(this)
                val cartViewModel: CartViewModel = viewModel(factory = factory)
                DeliveryScreen(
                    viewModel = cartViewModel,
                    onAddAddress = { address -> cartViewModel.onEvent(CartEvent.AddNewAddress(address)) },
                    onOrderCreated = { amount -> cartViewModel.onEvent(CartEvent.CreateZaloPayOrder(amount)) },
                    onPay = { token -> cartViewModel.onEvent(CartEvent.PayWithZaloPay(token, this)) }
                )
            }
        }
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        ZaloPaySDK.getInstance().onResult(intent)
    }
}
