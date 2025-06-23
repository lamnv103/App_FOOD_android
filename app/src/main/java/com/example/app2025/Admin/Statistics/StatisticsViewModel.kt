package com.example.app2025.Admin.Statistics

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
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class StatisticsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    private val database = FirebaseDatabase.getInstance()
    private val ordersRef = database.getReference("Orders")
    private val orderItemsRef = database.getReference("OrderItems")

    // Store listeners to remove them when needed
    private var ordersListener: ValueEventListener? = null

    init {
        loadDailyStatistics()
    }

    override fun onCleared() {
        super.onCleared()
        // Remove listeners when ViewModel is cleared
        removeListeners()
    }

    private fun removeListeners() {
        ordersListener?.let {
            ordersRef.removeEventListener(it)
            ordersListener = null
        }
    }

    /** Tải thống kê theo ngày (7 ngày gần nhất) */
    fun loadDailyStatistics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val calendar = Calendar.getInstance()
            val endDate = calendar.timeInMillis
            calendar.add(Calendar.DAY_OF_YEAR, -6) // 7 ngày bao gồm hôm nay
            val startDate = calendar.timeInMillis
            loadStatistics(startDate, endDate, "day")
        }
    }

    /** Tải thống kê theo tháng (6 tháng gần nhất) */
    fun loadMonthlyStatistics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val calendar = Calendar.getInstance()
            val endDate = calendar.timeInMillis
            calendar.add(Calendar.MONTH, -5) // 6 tháng bao gồm tháng hiện tại
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startDate = calendar.timeInMillis
            loadStatistics(startDate, endDate, "month")
        }
    }

    /** Tải thống kê theo năm (năm hiện tại) */
    fun loadYearlyStatistics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            calendar.set(year, Calendar.JANUARY, 1, 0, 0, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startDate = calendar.timeInMillis
            calendar.set(year, Calendar.DECEMBER, 31, 23, 59, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            val endDate = calendar.timeInMillis
            loadStatistics(startDate, endDate, "year")
        }
    }

    /** Tải và xử lý dữ liệu thống kê từ Firebase */
    private fun loadStatistics(startDate: Long, endDate: Long, period: String) {
        // Remove previous listeners
        removeListeners()

        // Initialize the UI state with loading
        _uiState.update { it.copy(
            isLoading = true,
            error = null,
            totalOrders = 0,
            completedOrders = 0,
            cancelledOrders = 0,
            totalRevenue = "0₫",
            revenueData = emptyList(),
            topProducts = emptyList()
        )}

        Log.d("StatisticsViewModel", "Bắt đầu tải dữ liệu từ $startDate đến $endDate, period: $period")

        // Create a new listener
        ordersListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("StatisticsViewModel", "Đã tải: ${snapshot.childrenCount} đơn hàng")

                viewModelScope.launch {
                    try {
                        processOrdersData(snapshot, startDate, endDate, period)
                    } catch (e: Exception) {
                        Log.e("StatisticsViewModel", "Lỗi xử lý dữ liệu: ${e.message}", e)
                        _uiState.update { it.copy(
                            isLoading = false,
                            error = "Lỗi xử lý dữ liệu: ${e.message}"
                        )}
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("StatisticsViewModel", "Lỗi tải dữ liệu: ${error.message}")
                _uiState.update { it.copy(
                    isLoading = false,
                    error = error.message
                )}
            }
        }

        // Add the listener to Firebase
        ordersRef.orderByChild("timestamp")
            .startAt(startDate.toDouble())
            .endAt(endDate.toDouble())
            .addValueEventListener(ordersListener!!)
    }

    private suspend fun processOrdersData(snapshot: DataSnapshot, startDate: Long, endDate: Long, period: String) {
        var totalOrders = 0
        var completedOrders = 0
        var cancelledOrders = 0
        var totalRevenue = 0.0
        val revenueMap = initializeRevenueMap(period)
        val productMap = mutableMapOf<String, ProductStat>()
        val completedOrderIds = mutableListOf<String>()

        // Process orders
        for (orderSnapshot in snapshot.children) {
            val status = orderSnapshot.child("status").getValue(String::class.java)
            val timestamp = orderSnapshot.child("timestamp").getValue(Long::class.java) ?: 0L

            if (timestamp < startDate || timestamp > endDate) {
                Log.d("StatisticsViewModel", "Bỏ qua đơn hàng ngoài phạm vi: $timestamp")
                continue
            }

            totalOrders++

            when (status) {
                "completed" -> {
                    completedOrders++
                    val totalPrice = orderSnapshot.child("total_price").getValue(String::class.java)
                    val price = parseCurrency(totalPrice)

                    if (price == 0.0 && totalPrice != null && totalPrice.isNotEmpty()) {
                        Log.w("StatisticsViewModel", "Lỗi phân tích total_price: $totalPrice")
                    }

                    totalRevenue += price

                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = timestamp
                    val dateKey = formatDateKey(calendar, period)

                    revenueMap[dateKey] = (revenueMap[dateKey] ?: 0.0) + price

                    val orderId = orderSnapshot.key
                    if (orderId != null) {
                        completedOrderIds.add(orderId)
                    }
                }
                "cancelled" -> cancelledOrders++
            }
        }

        // Load order items for all completed orders
        if (completedOrderIds.isNotEmpty()) {
            loadAllOrderItems(completedOrderIds, productMap)
        }

        // Format data for display
        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        val revenueData = formatRevenueData(revenueMap, period)

        val topProducts = productMap.values
            .sortedByDescending { it.revenue }
            .take(5)
            .map {
                TopProduct(
                    name = it.name,
                    quantity = it.quantity,
                    revenue = formatter.format(it.revenue)
                )
            }

        // Update UI state
        _uiState.update {
            it.copy(
                isLoading = false,
                totalOrders = totalOrders,
                completedOrders = completedOrders,
                cancelledOrders = cancelledOrders,
                totalRevenue = formatter.format(totalRevenue),
                revenueData = revenueData,
                topProducts = topProducts
            )
        }

        Log.d("StatisticsViewModel", "Đã cập nhật uiState: đơn hàng=$totalOrders, hoàn thành=$completedOrders, hủy=$cancelledOrders, doanh thu=$totalRevenue")
    }

    private fun initializeRevenueMap(period: String): MutableMap<String, Double> {
        val revenueMap = mutableMapOf<String, Double>()
        val calendar = Calendar.getInstance()

        when (period) {
            "day" -> {
                val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
                for (i in 6 downTo 0) {
                    calendar.timeInMillis = System.currentTimeMillis()
                    calendar.add(Calendar.DAY_OF_YEAR, -i)
                    val dateKey = dateFormat.format(calendar.time)
                    revenueMap[dateKey] = 0.0
                }
            }
            "month" -> {
                val dateFormat = SimpleDateFormat("MM/yy", Locale.getDefault())
                for (i in 5 downTo 0) {
                    calendar.timeInMillis = System.currentTimeMillis()
                    calendar.add(Calendar.MONTH, -i)
                    val dateKey = dateFormat.format(calendar.time)
                    revenueMap[dateKey] = 0.0
                }
            }
            "year" -> {
                val calendar = Calendar.getInstance()
                val year = calendar.get(Calendar.YEAR)
                val dateFormat = SimpleDateFormat("MM/yy", Locale.getDefault())
                for (month in 0..11) {
                    calendar.set(year, month, 1)
                    val dateKey = dateFormat.format(calendar.time)
                    revenueMap[dateKey] = 0.0
                }
            }
        }

        return revenueMap
    }

    private fun formatDateKey(calendar: Calendar, period: String): String {
        return when (period) {
            "day" -> SimpleDateFormat("dd/MM", Locale.getDefault()).format(calendar.time)
            else -> SimpleDateFormat("MM/yy", Locale.getDefault()).format(calendar.time)
        }
    }

    private fun formatRevenueData(revenueMap: Map<String, Double>, period: String): List<RevenueData> {
        return revenueMap.entries
            .sortedBy {
                when (period) {
                    "day" -> {
                        val parts = it.key.split("/")
                        if (parts.size < 2) return@sortedBy 0
                        try {
                            val day = parts[0].toInt()
                            val month = parts[1].toInt()
                            month * 100 + day
                        } catch (e: NumberFormatException) {
                            Log.e("StatisticsViewModel", "Lỗi phân tích ngày: ${it.key}", e)
                            0
                        }
                    }
                    else -> {
                        val parts = it.key.split("/")
                        if (parts.size < 2) return@sortedBy 0
                        try {
                            val month = parts[0].toInt()
                            val year = parts[1].toInt()
                            year * 100 + month
                        } catch (e: NumberFormatException) {
                            Log.e("StatisticsViewModel", "Lỗi phân tích tháng: ${it.key}", e)
                            0
                        }
                    }
                }
            }
            .map { RevenueData(it.key, it.value) }
    }

    private fun parseCurrency(value: String?): Double {
        if (value.isNullOrEmpty()) return 0.0

        return try {
            val cleanValue = value.replace("[^\\d]".toRegex(), "")
            if (cleanValue.isEmpty()) 0.0 else cleanValue.toDouble()
        } catch (e: NumberFormatException) {
            Log.e("StatisticsViewModel", "Lỗi phân tích giá trị: $value", e)
            0.0
        }
    }

    /** Tải chi tiết các sản phẩm trong đơn hàng */
    private suspend fun loadAllOrderItems(orderIds: List<String>, productMap: MutableMap<String, ProductStat>) {
        for (orderId in orderIds) {
            try {
                val items = loadOrderItemsForOrder(orderId)
                for (item in items) {
                    val productId = item.first
                    val name = item.second
                    val price = item.third
                    val quantity = item.fourth

                    val revenue = price * quantity
                    val stat = productMap.getOrPut(productId) {
                        ProductStat(name = name, quantity = 0, revenue = 0.0)
                    }

                    productMap[productId] = stat.copy(
                        quantity = stat.quantity + quantity,
                        revenue = stat.revenue + revenue
                    )
                }
            } catch (e: Exception) {
                Log.e("StatisticsViewModel", "Lỗi tải sản phẩm cho đơn hàng $orderId: ${e.message}", e)
            }
        }
    }

    private suspend fun loadOrderItemsForOrder(orderId: String): List<Quadruple<String, String, Double, Int>> {
        return suspendCoroutine { continuation ->
            orderItemsRef.orderByChild("order_id").equalTo(orderId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        Log.d("StatisticsViewModel", "Đã tải ${snapshot.childrenCount} sản phẩm cho đơn hàng $orderId")
                        val items = mutableListOf<Quadruple<String, String, Double, Int>>()

                        for (itemSnapshot in snapshot.children) {
                            try {
                                val productId = itemSnapshot.child("product_id").getValue(String::class.java) ?: ""
                                val name = itemSnapshot.child("name").getValue(String::class.java) ?: "Không xác định"
                                val priceStr = itemSnapshot.child("price").getValue(String::class.java) ?: "0"
                                val price = parseCurrency(priceStr)
                                val quantity = itemSnapshot.child("quantity").getValue(Int::class.java) ?: 1

                                items.add(Quadruple(productId, name, price, quantity))
                            } catch (e: Exception) {
                                Log.e("StatisticsViewModel", "Lỗi xử lý sản phẩm: ${e.message}", e)
                            }
                        }

                        continuation.resume(items)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("StatisticsViewModel", "Lỗi tải sản phẩm đơn hàng: ${error.message}")
                        continuation.resumeWithException(Exception(error.message))
                    }
                })
        }
    }
}

/** Trạng thái giao diện thống kê */
data class StatisticsUiState(
    val error: String? = null,
    val isLoading: Boolean = true,
    val totalOrders: Int = 0,
    val completedOrders: Int = 0,
    val cancelledOrders: Int = 0,
    val totalRevenue: String = "0₫",
    val revenueData: List<RevenueData> = emptyList(),
    val topProducts: List<TopProduct> = emptyList()
)

/** Dữ liệu doanh thu cho biểu đồ */
data class RevenueData(
    val label: String,
    val value: Double
)

/** Thông tin sản phẩm bán chạy */
data class TopProduct(
    val name: String,
    val quantity: Int,
    val revenue: String
)

/** Thống kê sản phẩm tạm thời */
data class ProductStat(
    val name: String,
    val quantity: Int,
    val revenue: Double
)

/** Helper class for returning multiple values */
data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)