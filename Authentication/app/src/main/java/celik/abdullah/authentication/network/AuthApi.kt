package celik.abdullah.authentication.network

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

/*
* A collection of REST API endpoints to communicate with the logic
* on the server side.
* */
interface AuthApi {

    // used to execute the registration request
    @POST("api/auth/register")
    @FormUrlEncoded
    suspend fun register(@Field("username") username: String,
                         @Field("email") email: String,
                         @Field("password") password: String) : AuthenticationResponse

    // used to execute the login request
    @POST("api/auth/login")
    @FormUrlEncoded
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String): AuthenticationResponse

    // used to execute the password changing request
    @POST("changePassword")
    @FormUrlEncoded
    suspend fun changePassword(
        @Field("email") email: String,
        @Field("newPassword") newPassword: String) : NetworkChangePasswordResponse

    // used to execute the logout request
    @POST("logout")
    suspend fun logout(@Header("Authorization") token:String) : Response<Unit>
}