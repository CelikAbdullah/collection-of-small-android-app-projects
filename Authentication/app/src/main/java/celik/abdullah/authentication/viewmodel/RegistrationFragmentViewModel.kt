package celik.abdullah.authentication.viewmodel

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import celik.abdullah.authentication.R
import celik.abdullah.authentication.models.form.RegistrationFormState
import celik.abdullah.authentication.models.result.AuthenticationResult
import celik.abdullah.authentication.models.result.LogoutResult
import celik.abdullah.authentication.network.NetworkResult
import celik.abdullah.authentication.network.NetworkUserReponse
import celik.abdullah.authentication.repository.AuthRepository
import celik.abdullah.authentication.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/*
* The ViewModel of the RegistrationFragment. It delegates the requests of the
* RegistrationFragment to the repository class.
* */
@HiltViewModel
class RegistrationFragmentViewModel @Inject constructor(private val authRepository:AuthRepository): ViewModel(){

    // used to start the registration process
    private val _startRegistration = MutableLiveData<Event<Unit>>()
    val startRegistration: LiveData<Event<Unit>> = _startRegistration
    fun startRegistration(){ _startRegistration.value = Event(Unit) }

    // holds the result of our registration request
    private val _registrationResult = MutableLiveData<AuthenticationResult>()
    val registrationResult : LiveData<AuthenticationResult> = _registrationResult

    // used to perform the registration request
    fun register(userName:String, email: String, password: String) =
        viewModelScope.launch {
            when(val result = authRepository.register(userName, email, password)){
                // successful registration: pass the data we got from the webservice to the LiveData object so that the RegistrationFragment is notified
                is NetworkResult.Success -> _registrationResult.value = AuthenticationResult(success = result.data)
                // error in registration: pass the specific error we got from the webservice to the LiveData object so that the RegistrationFragment is notified
                // the specific error will tell the user what went wrong during registration
                is NetworkResult.Error -> _registrationResult.value = AuthenticationResult(error = result.error?.errorMessage)
                // network error; just telling the user that he/she has no internet
                is NetworkResult.NetworkError -> _registrationResult.value = AuthenticationResult(networkError = R.string.network_error_message)
                // some unknown issue is happened
                is NetworkResult.InvalidData -> _registrationResult.value = AuthenticationResult(invalidData = R.string.invalid_data)
            }
        }

    private val _registrationFormState = MutableLiveData<RegistrationFormState>()
    val registrationFormState : LiveData<RegistrationFormState> = _registrationFormState

    fun registrationDataChanged(email: String, username: String, password: String){
        if(!isEmailValid(email)){
            _registrationFormState.value = RegistrationFormState(emailError = R.string.invalid_email)
        }
        else if(!isUsernameValid(username)){
            _registrationFormState.value = RegistrationFormState(usernameError = R.string.invalid_username)
        }
        else if(!isPasswordValid(password)){
            _registrationFormState.value = RegistrationFormState(passwordError = R.string.invalid_password)
        }
        else{
            _registrationFormState.value = RegistrationFormState(isDataValid = true)
        }
    }

    // a simple username validation check
    private fun isEmailValid(username:String): Boolean =
        if (username.contains("@")) {
            Patterns.EMAIL_ADDRESS.matcher(username).matches()
        } else {
            false
        }


    // a simple username validation check
    private fun isUsernameValid(username: String): Boolean = username.length > 8 && username.isNotBlank()
    // a simple password validation check
    private fun isPasswordValid(password: String): Boolean = password.length > 5 && password.isNotBlank()

    // save user data to the local database
    fun storeUserDataLocally(user: NetworkUserReponse) =
        viewModelScope.launch {
            authRepository.saveUserData(user)
        }


    // if the user accepts, we store his/her credentials via the Security library
    fun saveUserDataIntoEncryptedSharedPreferences(email: String, username: String, password: String, token: String) =
        viewModelScope.launch {
            authRepository.saveUserCredentialsIntoEncrpytSharedPrefs(
                email=email,
                username=username,
                password=password,
                token=token
            )
        }
}