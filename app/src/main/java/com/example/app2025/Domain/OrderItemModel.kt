package com.example.app2025.Domain


data class OrderItemModel(
    var food_id: String = "",
    var id: String = "",
    var order_id: String = "",
    var price: String = "",
    var quality: String = "", // Số lượng (quantity)
    var product_name: String = "", // Thêm tên sản phẩm
    var product_image: String = "" // Thêm hình ảnh sản phẩm
)