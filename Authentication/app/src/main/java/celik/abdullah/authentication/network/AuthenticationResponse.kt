package celik.abdullah.authentication.network

import com.google.gson.annotations.SerializedName

/*
* The response we get from the server side after a login/registration request.
* */
data class AuthenticationResponse(
    @SerializedName("response") val response: String,
    @SerializedName("user") val user: NetworkUserReponse,
    @SerializedName("token") val token:String,
)
