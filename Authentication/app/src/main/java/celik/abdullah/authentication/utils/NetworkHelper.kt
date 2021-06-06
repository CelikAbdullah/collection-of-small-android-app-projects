package celik.abdullah.authentication.utils

import celik.abdullah.authentication.network.NetworkErrorResponse
import celik.abdullah.authentication.network.NetworkResult
import com.google.gson.Gson
import retrofit2.Response

fun <T : Any> handleResponseOfApiCall(response: Response<T>): NetworkResult<T> {
    /*
    * we divide every response in 2 branches: a successful response and an unsuccessful response
    * In the successful branch, we check if the response's body is null. If not, we wrap the body into
    * NetworkResult.Success. Otherwise, we return a NetworkResult.InvalidData (if the response is not 204)
    * The reason for the special case (looking at code==204) is because the logout request with returned code 204
    * is considered as a successful response.
    *
    * In the unsuccessful branch, we convert every response with code >= 400 into a specific error. That way, the user
    * get informed what happened (e.g. if the user has given a wrong username during the login, then he/she gets the
    * message "Please check the username.It might be wrong."
    * Otherwise, we return just NetworkResult.InvalidData.
    * */
    return if(response.isSuccessful){
        val body = response.body()
        // if response is succesful and not null, then everything is ok
        if(body!=null) NetworkResult.Success(body)
        else{
            // if response's body is null but its code is 204 then it is still okay; this special case happens when we make a logout request
            if(response.code()==204) NetworkResult.Success(body)
            // is response's body is null, we return invalid data to indicate that sth. unknown happened
            else NetworkResult.InvalidData
        }
    }
    else{
        // if the response is not successful, we check its code & convert the specific error into NetworkErrorResponse
        if(response.code()>=400) NetworkResult.Error(Gson().fromJson(response.errorBody()?.charStream(), NetworkErrorResponse::class.java))
        // otherwise, just inform the user that sth. unknown happened
        else NetworkResult.InvalidData
    }
}
