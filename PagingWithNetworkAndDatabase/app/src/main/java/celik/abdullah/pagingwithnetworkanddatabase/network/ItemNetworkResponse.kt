package celik.abdullah.pagingwithnetworkanddatabase.network

import celik.abdullah.pagingwithnetworkanddatabase.model.Item
import com.google.gson.annotations.SerializedName

/*
 * Data class to hold repo responses from REST API calls.
 */
data class ItemNetworkResponse(
    @SerializedName("count") val count: Long = 0,
    @SerializedName("next") val next: String? = null,
    @SerializedName("previous") val previous: String? = null,
    @SerializedName("results") val listOfItems: List<Item> = emptyList()
)
