package com.example.app2025.Admin.Orders

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class OrderManagementViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(OrderManagementUiState())
    val uiState: StateFlow<OrderManagementUiState> = _uiState.asStateFlow()

    private val database = FirebaseDatabase.getInstance()
    private val ordersRef = database.getReference("Orders")

    fun loadOrders() {
        _uiState.update { it.copy(isLoading = true, error = null) }

        ordersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val orders = snapshot.children.mapNotNull {
                    it.getValue(OrderModel::class.java)
                }.sortedByDescending { it.timestamp }

                _uiState.update {
                    it.copy(isLoading = false, orders = orders)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                _uiState.update {
                    it.copy(isLoading = false, error = error.message)
                }
                Log.e("OrderManagementViewModel", "Database error: ${error.message}")
            }
        })
    }

    fun updateOrderStatus(orderId: String, newStatus: String) {
        ordersRef.child(orderId)
            .child("status")
            .setValue(newStatus)
            .addOnSuccessListener {
                Log.d("OrderManagementViewModel", "Status updated successfully")
            }
            .addOnFailureListener { e ->
                Log.e("OrderManagementViewModel", "Error updating status: ${e.message}")
            }
    }
}

data class OrderManagementUiState(
    val isLoading: Boolean = false,
    val orders: List<OrderModel> = emptyList(),
    val error: String? = null
)

data class OrderModel(
    val id: String = "",
    val user_id: String = "",
    val user_name: String = "",
    val address_id: String = "",
    val delivery_address: String = "",
    val payment_id: String = "",
    val status: String = "pending",
    val total_price: String = "",
    val timestamp: Long = 0L,   // ✅ Changed from Long? to Long
    val items_count: Int = 0
)
