package com.example.app2025.Login

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.app2025.Domain.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

private const val TAG = "AuthViewModel"

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth
    private val database = FirebaseDatabase.getInstance()
    private val usersRef = database.getReference("Users")

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    private val _currentUser = MutableLiveData<UserModel?>()
    val currentUser: LiveData<UserModel?> = _currentUser

    init {
        checkAuthStatus()
    }

    fun checkAuthStatus() {
        val user = auth.currentUser
        if (user == null) {
            _authState.value = AuthState.Unauthenticated
            _currentUser.value = null
        } else {
            // Check if email is verified
            if (user.isEmailVerified) {
                fetchUserData(user)
            } else {
                _authState.value = AuthState.EmailNotVerified
                _currentUser.value = null
            }
        }
    }

    private fun fetchUserData(firebaseUser: FirebaseUser) {
        _authState.value = AuthState.Loading

        usersRef.child(firebaseUser.uid).get().addOnSuccessListener { snapshot ->
            val userModel = snapshot.getValue(UserModel::class.java)
            if (userModel != null) {
                _currentUser.value = userModel
                _authState.value = AuthState.Authenticated
                Log.d(TAG, "User data loaded successfully")
            } else {
                // Nếu không tìm thấy thông tin người dùng, tạo mới từ thông tin auth
                val newUser = UserModel(
                    id = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    name = firebaseUser.displayName ?: "",
                    image = firebaseUser.photoUrl?.toString() ?: "",
                    phone = firebaseUser.phoneNumber ?: ""
                )
                _currentUser.value = newUser

                // Lưu thông tin người dùng mới vào database
                usersRef.child(firebaseUser.uid).setValue(newUser)
                    .addOnSuccessListener {
                        _authState.value = AuthState.Authenticated
                        Log.d(TAG, "New user profile created")
                    }
                    .addOnFailureListener { e ->
                        _authState.value = AuthState.Error("Không thể lưu thông tin người dùng: ${e.message}")
                        Log.e(TAG, "Failed to create user profile", e)
                    }
            }
        }.addOnFailureListener { e ->
            _authState.value = AuthState.Error("Không thể lấy thông tin người dùng: ${e.message}")
            Log.e(TAG, "Failed to fetch user data", e)
        }
    }

    fun login(email: String, password: String) {
        // Validate email format
        if (!isValidEmail(email)) {
            _authState.value = AuthState.Error("Email không hợp lệ. Vui lòng nhập đúng định dạng email (phải có @)")
            return
        }

        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email hoặc mật khẩu không được để trống")
            return
        }

        _authState.value = AuthState.Loading
        Log.d(TAG, "Attempting login with email: $email")

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithEmail:success")
                    val user = auth.currentUser
                    if (user != null) {
                        if (user.isEmailVerified) {
                            fetchUserData(user)
                        } else {
                            _authState.value = AuthState.EmailNotVerified
                            Log.d(TAG, "Email not verified")
                        }
                    } else {
                        _authState.value = AuthState.Error("Đăng nhập thất bại")
                    }
                } else {
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    _authState.value = AuthState.Error(task.exception?.message ?: "Đăng nhập thất bại")
                }
            }
    }

    fun signup(email: String, password: String, name: String = "", phone: String = "") {
        // Validate email format
        if (!isValidEmail(email)) {
            _authState.value = AuthState.Error("Email không hợp lệ. Vui lòng nhập đúng định dạng email (phải có @)")
            return
        }

        // Validate phone number
        if (phone.isNotEmpty() && !isValidPhoneNumber(phone)) {
            _authState.value = AuthState.Error("Số điện thoại không hợp lệ. Vui lòng nhập đúng 10 số")
            return
        }

        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email hoặc mật khẩu không được để trống")
            return
        }

        _authState.value = AuthState.Loading
        Log.d(TAG, "Checking if email already exists: $email")

        // Check if email already exists in the database
        checkEmailExists(email) { emailExists ->
            if (emailExists) {
                _authState.value = AuthState.Error("Email đã được sử dụng. Vui lòng sử dụng email khác")
                return@checkEmailExists
            }

            Log.d(TAG, "Attempting to create user with email: $email")
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "createUserWithEmail:success")
                        // Tạo thông tin người dùng mới
                        val user = auth.currentUser
                        if (user != null) {
                            // Gửi email xác thực
                            user.sendEmailVerification()
                                .addOnCompleteListener { verificationTask ->
                                    if (verificationTask.isSuccessful) {
                                        Log.d(TAG, "Verification email sent")

                                        // Lưu thông tin người dùng vào database
                                        val newUser = UserModel(
                                            id = user.uid,
                                            email = email,
                                            name = name,
                                            phone = phone
                                        )

                                        usersRef.child(user.uid).setValue(newUser)
                                            .addOnSuccessListener {
                                                _currentUser.value = newUser
                                                _authState.value = AuthState.RegistrationSuccess
                                                Log.d(TAG, "User profile created successfully")
                                            }
                                            .addOnFailureListener { e ->
                                                _authState.value = AuthState.Error("Không thể lưu thông tin người dùng: ${e.message}")
                                                Log.e(TAG, "Failed to create user profile", e)
                                            }
                                    } else {
                                        _authState.value = AuthState.Error("Không thể gửi email xác thực: ${verificationTask.exception?.message}")
                                        Log.e(TAG, "Failed to send verification email", verificationTask.exception)
                                    }
                                }
                        } else {
                            _authState.value = AuthState.Error("Đăng ký thất bại")
                        }
                    } else {
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                        _authState.value = AuthState.Error(task.exception?.message ?: "Đăng ký thất bại")
                    }
                }
        }
    }

    // Check if email already exists in the database
    private fun checkEmailExists(email: String, callback: (Boolean) -> Unit) {
        val normalizedEmail = email.lowercase().trim()

        // First check in Firebase Auth
        auth.fetchSignInMethodsForEmail(normalizedEmail)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val signInMethods = task.result?.signInMethods
                    if (signInMethods != null && signInMethods.isNotEmpty()) {
                        // Email exists in Firebase Auth
                        callback(true)
                        return@addOnCompleteListener
                    }

                    // If not found in Auth, check in the database
                    usersRef.orderByChild("email").equalTo(normalizedEmail)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                callback(snapshot.exists())
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.e(TAG, "Error checking email existence", error.toException())
                                // Assume email doesn't exist if there's an error
                                callback(false)
                            }
                        })
                } else {
                    Log.e(TAG, "Error checking email existence", task.exception)
                    // Assume email doesn't exist if there's an error
                    callback(false)
                }
            }
    }

    // Validate email format
    private fun isValidEmail(email: String): Boolean {
        return email.contains("@") && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Validate phone number (must be 10 digits)
    private fun isValidPhoneNumber(phone: String): Boolean {
        return phone.length == 10 && phone.all { it.isDigit() }
    }

    fun signout() {
        auth.signOut()
        _currentUser.value = null
        _authState.value = AuthState.Unauthenticated
        Log.d(TAG, "User signed out")
    }

    fun updateUserProfile(user: UserModel) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            usersRef.child(currentUser.uid).setValue(user)
                .addOnSuccessListener {
                    _currentUser.value = user
                    Log.d(TAG, "User profile updated successfully")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to update user profile", e)
                }
        }
    }

    fun reload() {
        val user = auth.currentUser
        user?.reload()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                checkAuthStatus()
                Log.d(TAG, "User data reloaded")
            } else {
                Log.e(TAG, "Failed to reload user", task.exception)
            }
        }
    }

    fun resendVerificationEmail() {
        val user = auth.currentUser
        if (user != null && !user.isEmailVerified) {
            user.sendEmailVerification()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _authState.value = AuthState.VerificationEmailSent
                        Log.d(TAG, "Verification email resent")
                    } else {
                        _authState.value = AuthState.Error("Không thể gửi lại email xác thực: ${task.exception?.message}")
                        Log.e(TAG, "Failed to resend verification email", task.exception)
                    }
                }
        } else {
            _authState.value = AuthState.Error("Không thể gửi email xác thực")
        }
    }

    fun sendPasswordResetEmail(email: String) {
        if (email.isEmpty()) {
            _authState.value = AuthState.Error("Email không được để trống")
            return
        }

        if (!isValidEmail(email)) {
            _authState.value = AuthState.Error("Email không hợp lệ. Vui lòng nhập đúng định dạng email (phải có @)")
            return
        }

        _authState.value = AuthState.Loading
        Log.d(TAG, "Sending password reset email to: $email")

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.PasswordResetEmailSent
                    Log.d(TAG, "Password reset email sent successfully")
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Không thể gửi email đặt lại mật khẩu")
                    Log.e(TAG, "Failed to send password reset email", task.exception)
                }
            }
    }
}

sealed class AuthState {
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    object EmailNotVerified : AuthState()
    object RegistrationSuccess : AuthState()
    object VerificationEmailSent : AuthState()
    object PasswordResetEmailSent : AuthState()
    data class Error(val message: String) : AuthState()
}