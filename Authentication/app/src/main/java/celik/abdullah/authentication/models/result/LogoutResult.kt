package celik.abdullah.authentication.models.result


/*
* Holds the result we get after a logout request.
* This data class will be populated with the response we get from the server.
* The response can be success, error or networkError.
* It will be wrapped into a LiveData object so that a Fragment can listen to it.
* E.g. the LoginFragment listens to this to decide which action it should take.
* */
data class LogoutResult(
    val success: Int? = null,
    val error: String? = null,
    val networkError: Int? = null,
    val invalidData : Int? = null
)