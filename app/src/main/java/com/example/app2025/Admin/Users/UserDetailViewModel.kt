package com.example.app2025.Admin.Users

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
import kotlinx.coroutines.tasks.await
import java.text.NumberFormat
import java.util.*

class UserDetailViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(UserDetailUiState())
    val uiState: StateFlow<UserDetailUiState> = _uiState.asStateFlow()

    private val database = FirebaseDatabase.getInstance()
    private val usersRef = database.getReference("Users")
    private val addressesRef = database.getReference("Addresses")
    private val ordersRef = database.getReference("Orders")

    fun loadUserDetails(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Load user data
                val userSnapshot = usersRef.child(userId).get().await()
                val user = userSnapshot.getValue(UserDetailModel::class.java)

                if (user != null) {
                    user.id = userId // Ensure ID is set
                    _uiState.update { it.copy(user = user) }

                    // Load address if available
                    if (user.address_id.isNotEmpty()) {
                        loadUserAddress(user.address_id)
                    }

                    // Load order statistics
                    loadOrderStatistics(userId)
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Không tìm thấy thông tin người dùng"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("UserDetailViewModel", "Error loading user details: ${e.message}", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Lỗi: ${e.message}"
                    )
                }
            }
        }
    }

    private suspend fun loadUserAddress(addressId: String) {
        try {
            val addressSnapshot = addressesRef.child(addressId).get().await()
            val address = addressSnapshot.getValue(AddressModel::class.java)

            if (address != null) {
                _uiState.update { it.copy(address = address) }
            }
        } catch (e: Exception) {
            Log.e("UserDetailViewModel", "Error loading address: ${e.message}", e)
        }
    }

    private fun loadOrderStatistics(userId: String) {
        ordersRef.orderByChild("user_id").equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var totalOrders = 0
                    var completedOrders = 0
                    var cancelledOrders = 0
                    var totalSpent = 0.0

                    for (orderSnapshot in snapshot.children) {
                        totalOrders++

                        val status = orderSnapshot.child("status").getValue(String::class.java)
                        when (status) {
                            "completed" -> completedOrders++
                            "cancelled" -> cancelledOrders++
                        }

                        if (status == "completed") {
                            val totalPrice = orderSnapshot.child("total_price").getValue(String::class.java)
                            totalPrice?.replace("đ", "")?.replace(",", "")?.trim()?.toDoubleOrNull()?.let {
                                totalSpent += it
                            }
                        }
                    }

                    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            totalOrders = totalOrders,
                            completedOrders = completedOrders,
                            cancelledOrders = cancelledOrders,
                            totalSpent = formatter.format(totalSpent)
                        )
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("UserDetailViewModel", "Error loading orders: ${error.message}")
                    _uiState.update { it.copy(isLoading = false) }
                }
            })
    }

    fun toggleUserBlockStatus() {
        viewModelScope.launch {
            val user = _uiState.value.user ?: return@launch

            try {
                val newBlockStatus = !(user.isBlocked ?: false)

                usersRef.child(user.id).child("isBlocked").setValue(newBlockStatus).await()

                // Update local state
                _uiState.update {
                    it.copy(
                        user = it.user?.copy(isBlocked = newBlockStatus),
                        successMessage = if (newBlockStatus)
                            "Đã chặn người dùng thành công"
                        else
                            "Đã bỏ chặn người dùng thành công"
                    )
                }
            } catch (e: Exception) {
                Log.e("UserDetailViewModel", "Error toggling block status: ${e.message}", e)
                _uiState.update {
                    it.copy(error = "Lỗi: ${e.message}")
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update {
            it.copy(successMessage = null, error = null)
        }
    }
}

data class UserDetailUiState(
    val isLoading: Boolean = true,
    val user: UserDetailModel? = null,
    val address: AddressModel? = null,
    val totalOrders: Int = 0,
    val completedOrders: Int = 0,
    val cancelledOrders: Int = 0,
    val totalSpent: String = "0₫",
    val successMessage: String? = null,
    val error: String? = null
)

data class UserDetailModel(
    var id: String = "",
    val email: String = "",
    val image: String = "",
    val name: String = "",
    val phone: String = "",
    val birthday: String? = "",
    val address_id: String = "",
    val isBlocked: Boolean? = false,
    val createdAt: Long = 0,
    val lastLogin: Long = 0
)

data class AddressModel(
    val id: String = "",
    val user_id: String = "",
    val name: String = "",
    val address: String = "",
    val city: String = "",
    val type: String = ""
)
