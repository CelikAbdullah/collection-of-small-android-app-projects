package celik.abdullah.pagingwithnetworkanddatabase.network

import retrofit2.http.GET
import retrofit2.http.Query

/*
* REST API communication setup via Retrofit
* */
interface ItemApi {

    // used to get a paginated list of items from the REST API
    @GET("items/")
    suspend fun getItems(@Query("page") page:Int) : ItemNetworkResponse

    // used to search for items
    @GET("items/")
    suspend fun getItemsBySearchQuery(@Query("search") query: String, @Query("page") page: Int): ItemNetworkResponse
}