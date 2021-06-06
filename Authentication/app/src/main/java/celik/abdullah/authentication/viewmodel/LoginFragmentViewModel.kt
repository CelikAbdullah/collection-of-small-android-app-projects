package celik.abdullah.authentication.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import celik.abdullah.authentication.R
import celik.abdullah.authentication.models.form.LoginFormState
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
* The ViewModel for the LoginFragment. It delegates the requests of the
* LoginFragment to the repository class.
* */
@HiltViewModel
class LoginFragmentViewModel @Inject constructor(private val authRepository: AuthRepository): ViewModel() {

    // holds the result of our login result
    private val _loginResult = MutableLiveData<AuthenticationResult>()
    val loginResult: LiveData<AuthenticationResult> = _loginResult

    // used to start the login procedure
    private val _startLogin = MutableLiveData<Event<Unit>>()
    val startLogin : LiveData<Event<Unit>> = _startLogin
    fun startLogin(){ _startLogin.value = Event(Unit) }

    fun login(username:String, password:String) =
        viewModelScope.launch{
            when(val result = authRepository.login(username, password)){
                // successful login: pass the data we got from the webservice to the LiveData object so that the LoginFragment is notified
                is NetworkResult.Success -> _loginResult.value = AuthenticationResult(success = result.data)
                // error in login: pass the specific error we got from the webservice to the LiveData object so that the LoginFragment is notified
                // the specific error will tell the user what went wrong during login
                is NetworkResult.Error -> _loginResult.value = AuthenticationResult(error = result.error?.errorMessage)
                // network error; just telling the user that he/she has no internet
                is NetworkResult.NetworkError -> _loginResult.value = AuthenticationResult(networkError = R.string.network_error_message)
                // some unknown issue is happened
                is NetworkResult.InvalidData -> _loginResult.value = AuthenticationResult(invalidData = R.string.invalid_data)
            }
        }

    // used to start the login procedure
    private val _startLogout = MutableLiveData<Event<Unit>>()
    val startLogout : LiveData<Event<Unit>> = _startLogout
    fun startLogout(){ _startLogout.value = Event(Unit) }

    // holds our logout result
    private val _logoutResult = MutableLiveData<LogoutResult>()
    val logoutResult: LiveData<LogoutResult> = _logoutResult

    // used to perform the logout request
    fun logout() {
        viewModelScope.launch{
            when(val result = authRepository.logout()){
                // logout is sucessful
                is NetworkResult.Success -> _logoutResult.value = LogoutResult(success=R.string.logout_successful_message)
                // logout results in an error
                is NetworkResult.Error -> _logoutResult.value = LogoutResult(error = result.error?.errorMessage)
                // the user might have internet issues
                is NetworkResult.NetworkError -> _logoutResult.value = LogoutResult(networkError=R.string.network_error_message)
                // some unknown issue is happened
                is NetworkResult.InvalidData -> _logoutResult.value = LogoutResult(invalidData = R.string.invalid_data)
            }
        }
    }

    // used to navigate to the RegistrationFragment
    private val _registerEvent = MutableLiveData<Event<Unit>>()
    val registerEvent : LiveData<Event<Unit>> = _registerEvent
    fun register(){ _registerEvent.value = Event(Unit) }

    // used to navigate to the ForgotPasswordFragment
    private val _forgotPasswordEvent = MutableLiveData<Event<Unit>>()
    val forgotPasswordEvent : LiveData<Event<Unit>> = _forgotPasswordEvent
    fun forgotPasswordEvent(){ _forgotPasswordEvent.value = Event(Unit) }

    private val _loginFormState =  MutableLiveData<LoginFormState>()
    val loginFormState : LiveData<LoginFormState> = _loginFormState
    fun loginDataChanged(username: String, password: String){
        if(!isUsernameValid(username)){
            _loginFormState.value =
                LoginFormState(emailError = R.string.invalid_email)
        }
        else if(!isPasswordValid(password)){
            _loginFormState.value =
                LoginFormState(passwordError = R.string.invalid_password)
        }
        else{
            _loginFormState.value =
                LoginFormState(isDataValid = true)
        }
    }

    fun clearToken() = authRepository.clearToken()


    // a simple password validation check
    private fun isPasswordValid(password: String): Boolean = password.length > 5 && password.isNotBlank()

    // a simple username validation check
    private fun isUsernameValid(username: String): Boolean = username.length > 8 && username.isNotBlank()

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

    // used to check if a token exists - if true, then we know that user is logged-in
    fun checkIfTokenExists(): Boolean = authRepository.retrieveUserTokenFromEncryptSharedPrefs() != null

    // used to get the current user's name and its password
    fun retrieveUsernameFromEncryptedSharedPrefs() = authRepository.retrieveUserNameFromEncryptSharedPrefs()
    fun retrievePasswordFromEncryptedSharedPrefs() = authRepository.retrieveUserPasswordFromEncryptSharedPrefs()

}