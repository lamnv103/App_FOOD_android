package com.example.app2025.Admin.Orders

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class OrderDetailViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(OrderDetailUiState())
    val uiState: StateFlow<OrderDetailUiState> = _uiState.asStateFlow()

    private val database = FirebaseDatabase.getInstance()
    private val ordersRef = database.getReference("Orders")
    private val orderItemsRef = database.getReference("OrderItems")

    fun loadOrderDetails(orderId: String) {
        if (orderId.isEmpty()) {
            _uiState.update { it.copy(error = "Mã đơn hàng không hợp lệ") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Load order
            ordersRef.child(orderId).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val order = snapshot.getValue(OrderModel::class.java)

                    if (order != null) {
                        _uiState.update { it.copy(order = order) }

                        // Load order items
                        loadOrderItems(orderId)
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Không tìm thấy đơn hàng"
                            )
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Lỗi: ${error.message}"
                        )
                    }
                }
            })
        }
    }

    private fun loadOrderItems(orderId: String) {
        orderItemsRef.orderByChild("order_id").equalTo(orderId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val items = mutableListOf<OrderItemModel>()

                    for (itemSnapshot in snapshot.children) {
                        val item = itemSnapshot.getValue(OrderItemModel::class.java)
                        if (item != null) {
                            items.add(item)
                        }
                    }

                    // Calculate subtotal
                    val subtotal = items.sumOf { it.price.toDoubleOrNull()?.times(it.quantity) ?: 0.0 }

                    // Get total price from order
                    val totalPrice = _uiState.value.order?.total_price?.toDoubleOrNull() ?: 0.0

                    // Calculate shipping fee (total - subtotal)
                    val shippingFee = totalPrice - subtotal

                    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            orderItems = items,
                            subtotal = formatter.format(subtotal),
                            shippingFee = formatter.format(shippingFee)
                        )
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Lỗi khi tải chi tiết đơn hàng: ${error.message}"
                        )
                    }
                }
            })
    }

    fun updateOrderStatus(orderId: String, newStatus: String) {
        viewModelScope.launch {
            try {
                ordersRef.child(orderId)
                    .child("status")
                    .setValue(newStatus)
                    .addOnSuccessListener {
                        Log.d("OrderDetailViewModel", "Status updated successfully")
                    }
                    .addOnFailureListener { e ->
                        Log.e("OrderDetailViewModel", "Error updating status: ${e.message}")
                    }
            } catch (e: Exception) {
                Log.e("OrderDetailViewModel", "Error: ${e.message}")
            }
        }
    }
}

data class OrderDetailUiState(
    val isLoading: Boolean = true,
    val order: OrderModel? = null,
    val orderItems: List<OrderItemModel> = emptyList(),
    val subtotal: String = "0₫",
    val shippingFee: String = "0₫",
    val error: String? = null
)

data class OrderItemModel(
    val id: String = "",
    val order_id: String = "",
    val product_id: String = "",
    val name: String = "",
    val price: String = "0",
    val quantity: Int = 0,
    val image_url: String = ""
)