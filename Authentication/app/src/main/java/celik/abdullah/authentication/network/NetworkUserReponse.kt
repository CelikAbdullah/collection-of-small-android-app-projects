package celik.abdullah.authentication.network

import com.google.gson.annotations.SerializedName

/*
* Holds the user's data we get after a successful login or registration
* request.
* */
data class NetworkUserReponse(
    @SerializedName("id") val userId: Long,
    @SerializedName("email") val userEmail: String,
    @SerializedName("username") val userName: String,
    @SerializedName("date_joined") val userDateJoined: String,
    @SerializedName("picture") val userPicture: String
)
