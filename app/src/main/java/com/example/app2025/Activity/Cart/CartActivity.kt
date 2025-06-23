package com.example.app2025.Activity.Cart

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app2025.Activity.BaseActivity
import com.example.app2025.Activity.Order.OrderHistoryActivity
import com.example.app2025.Domain.AddressesModel
import com.example.app2025.R
import com.example.app2025.ViewModel.CartEvent
import com.example.app2025.ViewModel.CartState
import com.example.app2025.ViewModel.CartViewModel
import com.example.app2025.ViewModel.PaymentResult
import com.uilover.project2142.Helper.ManagmentCart
import vn.zalopay.sdk.Environment
import vn.zalopay.sdk.ZaloPaySDK
import java.util.ArrayList
import kotlin.math.roundToInt

// Thêm Factory cho CartViewModel để truyền context
class CartViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CartViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CartViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// Trong CartActivity.kt
class CartActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Khởi tạo ZaloPay SDK
        ZaloPaySDK.init(2553, Environment.SANDBOX)

        setContent {
            // Initialize the CartViewModel with context
            val factory = CartViewModelFactory(this)
            val cartViewModel: CartViewModel = viewModel(factory = factory)
            val cartState by cartViewModel.state.collectAsState()

            CartScreen(
                managmentCart = ManagmentCart(this),
                onBackClick = { finish() },
                state = cartState,
                onEvent = cartViewModel::onEvent,
                onOrderSuccess = {
                    // Navigate to order history after successful order
                    startActivity(Intent(this, OrderHistoryActivity::class.java))
                    finish()
                }
            )
        }
    }

    // Xử lý callback từ ZaloPay
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        ZaloPaySDK.getInstance().onResult(intent)
    }
}

@Composable
fun CartScreen(
    managmentCart: ManagmentCart = ManagmentCart(LocalContext.current),
    onBackClick: () -> Unit,
    state: CartState,
    onEvent: (CartEvent) -> Unit,
    onOrderSuccess: () -> Unit
){
    val context = LocalContext.current
    var selectedAddressId by remember { mutableStateOf("") }
    // Add the selectedPaymentMethod state variable
    var selectedPaymentMethod by remember { mutableStateOf("Cash") }
    val cartItem = remember { mutableStateOf(managmentCart.getListCart()) }
    val tax = remember { mutableStateOf(0.0) }
    calculatorCart(managmentCart, tax)

    // Tính toán tổng số tiền đầy đủ
    val itemTotal = managmentCart.getTotalFee()
    val delivery = 10.0
    val fullTotal = itemTotal + tax.value + delivery

    // Show toast messages for success or errors
    LaunchedEffect(state.successMessage) {
        state.successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    // Handle order placed state
    LaunchedEffect(state.orderPlaced) {
        if (state.orderPlaced) {
            Toast.makeText(context, "Đặt hàng thành công!", Toast.LENGTH_LONG).show()
            onOrderSuccess()
        }
    }

    // If there's only one address and none selected, select it automatically
    LaunchedEffect(state.userAddresses) {
        if (state.userAddresses.size == 1 && selectedAddressId.isEmpty()) {
            selectedAddressId = state.userAddresses[0].id
        }
    }

    // Refresh cart items when needed
    LaunchedEffect(state.orderPlaced) {
        if (state.orderPlaced) {
            cartItem.value = ArrayList(managmentCart.getListCart())
        }
    }
    // THÊM MỚI: Theo dõi token ZaloPay và tự động mở ZaloPay khi có token
    LaunchedEffect(state.zpToken) {
        state.zpToken?.let { token ->
            android.util.Log.d("ZaloPay", "Đã nhận token trong CartScreen, đang mở ZaloPay: $token")
            // Khi nhận được token, ngay lập tức gọi hàm thanh toán với ZaloPay
            onEvent(CartEvent.PayWithZaloPay(token, context as androidx.activity.ComponentActivity))
        }
    }

    // THÊM MỚI: Theo dõi kết quả thanh toán
    LaunchedEffect(state.paymentResult) {
        state.paymentResult?.let { result ->
            when (result) {
                is PaymentResult.Success -> {
                    Toast.makeText(context, "Thanh toán thành công!", Toast.LENGTH_SHORT).show()
                    // Hoàn tất đơn hàng sau khi thanh toán thành công
                    onEvent(CartEvent.PlaceOrder(selectedAddressId, "ZaloPay"))
                }
                is PaymentResult.Canceled -> {
                    Toast.makeText(context, "Đã hủy thanh toán", Toast.LENGTH_SHORT).show()
                    onEvent(CartEvent.ResetPaymentResult)
                }
                is PaymentResult.Error -> {
                    Toast.makeText(context, "Lỗi thanh toán: ${result.error}", Toast.LENGTH_SHORT).show()
                    onEvent(CartEvent.ResetPaymentResult)
                }
            }
        }
    }


    LazyColumn(modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
        item {
            ConstraintLayout(modifier = Modifier.padding(top = 36.dp)) {
                val (backBtn, cartTxt) = createRefs()
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .constrainAs(cartTxt) { centerTo(parent) },
                    text = "Giỏ Hàng ",
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 25.sp
                )
                Image(painter = painterResource(R.drawable.back_grey),
                    contentDescription = null,
                    modifier = Modifier
                        .constrainAs(backBtn) {
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            start.linkTo(parent.start)
                        }
                        .clickable { onBackClick() }
                )
            }
        }
        if(cartItem.value.isEmpty()){
            item {
                Text (
                    text = "Giỏ hàng trống",
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }else {
            items(cartItem.value) {item->
                CartItem(
                    cartItems = cartItem.value,
                    item = item,
                    managmentCart = managmentCart,
                    onItemChange = {
                        calculatorCart(managmentCart, tax)
                        cartItem.value = ArrayList(managmentCart.getListCart())
                    }
                )
            }
            item {
                Text(
                    text = "TÓM TẮT ĐƠN HÀNG",
                    color = colorResource(R.color.darkPurple),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
            item{
                CartSummary(
                    itemTotal = managmentCart.getTotalFee(),
                    tax = tax.value,
                    delivery = 10.0
                )
            }

            item {
                Text(
                    text = "Thông tin",
                    color = colorResource(R.color.darkPurple),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            item {
                DeliveryInfoBox(
                    addresses = state.userAddresses,
                    selectedAddressId = selectedAddressId,
                    selectedPaymentMethod = selectedPaymentMethod,
                    onAddressSelected = { addressId -> selectedAddressId = addressId },
                    onPaymentMethodSelected = { method -> selectedPaymentMethod = method },
                    onPlaceOrder = {
                        if (selectedAddressId.isNotEmpty()) {
                            if (selectedPaymentMethod == "ZaloPay") {
                                // Chuyển fullTotal thành số nguyên bằng cách nhân với 100
                                val amount = (fullTotal * 1000).toLong().toString()
                                onEvent(CartEvent.CreateZaloPayOrder(amount))
                            } else {
                                onEvent(CartEvent.PlaceOrder(selectedAddressId, selectedPaymentMethod))
                            }
                        } else {
                            Toast.makeText(context, "Vui lòng chọn địa chỉ giao hàng", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onAddNewAddress = { newAddress -> onEvent(CartEvent.AddNewAddress(newAddress)) }
                )
            }
        }
    }
}

fun calculatorCart(managmentCart: ManagmentCart, tax: MutableState<Double>) {
    val percentTax = 0.02 // 2% thuế
    val itemTotal = managmentCart.getTotalFee()
    tax.value = (itemTotal * percentTax * 100.0).roundToInt() / 100.0
}
