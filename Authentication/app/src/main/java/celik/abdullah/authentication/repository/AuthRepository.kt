package celik.abdullah.authentication.repository


import android.util.Log
import celik.abdullah.authentication.utils.SessionManager
import celik.abdullah.authentication.database.AuthDatabase
import celik.abdullah.authentication.models.User
import celik.abdullah.authentication.network.*
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
        try{
            val loginResponse = service.login(username, password)
            // everything is ok; return a success
            NetworkResult.Success(loginResponse)
        }catch (e:Throwable){
            // something went wrong
            when(e){
                // the user got a specific error from the webservice
                is HttpException -> NetworkResult.Error(errorHandling(e))
                // the user has no internet access
                is IOException -> NetworkResult.NetworkError
                else -> NetworkResult.Error(null)
            }
        }


    // used to perform the registration request
    suspend fun register(username: String, email: String, password: String): NetworkResult<AuthenticationResponse> =
        try{
            val registrationResponse = service.register(username, email, password)
            // everything is ok; return a success
            NetworkResult.Success(registrationResponse)
        }catch (e:Throwable){
            // something went wrong
            when(e){
                // the user got a specific error from the webservice
                is HttpException -> NetworkResult.Error(errorHandling(e))
                // the user has no internet access
                is IOException -> NetworkResult.NetworkError
                else -> NetworkResult.Error(null)
            }
        }


    // used to change the password
    suspend fun changePassword(email:String, newPassword:String): NetworkResult<NetworkChangePasswordResponse> =
        try {
            val changePasswordResponse = service.changePassword(email, newPassword)
            NetworkResult.Success(changePasswordResponse)
        }catch (e:Throwable){
            when(e){
                is HttpException -> NetworkResult.Error(errorHandling(e))
                is IOException -> NetworkResult.NetworkError
                else -> NetworkResult.Error(null)
            }
        }

    /*
    * Used to perform our logout request
    * The reason why this method handles the response a little bit differently than the others
    * is because of the response we get from the server. The django knox authentication library
    * returns a "204 no-content" message when the request is successful instead of returning a 
    * 200 message indicating that the request was successful. The 204 message is also okay but 
    * Retrofit throws a null pointer exception when it gets such a response. 
    * To apply this to my NetworkResult sealed class, I had to treat the response as you see below.
    * */
    suspend fun logout(): NetworkResult<Response<Unit>>{
        return try{
            val token = sessionManager.retrieveUserTokenFromEncryptSharedPrefs()
            val logoutResponse = service.logout("Token $token")
            if(!logoutResponse.isSuccessful){
                /*
                * I check the response code for HTTP exceptions
                * Every JSON output with code equal or above 400 will be converted into our
                * NetworkErrorResponse data class.
                * */
                if(logoutResponse.code()>=400){
                    NetworkResult.Error(Gson().fromJson(logoutResponse.errorBody()?.charStream(), NetworkErrorResponse::class.java))
                }else{
                    NetworkResult.Success(logoutResponse)
                }
            }else{
                NetworkResult.Success(logoutResponse)
            }
        }catch(e:IOException){
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
    fun retrieveUserEmailFromEncryptSharedPrefs() = sessionManager.retrieveUserEmailFromEncryptSharedPrefs()


    /*
    * When a generic error occurs (non-200), then it should try to convert the response body to
    * an error response. This way we can show a specific error to the user
    * */
    private fun errorHandling(throwable: HttpException): NetworkErrorResponse? =
        try {
            throwable.response()?.errorBody()?.let { errorBody ->
                Gson().fromJson(errorBody.string(), NetworkErrorResponse::class.java)
            }
        }catch (e:Exception){
            null
        }
}