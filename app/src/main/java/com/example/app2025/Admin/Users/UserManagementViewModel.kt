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
import java.util.*
import java.util.concurrent.TimeUnit

class UserManagementViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(UserManagementUiState())
    val uiState: StateFlow<UserManagementUiState> = _uiState.asStateFlow()

    private val database = FirebaseDatabase.getInstance()
    private val usersRef = database.getReference("Users")

    fun loadUsers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            usersRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val users = mutableListOf<UserModel>()
                    var activeCount = 0
                    var newCount = 0

                    // Calculate 30 days ago for "new user" status
                    val thirtyDaysAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30)

                    for (userSnapshot in snapshot.children) {
                        val user = userSnapshot.getValue(UserModel::class.java)
                        if (user != null) {
                            users.add(user)

                            // Count active users (those who have logged in recently)
                            val lastLoginTimestamp = userSnapshot.child("last_login").getValue(Long::class.java) ?: 0
                            if (lastLoginTimestamp > 0) {
                                activeCount++

                                // Count new users (registered in the last 30 days)
                                val createdAt = userSnapshot.child("created_at").getValue(Long::class.java) ?: 0
                                if (createdAt > thirtyDaysAgo) {
                                    newCount++
                                }
                            }
                        }
                    }

                    // Sort users by name
                    users.sortBy { it.name }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            users = users,
                            activeUsers = activeCount,
                            newUsers = newCount
                        )
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message
                        )
                    }
                    Log.e("UserViewModel", "Database error: ${error.message}")
                }
            })
        }
    }
}

data class UserManagementUiState(
    val isLoading: Boolean = false,
    val users: List<UserModel> = emptyList(),
    val activeUsers: Int = 0,
    val newUsers: Int = 0,
    val error: String? = null
)

data class UserModel(
    val id: String = "",
    val email: String = "",
    val image: String = "",
    val name: String = "",
    val phone: String = "",
    val birthday: String? = "",
    val address_id: String = ""
)
