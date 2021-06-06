package celik.abdullah.authentication.repository


import android.net.Network
import android.util.Log
import celik.abdullah.authentication.utils.SessionManager
import celik.abdullah.authentication.database.AuthDatabase
import celik.abdullah.authentication.models.User
import celik.abdullah.authentication.network.*
import celik.abdullah.authentication.utils.handleResponseOfApiCall
import com.google.gson.Gson
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

/*
* The repository has access to local & remote data sources and to the session manager.
* */
class AuthRepository @Inject constructor(private val service: AuthApi,
                                         private val database:AuthDatabase,
                                         private val sessionManager: SessionManager
) {

    // used to perform the login request
    suspend fun login(username:String, password:String): NetworkResult<AuthenticationResponse> =
        try {
            val loginResponse = service.login(username, password)
            handleResponseOfApiCall(loginResponse)
        }
        catch (e:IOException){
            NetworkResult.NetworkError
        }


    // used to perform the registration request
    suspend fun register(username: String, email: String, password: String): NetworkResult<AuthenticationResponse> =
        try{
            val registrationResponse = service.register(username, email, password)
            handleResponseOfApiCall(registrationResponse)
        }  catch (e:IOException){
            NetworkResult.NetworkError
        }


    // used to change the password
    suspend fun changePassword(email:String, newPassword:String): NetworkResult<NetworkChangePasswordResponse> =
        try {
            val changePasswordResponse = service.changePassword(email, newPassword)
            handleResponseOfApiCall(changePasswordResponse)
        }catch (e:IOException){
            NetworkResult.NetworkError
        }


    /*
    * Used to perform our logout request
    * */
    suspend fun logout(): NetworkResult<Unit>{
        return try{
            val token = sessionManager.retrieveUserTokenFromEncryptSharedPrefs()
            val logoutResponse = service.logout("Token $token")
            handleResponseOfApiCall(logoutResponse)
        }catch (e:IOException){
            NetworkResult.NetworkError
        }
    }


    // used to set the current token to null
    fun clearToken() = sessionManager.clearToken()

    // if the login request is a success
    // then store the user data into the database - this type of data could then be shown for example in a ProfileFragment
    suspend fun saveUserData(user: NetworkUserReponse) =
        database.userDao().insertUser(User(
            userName = user.userName,
            userEmail = user.userEmail,
            userId = user.userId,
            userDateJoined = user.userDateJoined,
            userPicture = user.userPicture))


    // The user might want to store the credentials like username, email or password
    // that way, the user does not need to re-type them again
    // Here, we also save the token (in an encrypted way) because some REST APIs might require a
    // token authentication (e.g. changing the profile picture of the user);
    // so a quick lookup for the token (via the EncryptedSharedPreferences) might be very handy later
    fun saveUserCredentialsIntoEncrpytSharedPrefs(email: String, username: String, password: String, token: String)=
        sessionManager.saveUserCredentialsIntoEncryptedSharedPrefs(
            email=email,
            username=username,
            password=password,
            token=token
        )

    // saves the new password of the user
    fun saveNewPasswordIntoEncryptedSharedPrefs(newPassword:String) = sessionManager.saveNewPasswordIntoSharedPrefs(newPassword)


    // With these functions the fragments can grab the saved credentials very easily
    fun retrieveUserTokenFromEncryptSharedPrefs() = sessionManager.retrieveUserTokenFromEncryptSharedPrefs()
    fun retrieveUserNameFromEncryptSharedPrefs() = sessionManager.retrieveUserNameFromEncryptSharedPrefs()
    fun retrieveUserPasswordFromEncryptSharedPrefs() = sessionManager.retrieveUserPasswordFromEncryptSharedPrefs()

}