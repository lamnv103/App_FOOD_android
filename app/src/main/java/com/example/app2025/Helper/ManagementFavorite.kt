package com.uilover.project2142.Helper

import android.content.Context
import android.widget.Toast
import com.example.app2025.Domain.FoodModel
import com.example.app2025.Helper.TinyDB

class ManagementFavorite(private val context: Context) {
    private val tinyDB = TinyDB(context)

    fun getFavoriteList(): ArrayList<FoodModel> {
        return tinyDB.getListObject("FavoriteList") ?: arrayListOf()
    }

    fun addToFavorite(item: FoodModel) {
        val favoriteList = getFavoriteList()
        val existAlready = favoriteList.any { it.Title == item.Title }

        if (!existAlready) {
            favoriteList.add(item)
            tinyDB.putListObject("FavoriteList", favoriteList)
            Toast.makeText(context, "Added to your Favorites", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Item already in Favorites", Toast.LENGTH_SHORT).show()
        }
    }

    fun removeFromFavorite(item: FoodModel) {
        val favoriteList = getFavoriteList()
        val index = favoriteList.indexOfFirst { it.Title == item.Title }

        if (index >= 0) {
            favoriteList.removeAt(index)
            tinyDB.putListObject("FavoriteList", favoriteList)
            Toast.makeText(context, "Removed from your Favorites", Toast.LENGTH_SHORT).show()
        }
    }

    fun isFavorite(item: FoodModel): Boolean {
        return getFavoriteList().any { it.Title == item.Title }
    }
}