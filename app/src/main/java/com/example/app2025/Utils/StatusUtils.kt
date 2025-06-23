package com.example.app2025.Utils

import androidx.compose.ui.graphics.Color

/**
 * Utility class for handling order status display consistently across the app
 */
object StatusUtils {

    /**
     * Status information containing display text and color
     */
    data class StatusInfo(
        val label: String,
        val color: Color
    )

    /**
     * Get status information for display
     * @param status The raw status string from the database
     * @param isAdminView Whether this is being displayed in the admin interface
     * @return StatusInfo containing the display text and color
     */
    fun getStatusInfo(status: String, isAdminView: Boolean = false): StatusInfo {
        return when (status) {
            "pending" -> StatusInfo(
                label = "Chờ xử lý",
                color = Color(0xFFFFA000)
            )
            "processing" -> StatusInfo(
                label = "Đang xử lý",
                color = Color(0xFF2196F3)
            )
            "completed" -> StatusInfo(
                label = "Đã giao hàng",
                color = Color(0xFF4CAF50)
            )
            "cancelled" -> StatusInfo(
                label = "Đã hủy",
                color = Color.Red
            )
            else -> StatusInfo(
                label = "Không xác định",
                color = Color.Gray
            )
        }
    }


    /**
     * Get the next logical status in the order workflow
     * Useful for suggesting the next status to admins
     */
    fun getNextStatus(currentStatus: String): String {
        return when (currentStatus) {
            "pending" -> "processing"
            "processing" -> "completed"
            else -> currentStatus
        }
    }

    /**
     * Check if a status can be cancelled
     */
    fun canBeCancelled(status: String): Boolean {
        return status == "pending" || status == "processing"
    }
}
