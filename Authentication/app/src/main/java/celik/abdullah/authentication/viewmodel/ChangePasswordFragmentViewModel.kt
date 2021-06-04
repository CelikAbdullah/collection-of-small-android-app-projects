package celik.abdullah.authentication.viewmodel

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import celik.abdullah.authentication.R
import celik.abdullah.authentication.models.form.ChangePasswordFormState
import celik.abdullah.authentication.models.result.ChangePasswordResult
import celik.abdullah.authentication.network.NetworkResult
import celik.abdullah.authentication.repository.AuthRepository
import celik.abdullah.authentication.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChangePasswordFragmentViewModel @Inject constructor(private val authRepository:AuthRepository): ViewModel() {

    // saves the new password into SharedPreferences
    fun saveNewPassword(newPassword: String) =
        authRepository.saveNewPasswordIntoEncryptedSharedPrefs(newPassword)

    // triggers setting of the new password
    private val _changePasswordEvent = MutableLiveData<Event<Unit>>()
    val changePasswordEvent : LiveData<Event<Unit>> = _changePasswordEvent
    fun resetPassword(){
        _changePasswordEvent.value = Event(Unit)
    }

    // used to reset the password
    fun changePassword(email: String, newPassword: String){
        viewModelScope.launch {
            when(val result = authRepository.changePassword(email, newPassword)){
                is NetworkResult.Success ->
                    _forgotPasswordResult.value = ChangePasswordResult(success = result.data?.response)
                is NetworkResult.Error ->
                    _forgotPasswordResult.value = ChangePasswordResult(error = result.error?.errorMessage)
                NetworkResult.NetworkError ->
                    _forgotPasswordResult.value = ChangePasswordResult(networkError =  R.string.network_error_message)
            }
        }
    }

    // used to validate the given inputs
    private val _forgotPasswordFormState = MutableLiveData<ChangePasswordFormState>()
    val changePasswordFormState : LiveData<ChangePasswordFormState> = _forgotPasswordFormState

    // used to handle the result of the network request for resetting the password
    private val _forgotPasswordResult = MutableLiveData<ChangePasswordResult>()
    val changePasswordResult : LiveData<ChangePasswordResult> = _forgotPasswordResult

    // used whenever the form data for resetting the password is changed
    fun changePasswordFormDataChanged(email: String, newPassword: String){
        if(!isEmailValid(email)){
            _forgotPasswordFormState.value = ChangePasswordFormState(emailError = R.string.invalid_email)
        }
        else if(!isPasswordValid(newPassword)){
            _forgotPasswordFormState.value = ChangePasswordFormState(newPasswordError = R.string.invalid_password)
        }
        else{
            _forgotPasswordFormState.value = ChangePasswordFormState(isDataValid = true)
        }
    }


    // A username validation check
    private fun isEmailValid(username:String): Boolean =
        if (username.contains("@")) {
            Patterns.EMAIL_ADDRESS.matcher(username).matches()
        } else {
            false
        }


    // A password validation check
    private fun isPasswordValid(password: String): Boolean =
        password.length > 5 && password.isNotBlank()

}