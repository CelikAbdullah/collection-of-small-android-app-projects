package celik.abdullah.authentication.models.result

import celik.abdullah.authentication.network.AuthenticationResponse

/*
* Holds the result we get after a registration/login process.
* This data class will be populated with the response we get from the server.
* The response can be success, error or networkError.
* It will be wrapped into a LiveData object so that a Fragment can listen to it.
* E.g. LoginFragment or RegistrationFragment listens to this to decide which
*      action to take.
* */
data class AuthenticationResult(
    val success: AuthenticationResponse? = null,
    val error: String? = null,
    val networkError: Int? = null,
    val invalidData : Int? = null,
)