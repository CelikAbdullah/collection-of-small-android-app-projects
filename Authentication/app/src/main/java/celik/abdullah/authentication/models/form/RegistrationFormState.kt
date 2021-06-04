package celik.abdullah.authentication.models.form

/**
 * Holds the state of our registration procedure.
 * */
data class RegistrationFormState(
    val emailError: Int? = null,
    val usernameError: Int? = null,
    val passwordError: Int? = null,
    val isDataValid: Boolean = false
)