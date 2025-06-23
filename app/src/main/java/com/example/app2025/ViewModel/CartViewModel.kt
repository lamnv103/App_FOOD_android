package com.example.app2025.ViewModel

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app2025.Api.CreateOrder
import com.example.app2025.Domain.AddressesModel
import com.example.app2025.Domain.OrderItemModel
import com.example.app2025.Domain.OrderModel
import com.example.app2025.Domain.FoodModel
import com.example.app2025.Helper.TinyDB
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.uilover.project2142.Helper.ManagmentCart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import vn.zalopay.sdk.ZaloPayError
import vn.zalopay.sdk.ZaloPaySDK
import vn.zalopay.sdk.listeners.PayOrderListener

// Sealed class for payment results
sealed class PaymentResult {
    data class Success(val transactionId: String, val token: String) : PaymentResult()
    object Canceled : PaymentResult()
    data class Error(val error: ZaloPayError) : PaymentResult()
}

// Data class for cart state
data class CartState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val userAddresses: List<AddressesModel> = emptyList(),
    val orderPlaced: Boolean = false,
    val zpToken: String? = null,
    val paymentResult: PaymentResult? = null
)

// Sealed class for cart events
sealed class CartEvent {
    data class PlaceOrder(val addressId: String, val paymentMethod: String = "cash") : CartEvent()
    data class AddNewAddress(val address: AddressesModel) : CartEvent()
    data class CreateZaloPayOrder(val amount: String) : CartEvent()
    data class PayWithZaloPay(val token: String, val activity: ComponentActivity) : CartEvent()
    object ResetPaymentResult : CartEvent()
}

