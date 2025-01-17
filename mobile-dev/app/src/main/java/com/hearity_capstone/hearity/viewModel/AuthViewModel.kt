package com.hearity_capstone.hearity.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hearity_capstone.hearity.data.api.TokenProvider
import com.hearity_capstone.hearity.data.model.UserModel
import com.hearity_capstone.hearity.data.model.authentication.SignUpRequest
import com.hearity_capstone.hearity.data.repository.authentication.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class AuthViewModel(
    private val repository: AuthRepository,
    private val tokenProvider: TokenProvider,
) : ViewModel() {
    private val _isLoading = MutableStateFlow<Boolean>(false)
    private val _isLoggedIn = MutableStateFlow<Boolean>(false)
    private val _loginState = MutableStateFlow<UserModel?>(null)
    private val _errorState = MutableStateFlow<String?>(null)
    private val _signupState = MutableStateFlow<SignupState>(SignupState.Idle)

    val signupState: StateFlow<SignupState> = _signupState
    val isLoading: StateFlow<Boolean> = _isLoading
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn
    val loginState: StateFlow<UserModel?> = _loginState
    val errorState: StateFlow<String?> = _errorState

    fun verifyToken() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val token = tokenProvider.getToken()

                val response = repository.verifyToken(token.toString())
                if (response.statusCode == 200) {
                    _isLoggedIn.value = true
                    _loginState.value = response.data
                }
            } catch (e: HttpException) {
                _errorState.value = "Session Expired"
            } catch (e: IOException) {
                _errorState.value = "Network error, please check your connection"
            } catch (e: IllegalStateException) {
                return@launch
            } catch (e: Exception) {
                _errorState.value = "Unexpected error"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signUp(request: SignUpRequest) {
        viewModelScope.launch {
            try {
                _signupState.value = SignupState.Loading
                _isLoading.value = true
                val response = repository.signUp(request)
                if (response.statusCode == 200) {
                    _signupState.value = SignupState.Success
                } else {
                    _signupState.value = SignupState.Error(response.message)
                }

            } catch (e: IOException) {
                _signupState.value =
                    SignupState.Error("Network error, please check your connection")
            } catch (e: IllegalStateException) {
                return@launch
            } catch (e: Exception) {
                _signupState.value = SignupState.Error("Unexpected error or user already exists")
            }
        }
    }


    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.login(email, password)
                if (response.statusCode == 200) {
                    _isLoggedIn.value = true
                    _loginState.value = response.data
                    tokenProvider.saveToken(response.token)
                } else {
                    _errorState.value = response.message
                }
            } catch (e: HttpException) {
                _errorState.value = "Something went wrong, check your email or password"
            } catch (e: IOException) {
                _errorState.value = "Network error, please check your connection"
            } catch (e: Exception) {
                _errorState.value = "Unexpected error"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                tokenProvider.clearToken()
                _isLoggedIn.value = false
                _loginState.value = null
                _errorState.value = null
            } catch (e: Exception) {
                _errorState.value = "Unexpected error"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearErrorState() {
        _errorState.value = null
    }
}

sealed class SignupState {
    object Idle : SignupState()
    object Loading : SignupState()
    object Success : SignupState()
    data class Error(val message: String) : SignupState()
}