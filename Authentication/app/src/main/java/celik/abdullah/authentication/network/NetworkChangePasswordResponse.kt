package celik.abdullah.authentication.network

import com.google.gson.annotations.SerializedName

/*
* Holds the response we get after a successful password change.
* */
data class NetworkChangePasswordResponse(
    @SerializedName("response") val response:String
)
