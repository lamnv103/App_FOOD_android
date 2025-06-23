package com.example.app2025.Admin.AddFood


import com.google.firebase.database.FirebaseDatabase

object FirebaseManager {

    private val database = FirebaseDatabase.getInstance()
    private val foodsRef = database.getReference("Foods")

    fun addProduct(
        product: Map<String, Any>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        foodsRef.push()
            .setValue(product)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError("Lỗi lưu dữ liệu: ${exception.message}")
            }
    }

    fun getProducts(
        onSuccess: (List<Map<String, Any>>) -> Unit,
        onError: (String) -> Unit
    ) {
        foodsRef.get()
            .addOnSuccessListener { snapshot ->
                val products = mutableListOf<Map<String, Any>>()

                snapshot.children.forEach { child ->
                    val product = child.value as? Map<String, Any>
                    if (product != null) {
                        val productWithId = product.toMutableMap()
                        productWithId["firebaseId"] = child.key ?: ""
                        products.add(productWithId)
                    }
                }

                onSuccess(products)
            }
            .addOnFailureListener { exception ->
                onError("Lỗi tải dữ liệu: ${exception.message}")
            }
    }

    fun deleteProduct(
        productId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        foodsRef.child(productId)
            .removeValue()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError("Lỗi xóa sản phẩm: ${exception.message}")
            }
    }
}
