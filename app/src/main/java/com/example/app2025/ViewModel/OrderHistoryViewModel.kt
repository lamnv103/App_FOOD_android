package com.example.app2025.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app2025.Domain.AddressesModel
import com.example.app2025.Domain.OrderItemModel
import com.example.app2025.Domain.OrderModel
import com.google.firebase.auth.FirebaseAuth
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

class OrderHistoryViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val ordersRef = database.getReference("Orders")
    private val orderItemsRef = database.getReference("OrderItems")
    private val addressesRef = database.getReference("Addresses")

    private val _state = MutableStateFlow(OrderHistoryState())
    val state: StateFlow<OrderHistoryState> = _state.asStateFlow()

    // Lưu trữ listener để có thể hủy khi không cần thiết
    private var ordersListener: ValueEventListener? = null

    init {
        loadOrderHistory()
    }

    private fun loadOrderHistory() {
        val currentUser = auth.currentUser ?: return
        _state.update { it.copy(isLoading = true, error = null) }

        // Hủy listener cũ nếu có
        ordersListener?.let {
            ordersRef.orderByChild("user_id").equalTo(currentUser.uid).removeEventListener(it)
        }

        // Tạo listener mới
        ordersListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                viewModelScope.launch {
                    try {
                        val orders = mutableListOf<OrderModel>()
                        for (orderSnapshot in snapshot.children) {
                            val order = orderSnapshot.getValue(OrderModel::class.java)
                            if (order != null) {
                                orders.add(order)
                            }
                        }

                        // Sort orders by date (newest first)
                        orders.sortByDescending { it.id } // Assuming id contains timestamp info

                        // Load details for each order
                        val ordersWithDetails = mutableListOf<OrderWithDetails>()
                        for (order in orders) {
                            val address = loadAddress(order.address_id)
                            val itemCount = getOrderItemCount(order.id)
                            val orderDate = getOrderDate(order.id)

                            ordersWithDetails.add(
                                OrderWithDetails(
                                    order = order,
                                    address = address,
                                    itemCount = itemCount,
                                    orderDate = orderDate
                                )
                            )
                        }

                        _state.update {
                            it.copy(
                                isLoading = false,
                                orders = ordersWithDetails
                            )
                        }
                    } catch (e: Exception) {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = "Lỗi khi tải lịch sử đơn hàng: ${e.message}"
                            )
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Không thể tải lịch sử đơn hàng: ${error.message}"
                    )
                }
            }
        }

        // Đăng ký listener để lắng nghe thay đổi trong thời gian thực
        ordersRef.orderByChild("user_id").equalTo(currentUser.uid).addValueEventListener(ordersListener!!)
    }

    private suspend fun loadAddress(addressId: String): AddressesModel? {
        return try {
            var address: AddressesModel? = null
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    address = snapshot.getValue(AddressesModel::class.java)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
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
            null
        }
    }

    private suspend fun getOrderItemCount(orderId: String): Int {
        return try {
            var count = 0
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    count = snapshot.childrenCount.toInt()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            }

            orderItemsRef.orderByChild("order_id").equalTo(orderId).addListenerForSingleValueEvent(listener)

            // Simple way to wait for the async operation to complete
            var attempts = 0
            while (count == 0 && attempts < 10) {
                kotlinx.coroutines.delay(100)
                attempts++
            }

            count
        } catch (e: Exception) {
            0
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
            null
        }
    }

    // Hủy listener khi ViewModel bị hủy
    override fun onCleared() {
        super.onCleared()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            ordersListener?.let {
                ordersRef.orderByChild("user_id").equalTo(currentUser.uid).removeEventListener(it)
            }
        }
    }

    data class OrderHistoryState(
        val isLoading: Boolean = false,
        val error: String? = null,
        val orders: List<OrderWithDetails> = emptyList()
    )

    data class OrderWithDetails(
        val order: OrderModel,
        val address: AddressesModel? = null,
        val itemCount: Int = 0,
        val orderDate: Date? = null
    )
}