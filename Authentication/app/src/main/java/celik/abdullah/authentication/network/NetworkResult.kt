package celik.abdullah.authentication.network

/**
 * A generic class that holds a value with its loading status.
 */
sealed class NetworkResult<out T : Any> {
    // contains the data we get from the web service when everthing is ok
    data class Success<out T : Any>(val data: T) : NetworkResult<T>()
    // contains the specific error message from the webservice when something during the authentication went wrong
    data class Error(val error: NetworkErrorResponse? = null) : NetworkResult<Nothing>()
    // contains "nothing"; tells the user that he/she has no internet access
    object NetworkError: NetworkResult<Nothing>()
}
