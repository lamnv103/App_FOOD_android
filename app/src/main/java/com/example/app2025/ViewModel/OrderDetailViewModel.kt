package com.example.app2025.ViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app2025.Domain.AddressesModel
import com.example.app2025.Domain.OrderItemModel
import com.example.app2025.Domain.OrderModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.*

class OrderDetailViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance()
    private val ordersRef = database.getReference("Orders")
    private val orderItemsRef = database.getReference("OrderItems")
    private val addressesRef = database.getReference("Addresses")
    private val foodsRef = database.getReference("Foods")

    private val _state = MutableStateFlow(OrderDetailState())
    val state: StateFlow<OrderDetailState> = _state.asStateFlow()

    // Lưu trữ các listener để có thể hủy khi không cần thiết
    private var orderListener: ValueEventListener? = null
    private var orderItemsListener: ValueEventListener? = null
    fun loadOrderDetails(orderId: String) {
        _state.update { it.copy(isLoading = true, error = null) }
        Log.d("OrderDetailViewModel", "Loading order details for ID: $orderId")

        // Hủy listener cũ nếu có
        orderListener?.let { ordersRef.child(orderId).removeEventListener(it) }

        // Tạo listener mới để lắng nghe thay đổi trong thời gian thực
        orderListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                viewModelScope.launch {
                    try {
                        val order = snapshot.getValue(OrderModel::class.java)
                        Log.d("OrderDetailViewModel", "Order data received: ${order?.status}")

                        if (order != null) {
                            // Load address
                            val address = loadAddress(order.address_id)

                            // Load order items
                            loadOrderItems(order.id)

                            // Get order date
                            val orderDate = getOrderDate(order.id)

                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    order = order,
                                    address = address,
                                    orderDate = orderDate
                                )
                            }
                        } else {
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    error = "Không tìm thấy đơn hàng"
                                )
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("OrderDetailViewModel", "Error loading order details: ${e.message}", e)
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = "Lỗi khi tải chi tiết đơn hàng: ${e.message}"
                            )
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("OrderDetailViewModel", "Database error: ${error.message}")
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Không thể tải chi tiết đơn hàng: ${error.message}"
                    )
                }
            }
        }

        // Đăng ký listener để lắng nghe thay đổi trong thời gian thực
        ordersRef.child(orderId).addValueEventListener(orderListener!!)
    }

    private suspend fun loadAddress(addressId: String): AddressesModel? {
        return try {
            var address: AddressesModel? = null
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    address = snapshot.getValue(AddressesModel::class.java)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("OrderDetailViewModel", "Error loading address: ${error.message}")
                }
            }

            addressesRef.child(addressId).addListenerForSingleValueEvent(listener)

            // Simple way to wait for the async operation to complete
            var attempts = 0
            while (address == null && attempts < 10) {
                kotlinx.coroutines.delay(100)
                attempts++
            }

            address
        } catch (e: Exception) {
            Log.e("OrderDetailViewModel", "Exception loading address: ${e.message}", e)
            null
        }
    }

    private fun loadOrderItems(orderId: String) {
        // Hủy listener cũ nếu có
        orderItemsListener?.let {
            orderItemsRef.orderByChild("order_id").equalTo(orderId).removeEventListener(it)
        }

        // Tạo listener mới
        orderItemsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                viewModelScope.launch {
                    try {
                        val items = mutableListOf<OrderItemModel>()
                        for (itemSnapshot in snapshot.children) {
                            val item = itemSnapshot.getValue(OrderItemModel::class.java)
                            if (item != null) {
                                items.add(item)
                            }
                        }

                        // Load food details for each item
                        val itemsWithDetails = mutableListOf<OrderItemModel>()
                        for (item in items) {
                            val foodDetails = loadFoodDetails(item.food_id)
                            val itemWithDetails = item.copy(
                                product_name = foodDetails?.first ?: "Sản phẩm không xác định",
                                product_image = foodDetails?.second ?: ""
                            )
                            itemsWithDetails.add(itemWithDetails)
                        }

                        // Calculate subtotal
                        val subtotal = itemsWithDetails.sumOf {
                            it.price.toDoubleOrNull()?.times(it.quality.toIntOrNull() ?: 1) ?: 0.0
                        }

                        // Get total price from order
                        val totalPrice = _state.value.order?.total_price?.toDoubleOrNull() ?: 0.0

                        // Calculate shipping fee (total - subtotal)
                        val shippingFee = totalPrice - subtotal

                        _state.update {
                            it.copy(
                                orderItems = itemsWithDetails,
                                subtotal = subtotal.toString(),
                                shippingFee = shippingFee.toString()
                            )
                        }
                    } catch (e: Exception) {
                        Log.e("OrderDetailViewModel", "Error loading order items: ${e.message}", e)
                        _state.update {
                            it.copy(
                                error = "Lỗi khi tải chi tiết sản phẩm: ${e.message}"
                            )
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("OrderDetailViewModel", "Database error loading items: ${error.message}")
                _state.update {
                    it.copy(
                        error = "Lỗi khi tải chi tiết sản phẩm: ${error.message}"
                    )
                }
            }
        }

        // Đăng ký listener để lắng nghe thay đổi trong thời gian thực
        orderItemsRef.orderByChild("order_id").equalTo(orderId).addValueEventListener(orderItemsListener!!)
    }

    private suspend fun loadFoodDetails(foodId: String): Pair<String, String>? {
        return try {
            var name: String? = null
            var image: String? = null

            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    name = snapshot.child("Title").getValue(String::class.java)
                    image = snapshot.child("ImagePath").getValue(String::class.java)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("OrderDetailViewModel", "Error loading food details: ${error.message}")
                }
            }

            foodsRef.child(foodId).addListenerForSingleValueEvent(listener)

            // Wait for the async operation to complete
            var attempts = 0
            while (name == null && attempts < 10) {
                kotlinx.coroutines.delay(100)
                attempts++
            }

            if (name != null) {
                Pair(name!!, image ?: "")
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("OrderDetailViewModel", "Exception loading food details: ${e.message}", e)
            null
        }
    }

    private fun getOrderDate(orderId: String): Date? {
        // Try to extract date from order ID if it contains timestamp
        return try {
            // Assuming order ID format includes timestamp at the beginning
            val timestamp = orderId.split("-").firstOrNull()?.toLongOrNull()
            if (timestamp != null) {
                Date(timestamp)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("OrderDetailViewModel", "Error parsing order date: ${e.message}", e)
            null
        }
    }

    fun cancelOrder(orderId: String) {
        _state.update { it.copy(isCancelling = true) }

        // Log trước khi hủy đơn hàng
        Log.d("OrderDetailViewModel", "Đang hủy đơn hàng: $orderId")

        // Cập nhật trạng thái đơn hàng thành "cancelled" trong Firebase
        ordersRef.child(orderId)
            .child("status")
            .setValue("cancelled")
            .addOnSuccessListener {
                Log.d("OrderDetailViewModel", "Đơn hàng đã được hủy thành công: $orderId")

                // Thêm thông tin thời gian hủy đơn
                val cancelTime = System.currentTimeMillis()
                ordersRef.child(orderId).child("cancelled_at").setValue(cancelTime)

                // Thêm thông tin người hủy (khách hàng)
                ordersRef.child(orderId).child("cancelled_by").setValue("customer")

                _state.update {
                    it.copy(
                        isCancelling = false,
                        cancelSuccess = true
                        // Không cần cập nhật order.status ở đây vì listener sẽ tự động cập nhật
                    )
                }
            }
            .addOnFailureListener { e ->
                Log.e("OrderDetailViewModel", "Lỗi khi hủy đơn hàng: ${e.message}", e)
                _state.update {
                    it.copy(
                        isCancelling = false,
                        error = "Không thể hủy đơn hàng: ${e.message}"
                    )
                }
            }
    }

    // Reset cancel success state
    fun resetCancelSuccess() {
        _state.update { it.copy(cancelSuccess = false) }
    }

    // Hủy các listener khi ViewModel bị hủy
    override fun onCleared() {
        super.onCleared()
        val currentOrder = _state.value.order
        if (currentOrder != null) {
            Log.d("OrderDetailViewModel", "Cleaning up listeners for order: ${currentOrder.id}")
            orderListener?.let { ordersRef.child(currentOrder.id).removeEventListener(it) }
            orderItemsListener?.let {
                orderItemsRef.orderByChild("order_id").equalTo(currentOrder.id).removeEventListener(it)
            }
        }
    }

    data class OrderDetailState(
        val isLoading: Boolean = false,
        val isCancelling: Boolean = false,
        val cancelSuccess: Boolean = false,
        val error: String? = null,
        val order: OrderModel? = null,
        val address: AddressesModel? = null,
        val orderItems: List<OrderItemModel> = emptyList(),
        val subtotal: String = "0",
        val shippingFee: String = "0",
        val orderDate: Date? = null
    )
}
