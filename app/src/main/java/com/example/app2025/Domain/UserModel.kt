package com.example.app2025.Domain

data class UserModel(
    var id: String = "",
    var email: String = "",
    var image: String = "",
    var name: String = "",
    var phone: String = "",
    var birthday: String? = "",
    var address_id: String = "" // Changed back to address_id to reference AddressesModel
) {
    // Keep the class empty if no additional functionality needed
}