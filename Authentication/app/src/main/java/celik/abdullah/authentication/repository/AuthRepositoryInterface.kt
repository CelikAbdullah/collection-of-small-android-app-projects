package celik.abdullah.authentication.repository

import android.content.SharedPreferences
import celik.abdullah.authentication.network.AuthenticationResponse
import celik.abdullah.authentication.network.NetworkChangePasswordResponse
import celik.abdullah.authentication.network.NetworkResult
import celik.abdullah.authentication.network.NetworkUserReponse

interface AuthRepositoryInterface {
    suspend fun login(username:String, password:String): NetworkResult<AuthenticationResponse>
    suspend fun register(username: String, email: String, password: String): NetworkResult<AuthenticationResponse>
    suspend fun changePassword(email:String, newPassword:String): NetworkResult<NetworkChangePasswordResponse>
    suspend fun logout(): NetworkResult<Unit>

    fun clearToken(): SharedPreferences.Editor
    suspend fun saveUserData(user: NetworkUserReponse)
    fun saveUserCredentialsIntoEncrpytSharedPrefs(email: String, username: String, password: String, token: String): SharedPreferences.Editor
    fun saveNewPasswordIntoEncryptedSharedPrefs(newPassword:String)
    fun retrieveUserTokenFromEncryptSharedPrefs(): String?
    fun retrieveUserNameFromEncryptSharedPrefs(): String?
    fun retrieveUserPasswordFromEncryptSharedPrefs(): String?
}