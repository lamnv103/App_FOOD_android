package com.example.app2025.Repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.app2025.Domain.BannerModel
import com.example.app2025.Domain.CategoryModel
import com.example.app2025.Domain.FoodModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener

class MainRepository {

    private val firebaseDatabase = FirebaseDatabase.getInstance()

    fun loadCategory(): LiveData<MutableList<CategoryModel>> {
        val listData = MutableLiveData<MutableList<CategoryModel>>()
        val ref = firebaseDatabase.getReference("Category")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<CategoryModel>()
                for (childSnapshot in snapshot.children) {
                    val item = childSnapshot.getValue(CategoryModel::class.java)
                    item?.let { list.add(it) }
                }
                listData.value = list
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Database error: ${error.message}")
                // Hoặc hiển thị thông báo lỗi lên UI nếu cần
            }

        })
        return listData
    }

    fun loadFood(): LiveData<MutableList<FoodModel>> {
        val listData = MutableLiveData<MutableList<FoodModel>>()
        val ref = firebaseDatabase.getReference("Foods")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<FoodModel>()
                for (childSnapshot in snapshot.children) {
                    val item = childSnapshot.getValue(FoodModel::class.java)
                    item?.let { list.add(it) }
                }
                listData.value = list
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Database error: ${error.message}")
                // Hoặc hiển thị thông báo lỗi lên UI nếu cần
            }
        })

        return listData
    }

    fun deleteFood(foodId: Int) {
        firebaseDatabase.getReference("Food/$foodId")
            .removeValue()
            .addOnSuccessListener {
                Log.d("Firebase", "Food item $foodId deleted successfully")
            }
            .addOnFailureListener { error ->
                Log.e("Firebase", "Failed to delete food item $foodId: ${error.message}")
            }
    }


    fun loadBanner(): LiveData<MutableList<BannerModel>> {
        val listData = MutableLiveData<MutableList<BannerModel>>()
        val ref = firebaseDatabase.getReference("Banners")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<BannerModel>()
                for (childSnapshot in snapshot.children) {
                    val item = childSnapshot.getValue(BannerModel::class.java)
                    item?.let { list.add(it) }
                }
                listData.value = list
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Database error: ${error.message}")
                // Hoặc hiển thị thông báo lỗi lên UI nếu cần
            }

        })
        return listData
    }

    fun loadFiltered(id:String):LiveData<MutableList<FoodModel>>{
        val listData = MutableLiveData<MutableList<FoodModel>>()
        val ref= firebaseDatabase.getReference("Foods")
        val query: Query=ref.orderByChild("CategoryId").equalTo(id)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("Firebase", "DataSnapshot received: ${snapshot.childrenCount}")
                val lists = mutableListOf<FoodModel>()
                for (childSnapshot in snapshot.children) {
                    val list = childSnapshot.getValue(FoodModel::class.java)
                    if (list != null) {
                        lists.add(list)
                    }
                }
                listData.value = lists
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Database error: ${error.message}")
                // Hoặc hiển thị thông báo lỗi lên UI nếu cần
            }
        })
        return listData
    }
}