package com.example.app2025.ViewModel

import android.content.Context
import android.net.Uri
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app2025.Admin.AddFood.CloudinaryUploader
import com.example.app2025.Domain.AddressesModel
import com.example.app2025.Domain.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val usersRef = database.getReference("Users")
    private val addressesRef = database.getReference("Addresses")

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        loadUserProfile()
    }

    fun onEvent(event: ProfileEvent) {
        when (event) {
            is ProfileEvent.SaveProfile -> {
                saveUserProfile(event.context)
            }
            is ProfileEvent.ToggleEditMode -> {
                _state.update {
                    it.copy(
                        isEditMode = !it.isEditMode,
                        // Reset form fields if canceling edit
                        nameInput = if (!it.isEditMode) it.user?.name ?: "" else it.nameInput,
                        phoneInput = if (!it.isEditMode) it.user?.phone ?: "" else it.phoneInput,
                        birthdayInput = if (!it.isEditMode) it.user?.birthday
                            ?: "" else it.birthdayInput,
                        selectedAddressId = if (!it.isEditMode) it.user?.address_id
                            ?: "" else it.selectedAddressId,
                        // Clear success/error messages when toggling edit mode
                        successMessage = null,
                        error = null
                    )
                }
            }

            is ProfileEvent.UpdateName -> {
                _state.update { it.copy(nameInput = event.name) }
            }

            is ProfileEvent.UpdatePhone -> {
                _state.update { it.copy(phoneInput = event.phone) }
            }

            is ProfileEvent.UpdateBirthday -> {
                _state.update { it.copy(birthdayInput = event.birthday) }
            }

            is ProfileEvent.SelectAddress -> {
                _state.update { it.copy(selectedAddressId = event.addressId) }
            }

            is ProfileEvent.SelectImage -> {
                _state.update { it.copy(selectedImageUri = event.uri) }
            }

            is ProfileEvent.SaveProfile -> {
                saveUserProfile(event.context) // Gọi saveUserProfile với context
            }

            is ProfileEvent.ShowDatePicker -> {
                _state.update { it.copy(showDatePicker = true) }
            }

            is ProfileEvent.HideDatePicker -> {
                _state.update { it.copy(showDatePicker = false) }
            }

            is ProfileEvent.ShowAddressSelector -> {
                _state.update { it.copy(showAddressSelector = true) }
            }

            is ProfileEvent.HideAddressSelector -> {
                _state.update { it.copy(showAddressSelector = false) }
            }

            is ProfileEvent.ClearMessages -> {
                _state.update { it.copy(successMessage = null, error = null) }
            }
        }
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, error = null) }

                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val snapshot = usersRef.child(currentUser.uid).get().await()
                    val userModel = snapshot.getValue(UserModel::class.java)

                    if (userModel != null) {
                        _state.update {
                            it.copy(
                                user = userModel,
                                nameInput = userModel.name,
                                phoneInput = userModel.phone,
                                birthdayInput = userModel.birthday ?: "",
                                selectedAddressId = userModel.address_id,
                                isLoading = false
                            )
                        }

                        // Load user addresses
                        loadUserAddresses(currentUser.uid)

                        // If user has a selected address, load it
                        if (userModel.address_id.isNotEmpty()) {
                            loadSelectedAddress(userModel.address_id)
                        }
                    } else {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = "Không thể tải thông tin người dùng"
                            )
                        }
                    }
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = "Người dùng chưa đăng nhập"
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "Đã xảy ra lỗi") }
            }
        }
    }

    private fun loadUserAddresses(userId: String) {
        addressesRef.orderByChild("user_id").equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val addresses = mutableListOf<AddressesModel>()
                    for (addressSnapshot in snapshot.children) {
                        val address = addressSnapshot.getValue(AddressesModel::class.java)
                        if (address != null) {
                            addresses.add(address)
                        }
                    }

                    _state.update { it.copy(userAddresses = addresses) }
                }

                override fun onCancelled(error: DatabaseError) {
                    _state.update { it.copy(error = "Không thể tải địa chỉ: ${error.message}") }
                }
            })
    }

    private fun loadSelectedAddress(addressId: String) {
        viewModelScope.launch {
            try {
                val snapshot = addressesRef.child(addressId).get().await()
                val address = snapshot.getValue(AddressesModel::class.java)

                if (address != null) {
                    _state.update { it.copy(selectedAddress = address) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Không thể tải địa chỉ đã chọn: ${e.message}") }
            }
        }
    }

    private fun saveUserProfile(context: Context) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, successMessage = null) }

            val currentUser = auth.currentUser
            if (currentUser == null) {
                _state.update { it.copy(isLoading = false, error = "Người dùng chưa đăng nhập") }
                return@launch
            }

            val currentState = _state.value

            if (currentState.nameInput.isBlank()) {
                _state.update { it.copy(isLoading = false, error = "Vui lòng nhập họ tên") }
                return@launch
            }

            if (currentState.phoneInput.isBlank()) {
                _state.update { it.copy(isLoading = false, error = "Vui lòng nhập số điện thoại") }
                return@launch
            }

            try {
                // Tạo mới hoặc cập nhật thông tin người dùng
                val updatedUser = currentState.user?.copy(
                    name = currentState.nameInput,
                    phone = currentState.phoneInput,
                    birthday = currentState.birthdayInput,
                    address_id = currentState.selectedAddressId
                ) ?: UserModel(
                    id = currentUser.uid,
                    email = currentUser.email ?: "",
                    name = currentState.nameInput,
                    phone = currentState.phoneInput,
                    birthday = currentState.birthdayInput,
                    address_id = currentState.selectedAddressId
                )

                val imageUri = currentState.selectedImageUri

                if (imageUri != null) {
                    // Upload ảnh lên Cloudinary
                    CloudinaryUploader.uploadImage(
                        context,
                        imageUri,
                        onSuccess = { imageUrl ->
                            updatedUser.image = imageUrl

                            // Gọi tiếp cập nhật Firebase
                            updateUserData(currentUser.uid, updatedUser)
                        },
                        onError = { errorMsg ->
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    error = "Lỗi tải ảnh: $errorMsg"
                                )
                            }
                        }
                    )
                } else {
                    // Không có ảnh, chỉ cập nhật thông tin
                    updateUserData(currentUser.uid, updatedUser)
                }

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Lỗi khi cập nhật hồ sơ: ${e.message}"
                    )
                }
            }
        }
    }

    private fun updateUserData(userId: String, updatedUser: UserModel) {
        viewModelScope.launch {
            try {
                val userUpdates = mutableMapOf<String, Any?>(
                    "name" to updatedUser.name,
                    "phone" to updatedUser.phone,
                    "birthday" to updatedUser.birthday,
                    "address_id" to updatedUser.address_id
                )

                updatedUser.image?.let { imageUrl ->
                    userUpdates["image"] = imageUrl
                }

                usersRef.child(userId).updateChildren(userUpdates).await()

                _state.update {
                    it.copy(
                        user = updatedUser,
                        isEditMode = false,
                        isLoading = false,
                        successMessage = "Cập nhật hồ sơ thành công",
                        error = null
                    )
                }

                if (updatedUser.address_id.isNotEmpty()) {
                    loadSelectedAddress(updatedUser.address_id)
                }

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Lỗi khi lưu dữ liệu: ${e.message}"
                    )
                }
            }
        }
    }
}
data class ProfileState(
    val user: UserModel? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val isEditMode: Boolean = false,
    val nameInput: String = "",
    val phoneInput: String = "",
    val birthdayInput: String = "",
    val selectedAddressId: String = "",
    val selectedImageUri: Uri? = null,
    val showDatePicker: Boolean = false,
    val showAddressSelector: Boolean = false,
    val userAddresses: List<AddressesModel> = emptyList(),
    val selectedAddress: AddressesModel? = null
)

sealed class ProfileEvent {
    object ToggleEditMode : ProfileEvent()
    data class UpdateName(val name: String) : ProfileEvent()
    data class UpdatePhone(val phone: String) : ProfileEvent()
    data class UpdateBirthday(val birthday: String) : ProfileEvent()
    data class SelectAddress(val addressId: String) : ProfileEvent()
    data class SelectImage(val uri: Uri) : ProfileEvent()
    object ShowDatePicker : ProfileEvent()
    object HideDatePicker : ProfileEvent()
    object ShowAddressSelector : ProfileEvent()
    object HideAddressSelector : ProfileEvent()
    object ClearMessages : ProfileEvent()
    data class SaveProfile(val context: Context) : ProfileEvent() // Định nghĩa với context
}