class CartViewModel(private val context: Context) : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val addressesRef = database.getReference("Addresses")
    private val ordersRef = database.getReference("Orders")
    private val orderItemsRef = database.getReference("OrderItems")

    private val managmentCart = ManagmentCart(context)

    private val _state = MutableStateFlow(CartState())
    val state: StateFlow<CartState> = _state.asStateFlow()

    init {
        loadUserAddresses()
    }

    fun onEvent(event: CartEvent) {
        when (event) {
            is CartEvent.PlaceOrder -> placeOrder(event.addressId, event.paymentMethod)
            is CartEvent.AddNewAddress -> addNewAddress(event.address)
            is CartEvent.CreateZaloPayOrder -> createZaloPayOrder(event.amount)
            is CartEvent.PayWithZaloPay -> payWithZaloPay(event.activity, event.token)
            is CartEvent.ResetPaymentResult -> resetPaymentResult()
        }
    }

    private fun loadUserAddresses() {
        val currentUser = auth.currentUser ?: return

        addressesRef.orderByChild("user_id").equalTo(currentUser.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val addresses = snapshot.children.mapNotNull { it.getValue(AddressesModel::class.java) }
                    _state.update { it.copy(userAddresses = addresses) }
                }

                override fun onCancelled(error: DatabaseError) {
                    _state.update { it.copy(error = "Không thể tải địa chỉ: ${error.message}") }
                }
            })
    }

    private fun addNewAddress(address: AddressesModel) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                val currentUser = auth.currentUser ?: throw Exception("Người dùng chưa đăng nhập")

                val addressToAdd = if (address.id.isEmpty()) {
                    val newAddressRef = addressesRef.push()
                    address.copy(id = newAddressRef.key ?: "", user_id = currentUser.uid)
                } else {
                    address
                }

                if (addressToAdd.id.isNotEmpty()) {
                    addressesRef.child(addressToAdd.id).setValue(addressToAdd).await()
                    _state.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Đã thêm địa chỉ mới"
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Lỗi khi thêm địa chỉ: ${e.message}"
                    )
                }
            }
        }
    }

    private fun placeOrder(addressId: String, paymentMethod: String) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                val currentUser = auth.currentUser ?: throw Exception("Người dùng chưa đăng nhập")

                val cartItems = managmentCart.getListCart()
                if (cartItems.isEmpty()) throw Exception("Giỏ hàng trống")

                val totalPrice = managmentCart.getTotalFee().toString()
                val timestamp = System.currentTimeMillis()
                val orderId = "$timestamp-${currentUser.uid}"

                val order = OrderModel(
                    id = orderId,
                    user_id = currentUser.uid,
                    address_id = addressId,
                    total_price = totalPrice,
                    status = "processing",
                    payment_id = paymentMethod
                )

                ordersRef.child(orderId).setValue(order).await()

                for (cartItem in cartItems) {
                    val orderItemId = orderItemsRef.push().key ?: continue
                    val orderItem = OrderItemModel(
                        id = orderItemId,
                        order_id = orderId,
                        food_id = cartItem.Id.toString(),
                        price = cartItem.Price.toString(),
                        quality = cartItem.numberInCart.toString()
                    )
                    orderItemsRef.child(orderItemId).setValue(orderItem).await()
                }

                // Clear cart
                TinyDB(context).putListObject("CartList", arrayListOf<FoodModel>())

                _state.update {
                    it.copy(
                        isLoading = false,
                        orderPlaced = true,
                        successMessage = "Đặt hàng thành công!"
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Lỗi khi đặt hàng: ${e.message}"
                    )
                }
            }
        }
    }
    // In CartViewModel.kt, update the createZaloPayOrder function
    private fun createZaloPayOrder(amount: String) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }

                // Log the amount
                android.util.Log.d("ZaloPay", "Creating order with amount: $amount")

                // Use withContext to move network operation off the main thread
                val data = withContext(Dispatchers.IO) {
                    CreateOrder().createOrder(amount)
                }

                // Log the response
                android.util.Log.d("ZaloPay", "ZaloPay response: $data")

                if (data.getString("return_code") == "1") {
                    val token = data.getString("zp_trans_token")
                    android.util.Log.d("ZaloPay", "Got token: $token")

                    _state.update {
                        it.copy(
                            isLoading = false,
                            zpToken = token
                        )
                    }
                } else {
                    val errorMsg = "ZaloPay error: ${data.optString("return_message", "Unknown error")}"
                    android.util.Log.e("ZaloPay", errorMsg)

                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = errorMsg
                        )
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ZaloPay", "Error creating order", e)

                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Lỗi khi tạo đơn hàng ZaloPay: ${e.message}"
                    )
                }
            }
        }
    }
    // Trong CartViewModel.kt
    private fun payWithZaloPay(activity: ComponentActivity, token: String) {
        android.util.Log.d("ZaloPay", "Paying with token: $token")

        // Sử dụng callback URL đúng với scheme và host trong AndroidManifest
        ZaloPaySDK.getInstance().payOrder(activity, token, "demozpdk://app", object : PayOrderListener {
            override fun onPaymentSucceeded(transactionId: String, transToken: String, appTransID: String) {
                android.util.Log.d("ZaloPay", "Payment succeeded: $transactionId")

                viewModelScope.launch {
                    _state.update {
                        it.copy(
                            paymentResult = PaymentResult.Success(transactionId, transToken),
                            zpToken = null
                        )
                    }
                }
            }

            override fun onPaymentCanceled(zpTransToken: String, appTransID: String) {
                android.util.Log.d("ZaloPay", "Payment canceled")

                viewModelScope.launch {
                    _state.update { it.copy(paymentResult = PaymentResult.Canceled, zpToken = null) }
                }
            }

            override fun onPaymentError(zaloPayError: ZaloPayError, zpTransToken: String, appTransID: String) {
                android.util.Log.e("ZaloPay", "Payment error: ${zaloPayError.toString()}")

                viewModelScope.launch {
                    _state.update { it.copy(paymentResult = PaymentResult.Error(zaloPayError), zpToken = null) }
                }
            }
        })
    }
    private fun resetPaymentResult() {
        _state.update { it.copy(paymentResult = null, zpToken = null) }
    }
}