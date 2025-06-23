package com.example.app2025.Admin.AddFood


import android.content.Context
import android.net.Uri
import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.create
import java.io.IOException

object CloudinaryUploader {

    private const val CLOUDINARY_URL = "https://api.cloudinary.com/v1_1/dmgsnnicn/image/upload"
    private const val UPLOAD_PRESET = "my_unsigned_preset"

    fun uploadImage(
        context: Context,
        imageUri: Uri,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(imageUri)
            Log.d("UploadDebug", "Uri: $imageUri, stream: $inputStream")


            if (inputStream == null) {
                onError("Không thể đọc ảnh")
                return
            }

            val imageBytes = inputStream.readBytes()
            inputStream.close()

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    "image.jpg",
                    create("image/*".toMediaTypeOrNull(), imageBytes)
                )
                .addFormDataPart("upload_preset", UPLOAD_PRESET)
                .build()

            val request = Request.Builder()
                .url(CLOUDINARY_URL)
                .post(requestBody)
                .build()

            OkHttpClient().newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    onError("Lỗi kết nối: ${e.message}")
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        onError("Lỗi tải lên: ${response.code}")
                        return
                    }

                    val responseBody = response.body?.string()
                    if (responseBody == null) {
                        onError("Không nhận được phản hồi từ server")
                        return
                    }

                    // Extract URL from JSON response
                    val imageUrl = Regex("\"secure_url\":\"(.*?)\"")
                        .find(responseBody)?.groupValues?.get(1)?.replace("\\/", "/")


                    if (imageUrl != null) {
                        onSuccess(imageUrl)
                    } else {
                        onError("Không thể lấy URL ảnh từ phản hồi")
                    }
                }
            })

        } catch (e: Exception) {
            onError("Lỗi: ${e.message}")
        }
    }
}
