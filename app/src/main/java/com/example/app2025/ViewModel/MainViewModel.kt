package com.example.app2025.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.app2025.Domain.BannerModel
import com.example.app2025.Domain.CategoryModel
import com.example.app2025.Domain.FoodModel
import com.example.app2025.Repository.MainRepository

class MainViewModel: ViewModel() {
    private val repository= MainRepository()
    private val _isLoading = MutableLiveData<Boolean>(true)

    val isLoading: LiveData<Boolean> get() = _isLoading

    fun loadBanner(): LiveData<MutableList<BannerModel>>{
        return repository.loadBanner()
    }

    fun loadCategory(): LiveData<MutableList<CategoryModel>>{
        return repository.loadCategory()
    }

    fun loadFood(): LiveData<MutableList<FoodModel>> {
        val foods = repository.loadFood()
        foods.observeForever { foodList ->
            _isLoading.value = foodList == null
        }
        return foods
    }

    fun deleteFood(foodId: Int) {
        repository.deleteFood(foodId)
    }

    fun loadFiltered(id: String): LiveData<MutableList<FoodModel>>{
        return repository.loadFiltered(id)
    }

}