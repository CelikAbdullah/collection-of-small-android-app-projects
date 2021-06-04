package celik.abdullah.authentication.network

import com.google.gson.annotations.SerializedName

/*
* Holds the response we get when there is an error during authentication.
* */
data class NetworkErrorResponse(
    @SerializedName("detail") val errorMessage: String
)
